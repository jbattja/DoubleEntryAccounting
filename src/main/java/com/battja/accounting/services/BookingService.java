package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.BookingEvent;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.repositories.BookingRepository;
import com.battja.accounting.repositories.JournalRepository;
import com.battja.accounting.repositories.TransactionRepository;
import com.battja.accounting.util.JournalUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    public Journal book(@NonNull Transaction transaction, @NonNull EventType event) {
        return book(transaction,event,null);
    }

    @Transactional
    public Journal book(@NonNull Transaction transaction, @NonNull EventType event, Collection<Batch> batchesToUse) {
        Set<Transaction> transactions = new HashSet<>();
        transactions.add(transaction);
        AdditionalBookingInfo additionalBookingInfo = new AdditionalBookingInfo();
        additionalBookingInfo.setTransactions(transactions);
        return book(event,additionalBookingInfo,batchesToUse);
    }

    @Transactional
    public Journal book(@NonNull Set<Transaction> transactions, @NonNull EventType event) {
        AdditionalBookingInfo additionalBookingInfo = new AdditionalBookingInfo();
        additionalBookingInfo.setTransactions(transactions);
        return book(event,additionalBookingInfo,null);
    }

    @Transactional
    public Journal book(@NonNull ReportLine reportLine, @NonNull EventType event, Collection<Batch> batchesToUse) {
        AdditionalBookingInfo additionalBookingInfo = new AdditionalBookingInfo();
        additionalBookingInfo.setReportLine(reportLine);
        return book(event,additionalBookingInfo,batchesToUse);
    }

    @Transactional
    public Journal book(@NonNull EventType event, @Nullable AdditionalBookingInfo additionalBookingInfo, Collection<Batch> batchesToUse) {
        BookingEvent bookingEvent;
        try {
            bookingEvent = event.getEventClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn(e.getMessage());
            return null;
        }
        if (additionalBookingInfo == null) {
            additionalBookingInfo = new AdditionalBookingInfo();
        }
        try {
            findFees(additionalBookingInfo,event);
            return bookInternal(bookingEvent, additionalBookingInfo, batchesToUse);
        } catch (BookingException e) {
            log.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    private Journal bookInternal(@NonNull BookingEvent event, @NonNull AdditionalBookingInfo additionalBookingInfo, Collection<Batch> batchesToUse) throws BookingException {
        event.book(additionalBookingInfo,batchesToUse);
        for (Booking booking : event.getBookings()) { // set batches
            if (booking.getBatch() == null) {
                booking.setBatch(batchService.findOrCreateAvailableBatch(booking));
            }
        }
        return storeJournal(new Journal(event.getBookings(), event.getEventTypeName()),additionalBookingInfo);
    }

    @Transactional
    public boolean bookBalanceTransfer(@NonNull Integer batchId) {
        Batch batch = batchService.getBatch(batchId);
        if (batch == null) {
            log.warn("No batch found with id " + batchId);
            return false;
        }
        List<Amount> openAmounts = batchService.getOpenAmounts(batch);
        if (openAmounts.size() > 0) {
            if (batch.getStatus().equals(Batch.BatchStatus.AVAILABLE)) {
                batchService.endBatchPeriod(batch.getId());
            }
            try {
                Batch newBatch = batchService.findOrCreateAvailableBatch(batch.getAccount(), batch.getRegister());
                Journal journal = createBalanceTransfer(batch, newBatch, openAmounts);
                storeJournal(journal,null);
                return true;
            } catch (BookingException e) {
                log.warn(e.getMessage());
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
        } else {
            log.warn("Cannot book balance transfer: " + batch.getId());
            return false;
        }
    }

    @Transactional
    private Journal createBalanceTransfer(@NonNull Batch batchFrom, @NonNull Batch batchTo, @NonNull List<Amount> amounts) {
        List<Booking> bookings = new ArrayList<>();
        for (Amount amount : amounts) {
            log.info("Booking Balance transfer between " + batchFrom.getId() + " and " + batchTo.getId() + ". Amount: " + amount);
            bookings.add(new Booking(batchFrom.getAccount(),batchFrom.getRegister(),amount.getValue()*-1,amount.getCurrency(),batchFrom));
            bookings.add(new Booking(batchTo.getAccount(),batchTo.getRegister(),amount.getValue(),amount.getCurrency(),batchTo));
        }
        return new Journal(bookings, "BalanceTransfer");
    }

    @Transactional
    private Journal storeJournal(@NonNull Journal journal, @Nullable AdditionalBookingInfo additionalBookingInfo) throws BookingException {
        if (!JournalUtil.isBalanced(journal)) {
            throw new BookingException("Journal not balanced for booking " + journal.getEventType());
        }
        journal = journalRepository.save(journal);
        log.info("Created journal " + journal);
        for (Booking booking : journal.getBookings()) {
            booking.setJournal(journal);
            bookingRepository.save(booking);
            log.info("Created booking " + booking);
            batchService.updateBatchEntries(booking);
        }
        if (additionalBookingInfo != null && additionalBookingInfo.getTransactions() != null) {
            for (Transaction t : additionalBookingInfo.getTransactions()) {
                transactionRepository.save(t);
            }
        }
        return journal;
    }

    private void findFees(@NonNull AdditionalBookingInfo additionalBookingInfo, @NonNull EventType eventType) {
        Map<Account,Fee> feeMap = new HashMap<>();
        additionalBookingInfo.setFees(feeMap);
        if (additionalBookingInfo.getTransactions() == null || additionalBookingInfo.getTransactions().isEmpty()) {
            return;
        }
        Set<Account> accounts = new HashSet<>();
        Set<Integer> addedAccountIds = new HashSet<>();
        Set<PaymentMethod> paymentMethods = new HashSet<>();
        for (Transaction t : additionalBookingInfo.getTransactions()) {
            if (t.getMerchantAccount() != null && !addedAccountIds.contains(t.getMerchantAccount().getId())) {
                accounts.add(t.getMerchantAccount());
                addedAccountIds.add(t.getMerchantAccount().getId());
            }
            if (t.getPartnerAccount() != null && !addedAccountIds.contains(t.getPartnerAccount().getId())) {
                accounts.add(t.getPartnerAccount());
                addedAccountIds.add(t.getPartnerAccount().getId());
            }
            if (t.getPaymentMethod() != null) {
                paymentMethods.add(t.getPaymentMethod());
            }
        }
        if (paymentMethods.size() != 1) {
            return;
        }
        for (Account account : accounts) {
            Contract contract = account.getContract();
            if (contract == null && account.getParent() != null) {
                contract = account.getParent().getContract();
            }
            if (contract == null) {
                continue;
            }
            List<Fee> possibleFees = feeService.getFeeLines(contract, eventType);
            if (possibleFees.isEmpty()) {
                continue;
            }
            Fee matchedFee = null;
            for (Fee fee : possibleFees) {
                if (paymentMethods.contains(fee.getPaymentMethod())) {
                    matchedFee = fee;
                    break;
                }
            }
            if (matchedFee == null) {
                for (Fee fee : possibleFees) {
                    if (fee.getPaymentMethod() == null) {
                        matchedFee = fee;
                        break;
                    }
                }
            }
            if (matchedFee != null) {
                feeMap.put(account,matchedFee);
            }
        }
        additionalBookingInfo.setFees(feeMap);
    }

    @Autowired
    BatchService batchService;
    @Autowired
    FeeService feeService;
    @Autowired
    JournalRepository journalRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    TransactionRepository transactionRepository;


}

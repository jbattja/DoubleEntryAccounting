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
        Set<Transaction> transactions = new HashSet<>();
        transactions.add(transaction);
        return book(transactions,event,null);
    }

    @Transactional
    public Journal book(@NonNull Set<Transaction> transactions, @NonNull EventType event) {
        return book(transactions,event,null);
    }

    @Transactional
    public Journal book(@NonNull Set<Transaction> transactions, @NonNull EventType event, AdditionalInfo additionalInfo) {
        BookingEvent bookingEvent;
        try {
            bookingEvent = event.getEventClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn(e.getMessage());
            return null;
        }
        try {
            Map<Account,Fee> fees = findFees(transactions,event);
            return bookInternal(transactions,bookingEvent, additionalInfo, fees);
        } catch (BookingException e) {
            log.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
    }

    private Journal bookInternal(@NonNull Set<Transaction> transactions, @NonNull BookingEvent event, AdditionalInfo additionalInfo, @NonNull Map<Account, Fee> fees) throws BookingException {
        event.book(transactions,additionalInfo, fees);
        for (Booking booking : event.getBookings()) { // set batches
            booking.setBatch(batchService.findOrCreateAvailableBatch(booking));
        }
        return storeJournal(new Journal(event.getBookings(), event.getEventTypeName()),transactions);
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
            bookings.add(new Booking(batchFrom.getAccount(),batchFrom.getRegister(),amount.getValue()*-1,amount.getCurrency(),batchFrom,null));
            bookings.add(new Booking(batchTo.getAccount(),batchTo.getRegister(),amount.getValue(),amount.getCurrency(),batchTo,null));
        }
        return new Journal(bookings, "BalanceTransfer");
    }

    @Transactional
    private Journal storeJournal(@NonNull Journal journal, @Nullable Set<Transaction> transactions) throws BookingException {
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
        if (transactions != null) {
            for (Transaction t : transactions) {
                transactionRepository.save(t);
            }
        }
        return journal;
    }

    private Map<Account,Fee> findFees(@NonNull Set<Transaction> transactions, @NonNull EventType eventType) {
        Set<Account> accounts = new HashSet<>();
        Set<PaymentMethod> paymentMethods = new HashSet<>();
        for (Transaction t : transactions) {
            accounts.add(t.getMerchantAccount());
            accounts.add(t.getPartnerAccount());
            if (t.getPaymentMethod() != null) {
                paymentMethods.add(t.getPaymentMethod());
            }
        }
        Map<Account,Fee> feeMap = new HashMap<>();
        if (paymentMethods.size() != 1) {
            return feeMap;
        }
        for (Account account : accounts) {
            List<Fee> possibleFees = feeService.getFeeLines(account.getContract(), eventType);
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
        return feeMap;
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

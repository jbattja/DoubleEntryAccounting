package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.journals.Amount;
import com.battja.accounting.journals.RegisterType;
import com.battja.accounting.repositories.BatchEntryRepository;
import com.battja.accounting.repositories.BookingRepository;
import com.battja.accounting.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private short counter = 0;

    @Value("${system.transactionprefix}")
    private String transactionPrefix;

    @Transactional
    public Transaction newPayment(@NonNull Amount amount, @NonNull Account merchantAccount, @NonNull Account acquirerAccount) {
        if (!merchantAccount.getAccountType().equals(Account.AccountType.MERCHANT)) {
            throw new IllegalArgumentException("Not a valid merchant account: " + merchantAccount);
        }
        if (!acquirerAccount.getAccountType().equals(Account.AccountType.ACQUIRER_ACCOUNT)) {
            throw new IllegalArgumentException("Not a valid acquirer account: " + acquirerAccount);
        }
        Transaction transaction = new Transaction();
        transaction.setMerchantAccount(merchantAccount);
        transaction.setAcquirerAccount(acquirerAccount);
        transaction.setAmount(amount.getValue());
        transaction.setCurrency(amount.getCurrency());
        transaction.setType(Transaction.TransactionType.PAYMENT);
        transaction.setTransactionReference(createTransactionReference());
        log.info("Storing transaction: " + transaction);
        transaction = transactionRepository.save(transaction);
        bookingService.book(transaction, EventType.RECEIVED);
        return transaction;
    }

    @Transactional
    public Transaction newCapture(@NonNull Transaction payment, @NonNull Amount amount) throws BookingException {
        payment = transactionRepository.findById(payment.getId()).orElse(null);
        if (payment == null) {
            throw new BookingException("Capture failed, invalid payment");
        }
        if (!payment.getCurrency().equals(amount.getCurrency())) {
            throw new BookingException("Capture failed, invalid currency. Tried to capture " + amount + " for payment: " + payment );
        }
        if (getRemainingBalance(payment) < amount.getValue()) {
            throw new BookingException("Capture failed, capture amount too high. Amount: " + amount + ", payment: " + payment);
        }
        Transaction capture = new Transaction();
        capture.setMerchantAccount(payment.getMerchantAccount());
        capture.setAcquirerAccount(payment.getAcquirerAccount());
        capture.setAmount(amount.getValue());
        capture.setCurrency(amount.getCurrency());
        capture.setType(Transaction.TransactionType.CAPTURE);
        capture.setTransactionReference(payment.getTransactionReference());
        capture.setModificationReference(createTransactionReference());
        capture = transactionRepository.save(capture);
        Set<Transaction> transactionSet = new HashSet<>();
        transactionSet.add(payment);
        transactionSet.add(capture);
        bookingService.book(transactionSet,EventType.PAID);
        return capture;
    }

    public String createTransactionReference() {
        counter++;
        if (counter > 999) {
            counter = 0;
        }
        String counterString = String.format("%03d",counter);
        return transactionPrefix + new Date().getTime() + counterString;
    }

    public Set<Journal> getJournalsByTransaction(Transaction transaction) {
        List<BatchEntry> batchEntries = batchEntryRepository.findByTransaction(transaction);
        Set<Journal> journals = new HashSet<>();
        for (BatchEntry entry : batchEntries) {
            journals.addAll(entry.getJournals());
        }
        return journals;
    }

    public List<Booking> getBookingsByTransaction(Transaction transaction) {
        return bookingRepository.findByTransaction(transaction);
    }


    public Transaction getByReference(String transactionReference) {
        List<Transaction> list = transactionRepository.findByTransactionReference(transactionReference);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            log.warn("More than 1 transaction with reference " + transactionReference);
        }
        return list.get(0);
    }

    public Transaction getTransaction(Integer transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }

    public List<Transaction> listAllPayments() {
        return transactionRepository.findByType(Transaction.TransactionType.PAYMENT);
    }

    public List<Transaction> listAllModifications(Transaction transaction) {
        List<Transaction> transactions = transactionRepository.findByTransactionReference(transaction.getTransactionReference());
        List<Transaction> modifications = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.getType().equals(Transaction.TransactionType.PAYMENT)) {
                modifications.add(t);
            }
        }
        return modifications;
    }


    public String getLatestJournal(Transaction transaction) {
        List<BatchEntry> entries = batchEntryRepository.findByTransaction(transaction);
        Set<Journal> journals = new HashSet<>();
        for (BatchEntry e : entries) {
            journals.addAll(e.getJournals());
        }
        Journal latestJournal = null;
        for (Journal j : journals) {
            if (latestJournal == null) {
                latestJournal = j;
            } else if (latestJournal.getDate().before(j.getDate())) {
                latestJournal = j;
            }
        }
        if (latestJournal != null) {
            return latestJournal.getEventType();
        }
        return "";
    }

    public boolean canTransactionCapture(@NonNull Transaction transaction) {
        return getRemainingBalance(transaction) > 0L;
    }

    public long getRemainingBalance(@NonNull Transaction transaction) {
        List<Booking> bookingsList = bookingRepository.findByTransaction(transaction);
        long authorisedBalance = 0L;
        for (Booking b : bookingsList) {
            if (b.getRegister().equals(RegisterType.AUTHORISED) && b.getAccount().getAccountName().equals(transaction.getMerchantAccount().getAccountName())) {
                authorisedBalance = authorisedBalance + b.getAmount();
            }
        }
        return authorisedBalance;
    }

    public boolean canTransactionAuthorize(@NonNull Transaction transaction) {
        List<Booking> bookingsList = bookingRepository.findByTransaction(transaction);
        long receivedBalance = 0L;
        for (Booking b : bookingsList) {
            if (b.getRegister().equals(RegisterType.RECEIVED) && b.getAccount().getAccountName().equals(transaction.getMerchantAccount().getAccountName())) {
                receivedBalance = receivedBalance + b.getAmount();
            }
        }
        return receivedBalance > 0L;
    }

    @Autowired
    BatchEntryRepository batchEntryRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    BookingService bookingService;
    @Autowired
    TransactionRepository transactionRepository;

}

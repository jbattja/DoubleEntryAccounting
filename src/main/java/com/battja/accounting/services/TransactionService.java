package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.exceptions.UnableToRouteException;
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
    String transactionPrefix;

    @Transactional
    public Transaction newPayment(@NonNull Amount amount, @NonNull PaymentMethod paymentMethod, @NonNull Account merchantAccount) throws UnableToRouteException {
        if (!merchantAccount.getAccountType().equals(Account.AccountType.MERCHANT)) {
            log.warn("Failed to create Payment: not a valid merchant account: " + merchantAccount);
            throw new IllegalArgumentException("Not a valid merchant account: " + merchantAccount.getAccountName());
        }
        Transaction transaction = new Transaction();
        transaction.setMerchantAccount(merchantAccount);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setAmount(amount.getValue());
        transaction.setCurrency(amount.getCurrency());
        transaction.setType(Transaction.TransactionType.PAYMENT);
        Account partnerAccount = routingService.getPartnerAccount(merchantAccount,transaction);
        if (partnerAccount == null) {
            log.warn("Not able to route payment: " + transaction);
            throw new UnableToRouteException("Not able to route " + paymentMethod + " payment in " + transaction.getCurrency() + " for merchant " + merchantAccount.getAccountName());
        }
        if (!partnerAccount.getAccountType().equals(Account.AccountType.PARTNER_ACCOUNT)) {
            log.warn("Failed to create Payment: not a valid partner account: " + partnerAccount);
            throw new IllegalArgumentException("Not a valid partner account: " + partnerAccount.getAccountName());
        }
        transaction.setPartnerAccount(partnerAccount);
        transaction.setTransactionReference(createTransactionReference());
        transaction = transactionRepository.save(transaction);
        bookingService.book(transaction, EventType.RECEIVED);
        log.info("Created transaction: " + transaction);
        return transaction;
    }

    @Transactional
    public Transaction newCapture(@NonNull Transaction payment, @NonNull Amount amount) throws BookingException {
        payment = transactionRepository.findById(payment.getId()).orElse(null);
        if (payment == null) {
            log.warn("Capture failed, payment is null");
            throw new BookingException("Capture failed, invalid payment");
        }
        if (!payment.getCurrency().equals(amount.getCurrency())) {
            log.info("Capture failed, invalid currency. Tried to capture " + amount + " for payment: " + payment);
            throw new BookingException("Capture failed, invalid currency");
        }
        if (getRemainingBalance(RegisterType.AUTHORISED,payment) < amount.getValue()) {
            log.info("Capture failed, capture amount too high. Amount: " + amount + ", payment: " + payment);
            throw new BookingException("Capture failed, capture amount too high");
        }
        Transaction capture = new Transaction();
        capture.setMerchantAccount(payment.getMerchantAccount());
        capture.setPartnerAccount(payment.getPartnerAccount());
        capture.setAmount(amount.getValue());
        capture.setCurrency(amount.getCurrency());
        capture.setType(Transaction.TransactionType.CAPTURE);
        capture.setPaymentMethod(payment.getPaymentMethod());
        capture.setTransactionReference(createTransactionReference());
        capture.setOriginalReference(payment.getTransactionReference());
        capture = transactionRepository.save(capture);
        Set<Transaction> transactionSet = new HashSet<>();
        transactionSet.add(payment);
        transactionSet.add(capture);
        bookingService.book(transactionSet,EventType.PAID);
        return capture;
    }

    @Transactional
    public Transaction newWithdrawal(@NonNull Amount amount, @NonNull Account merchantAccount, @NonNull Account bankAccount) {
        if (!merchantAccount.getAccountType().equals(Account.AccountType.MERCHANT)) {
            log.warn("Failed to create Withdrawal: not a valid merchant account: " + merchantAccount);
            throw new IllegalArgumentException("Not a valid merchant account: " + merchantAccount.getAccountName());
        }
        if (!bankAccount.getAccountType().equals(Account.AccountType.BANK_ACCOUNT)) {
            log.warn("Failed to create Payment: not a valid partner account: " + bankAccount);
            throw new IllegalArgumentException("Not a valid partner account: " + bankAccount.getAccountName());
        }
        Transaction transaction = new Transaction();
        transaction.setMerchantAccount(merchantAccount);
        transaction.setPartnerAccount(bankAccount);
        transaction.setAmount(amount.getValue());
        transaction.setCurrency(amount.getCurrency());
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setTransactionReference(createTransactionReference());
        transaction = transactionRepository.save(transaction);

        log.info("Created transaction: " + transaction);
        return transaction;
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

    public List<Booking> getBookingsByTransactions(List<Transaction> transactions) {
        return bookingRepository.findByTransactionIn(transactions);
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
        return transactionRepository.findByOriginalReference(transaction.getTransactionReference());
    }

    private long getRemainingBalance(RegisterType registerType, Transaction transaction) {
        List<Booking> bookingsList = bookingRepository.findByTransaction(transaction);
        long balance = 0L;
        for (Booking b : bookingsList) {
            if (b.getRegister().equals(registerType) && b.getAccount().getAccountName().equals(transaction.getMerchantAccount().getAccountName())) {
                balance = balance + b.getAmount();
            }
        }
        return balance;
    }

    public boolean canTransactionCapture(@NonNull Transaction transaction) {
        return getRemainingBalance(RegisterType.AUTHORISED, transaction) > 0L;
    }

    public boolean canTransactionAuthorize(@NonNull Transaction transaction) {
        return getRemainingBalance(RegisterType.RECEIVED, transaction) > 0L;
    }

    public boolean isCaptureStillOpen(Transaction capture) {
        return getRemainingBalance(RegisterType.CAPTURED, capture) > 0L;
    }

    public void authorizeOrRefusePayment(@NonNull Transaction transaction, boolean authorisedSuccess) throws BookingException {
        transaction = transactionRepository.findById(transaction.getId()).orElse(null);
        if (transaction == null) {
            log.warn("Cannot authorize payment: payment is null");
            throw new BookingException("Cannot authorize payment: invalid payment");
        }
        if (transaction.getType() != Transaction.TransactionType.PAYMENT) {
            log.warn("Cannot authorize payment: type is: " + transaction.getType());
            throw new BookingException("Cannot authorize payment: invalid payment");
        }
        Journal journal;
        if (authorisedSuccess) {
            journal = bookingService.book(transaction, EventType.AUTHORISED);
        } else {
            journal = bookingService.book(transaction, EventType.REFUSED);
        }
        if (journal == null) {
            throw new BookingException("Failed to authorize payment");
        }
    }

    public void cancelRemainingAmount(@NonNull Transaction transaction) throws BookingException {
        transaction = transactionRepository.findById(transaction.getId()).orElse(null);
        if (transaction == null) {
            log.warn("Cannot cancel payment: payment is null");
            throw new BookingException("Cannot cancel payment: invalid payment");
        }
        if (transaction.getType() != Transaction.TransactionType.PAYMENT) {
            log.warn("Cannot cancel payment: type is: " + transaction.getType());
            throw new BookingException("Cannot cancel payment: invalid payment");
        }
        long amount = getRemainingBalance(RegisterType.AUTHORISED,transaction);
        transaction.setAmount(amount);
        Journal journal = bookingService.book(transaction, EventType.CANCELLED);
        if (journal == null) {
            throw new BookingException("Failed to cancel payment");
        }
    }

    public void bookEarlySettlementToMerchant(@NonNull Transaction capture, Long amount) throws BookingException {
        capture = transactionRepository.findById(capture.getId()).orElse(null);
        if (capture == null) {
            log.warn("Cannot book settlement: capture is null");
            throw new BookingException("Cannot book settlement: invalid capture");
        }
        if (capture.getType() != Transaction.TransactionType.CAPTURE) {
            log.warn("Cannot book settlement: type is: " + capture.getType());
            throw new BookingException("Cannot book settlement: invalid capture");
        }
        List<Transaction> paymentList = transactionRepository.findByTransactionReference(capture.getOriginalReference());
        if (paymentList.size() != 1) {
            log.warn("Expected 1 payment with reference " + capture.getOriginalReference() + ", but found " + paymentList.size());
            throw new BookingException("Cannot book settlement");
        }
        long captureBalance = getRemainingBalance(RegisterType.CAPTURED,capture);
        if (captureBalance == 0) {
            log.info("Not able to book settlement, no open amount");
            throw new BookingException("Not able to book settlement, no open amount");
        }
        if (amount != null) {
            if (amount > captureBalance) {
                log.info("Not able to book settlement, amount too high. Amount request to settle: " + amount + ". Capture balance " + captureBalance);
                throw new BookingException("Not able to book capture to settlement, amount too high");
            }
        } else {
            amount = captureBalance;
        }
        capture.setAmount(amount); // overriding the amount, so we only settle the remaining amount
        Set<Transaction> transactionList = new HashSet<>();
        transactionList.add(paymentList.get(0));
        transactionList.add(capture);
        AdditionalInfo additionalInfo = new AdditionalInfo();
        additionalInfo.setFundingSource(Account.defaultPspAccount());
        Journal journal = bookingService.book(transactionList, EventType.SETTLED_TO_MERCHANT, additionalInfo);
        if (journal == null) {
            throw new BookingException("Failed to book settlement failed");
        }
    }

    public void bookSettlementFailed(@NonNull Transaction capture) throws BookingException {
        capture = transactionRepository.findById(capture.getId()).orElse(null);
        if (capture == null) {
            log.warn("Cannot book settlement failed: capture is null");
            throw new BookingException("Cannot book settlement failed: invalid capture");
        }
        if (capture.getType() != Transaction.TransactionType.CAPTURE) {
            log.warn("Cannot book settlement failed: type is: " + capture.getType());
            throw new BookingException("Cannot book settlement failed: invalid capture");
        }
        List<Transaction> paymentList = transactionRepository.findByTransactionReference(capture.getOriginalReference());
        if (paymentList.size() != 1) {
            log.warn("Expected 1 payment with reference " + capture.getOriginalReference() + ", but found " + paymentList.size());
            throw new BookingException("Cannot book settlement failed");
        }
        long amount = getRemainingBalance(RegisterType.CAPTURED,capture);
        if (amount == 0) {
            log.info("Not able to book capture to settlement failed, no open amount");
            throw new BookingException("Not able to book capture to settlement failed, no open amount");
        }
        capture.setAmount(amount); // overriding the amount, so we only close the remaining amount
        Set<Transaction> transactionList = new HashSet<>();
        transactionList.add(paymentList.get(0));
        transactionList.add(capture);
        Journal journal = bookingService.book(transactionList, EventType.SETTLEMENT_FAILED);
        if (journal == null) {
            throw new BookingException("Failed to book settlement failed");
        }
    }

    @Transactional
    public boolean withdrawMerchantPayable(@NonNull Integer batchId) {
        Batch batch = batchService.getBatch(batchId);
        if (batch == null) {
            log.warn("No batch found with id " + batchId);
            return false;
        }
        if (!batch.getRegister().equals(RegisterType.PAYABLE) || !batch.getAccount().getAccountType().equals(Account.AccountType.MERCHANT)) {
            log.warn("Batch is not a merchant payable batch: " + batch);
            return false;
        }
        List<Amount> openAmounts = batchService.getOpenAmounts(batch);
        if (batch.getStatus().equals(Batch.BatchStatus.AVAILABLE)) {
            batchService.endBatchPeriod(batch.getId());
        }
        List<Journal> journals = new ArrayList<>();
        for (Amount amount : openAmounts) {
            Account bankAccount = routingService.getSettlementAccount(batch.getAccount(),amount.getCurrency());
            if (bankAccount == null) {
                log.warn("Unable to create a " + amount.getCurrency() + " withdrawal for merchant " + batch.getAccount().getAccountName() + ". No settlement account found.");
                continue;
            }
            Transaction withdrawal = newWithdrawal(amount,batch.getAccount(),bankAccount);
            AdditionalInfo additionalInfo = new AdditionalInfo();
            additionalInfo.setFromBatch(batch);
            journals.add(bookingService.book(withdrawal, EventType.MERCHANT_WITHDRAWAL, additionalInfo));
        }
        return journals.size() > 0;
    }


    @Autowired
    BatchEntryRepository batchEntryRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    BookingService bookingService;
    @Autowired
    BatchService batchService;
    @Autowired
    RoutingService routingService;
    @Autowired
    TransactionRepository transactionRepository;

}

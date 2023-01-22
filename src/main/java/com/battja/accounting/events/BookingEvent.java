package com.battja.accounting.events;

import com.battja.accounting.entities.*;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.services.FeeService;
import org.springframework.lang.NonNull;

import java.util.*;

public abstract class BookingEvent {

    private final List<Booking> bookings;
    private Set <Transaction> transactions;
    private AdditionalInfo additionalInfo;
    private Map<Account,Fee> fees;
    protected AdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }
    protected Map<Account,Fee> getFees() {
        return fees;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public BookingEvent() {
        this.bookings = new ArrayList<>();
    }

    public abstract String getEventTypeName();
    protected abstract Transaction.TransactionType[] requiredTransactionTypes();

    private boolean validateTransactions(Set<Transaction> transactions) {
        if (transactions.size() != requiredTransactionTypes().length) {
            return false;
        }
        for (Transaction.TransactionType transactionType : requiredTransactionTypes()) {
            boolean typeFound = false;
            for (Transaction t : transactions) {
                if (t.getType().equals(transactionType)) {
                    typeFound = true;
                    break;
                }
            }
            if (!typeFound) return false;
        }
        return true;
    }

    public void book(@NonNull Set<Transaction> transactions, @NonNull AdditionalInfo additionalInfo, @NonNull Map<Account, Fee> fees) throws BookingException {
        if (!validateTransactions(transactions)) {
            throw new BookingException("Incorrect input of transactions for this BookingEvent " + getEventTypeName());
        }
        this.transactions = transactions;
        this.additionalInfo = additionalInfo;
        this.fees = fees;
        bookInternal();
        for (Transaction transaction : transactions) {
            transaction.setStatus(this.getEventTypeName());
        }
        deductFeesFromPayable();
    }

    private void deductFeesFromPayable() {
        // deduct fees from Payable
        Booking payableBooking = null;
        for (Booking booking : bookings) {
            if (RegisterType.PAYABLE.equals(booking.getRegister())) {
                payableBooking = booking;
                break;
            }
        }
        if (payableBooking == null) {
            return;
        }
        long deductionAmount = 0;
        List<Booking> bookingsToInvert = new ArrayList<>();
        for (Booking booking : bookings) {
            if (RegisterType.FEES.equals(booking.getRegister()) && payableBooking.getAccount().equals(booking.getAccount())
                    && payableBooking.getCurrency().equals(booking.getCurrency())) {
                deductionAmount += booking.getAmount();
                bookingsToInvert.add(booking);
            }
        }
        if (deductionAmount != 0) {
            payableBooking.setAmount(payableBooking.getAmount() + deductionAmount);
            for (Booking bookingToInvert : bookingsToInvert) {
                addBooking(bookingToInvert.getAccount(), bookingToInvert.getRegister(),
                        new Amount(bookingToInvert.getCurrency(), bookingToInvert.getAmount() * -1), bookingToInvert.getTransaction());
            }
        }
    }

    protected void bookFees(Transaction transaction) {
        for (Map.Entry<Account, Fee> fee : getFees().entrySet()) {
            List<Amount> calculatedFees =  FeeService.calculateFee(fee.getValue(),getCreditAmount(transaction));
            for (Amount amount : calculatedFees) {
                if (amount.getValue() != 0) {
                    addBooking(fee.getKey(),RegisterType.FEES,new Amount(amount.getCurrency(),amount.getValue()*-1),transaction);
                    addBooking(Account.defaultPspAccount(),RegisterType.REVENUE,new Amount(amount.getCurrency(),amount.getValue()),transaction);
                }
            }
        }
    }

    protected abstract void bookInternal() throws BookingException;

    protected void addBooking(@NonNull Account account, @NonNull RegisterType register, @NonNull Amount amount, Transaction transaction) {
        bookings.add(new Booking(account, register, amount.getValue(),amount.getCurrency(), null, transaction));
    }

    protected Amount getCreditAmount(Transaction transaction) {
        return new Amount(transaction.getCurrency(),transaction.getAmount());
    }

    protected Amount getDebitAmount(Transaction transaction) {
        return new Amount(transaction.getCurrency(),transaction.getAmount()*-1);
    }

    protected Transaction getTransaction(Transaction.TransactionType type) throws BookingException {
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(type)) {
                return transaction;
            }
        }
        throw new BookingException("Internal booking error: no transaction with transaction type " + type + " found");
    }


}

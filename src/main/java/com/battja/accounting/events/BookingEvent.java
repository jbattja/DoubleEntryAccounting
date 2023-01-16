package com.battja.accounting.events;

import com.battja.accounting.entities.*;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.entities.Amount;
import com.battja.accounting.entities.RegisterType;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BookingEvent {

    private final List<Booking> bookings;
    private Set <Transaction> transactions;
    private AdditionalInfo additionalInfo;
    protected AdditionalInfo getAdditionalInfo() {
        return additionalInfo;
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

    public void book(@NonNull Set <Transaction> transactions, @NonNull AdditionalInfo additionalInfo) throws BookingException {
        if (!validateTransactions(transactions)) {
            throw new BookingException("Incorrect input of transactions for this BookingEvent " + getEventTypeName());
        }
        this.transactions = transactions;
        this.additionalInfo = additionalInfo;
        bookInternal();
        for (Transaction transaction : transactions) {
            transaction.setStatus(this.getEventTypeName());
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

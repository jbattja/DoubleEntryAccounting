package com.battja.accounting.events;

import com.battja.accounting.entities.*;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.journals.Amount;
import com.battja.accounting.journals.RegisterType;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BookingEvent {

    private List<Booking> bookings;

    public List<Booking> getBookings() {
        return bookings;
    }

    public BookingEvent() {
        this.bookings = new ArrayList<>();
    }

    public abstract String getEventTypeName();
    protected abstract Transaction.TransactionType[] requiredTransactionTypes();
    public boolean validateTransactions(Set<Transaction> transactions) {
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

    public void book(@NonNull Set <Transaction> transactions) throws BookingException {
        bookInternal(transactions);
        for (Transaction transaction : transactions) {
            transaction.setStatus(this.getEventTypeName());
        }
    };

    protected abstract void bookInternal(@NonNull Set <Transaction> transactions) throws BookingException;

    protected boolean addBooking(@NonNull Account account, @NonNull RegisterType register, @NonNull Amount amount, Transaction transaction) throws BookingException {
        return bookings.add(new Booking(account, register, amount.getValue(),amount.getCurrency(), null, transaction));
    }

    protected Amount getCreditAmount(Transaction transaction) {
        return new Amount(transaction.getCurrency(),transaction.getAmount());
    }

    protected Amount getDebitAmount(Transaction transaction) {
        return new Amount(transaction.getCurrency(),transaction.getAmount()*-1);
    }

    protected Transaction getTransaction(Transaction.TransactionType type, Set<Transaction> transactions) throws BookingException {
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(type)) {
                return transaction;
            }
        }
        throw new BookingException("Internal booking error: no transaction with transaction type " + type + " found");
    }


}

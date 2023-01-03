package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.journals.RegisterType;

import java.util.Set;

public class RefusedEvent extends BookingEvent {
    @Override
    public String getEventTypeName() {
        return "Refused";
    }

    @Override
    public boolean validateTransactions(Set<Transaction> transactions) {
        if (transactions.size() != 1) {
            return false;
        }
        return transactions.iterator().next().getType().equals(Transaction.TransactionType.PAYMENT);
    }

    @Override
    public void bookInternal(Set<Transaction> transactions) throws BookingException {
        Transaction payment = transactions.iterator().next(); // already validated
        addBooking(payment.getAcquirerAccount(), RegisterType.RECEIVED, getCreditAmount(payment), payment);
        addBooking(payment.getMerchantAccount(), RegisterType.RECEIVED, getDebitAmount(payment), payment);
    }

}

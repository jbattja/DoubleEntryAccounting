package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.journals.RegisterType;
import org.springframework.lang.NonNull;

import java.util.Set;

public class ReceivedEvent extends BookingEvent {

    @Override
    public String getEventTypeName() {
        return "Received";
    }

    @Override
    public boolean validateTransactions(@NonNull Set<Transaction> transactions) {
        if (transactions.size() != 1) {
            return false;
        }
        return transactions.iterator().next().getType().equals(Transaction.TransactionType.PAYMENT);
    }

    @Override
    public void bookInternal(Set<Transaction> transactions) throws BookingException {
        Transaction payment = transactions.iterator().next(); // already validated
        addBooking(payment.getMerchantAccount(), RegisterType.RECEIVED, getCreditAmount(payment), payment);
        addBooking(payment.getAcquirerAccount(), RegisterType.RECEIVED, getDebitAmount(payment), payment);
    }

}

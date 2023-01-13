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
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[]{Transaction.TransactionType.PAYMENT};
    }

    @Override
    public void bookInternal(@NonNull Set<Transaction> transactions) throws BookingException {
        Transaction payment = getTransaction(Transaction.TransactionType.PAYMENT, transactions);
        addBooking(payment.getMerchantAccount(), RegisterType.RECEIVED, getCreditAmount(payment), payment);
        addBooking(payment.getAcquirerAccount(), RegisterType.RECEIVED, getDebitAmount(payment), payment);
    }

}

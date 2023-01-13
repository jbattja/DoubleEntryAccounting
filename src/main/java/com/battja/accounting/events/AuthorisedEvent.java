package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.journals.RegisterType;
import org.springframework.lang.NonNull;

import java.util.Set;

public class AuthorisedEvent extends BookingEvent {

    @Override
    public Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[] {Transaction.TransactionType.PAYMENT};
    }

    @Override
    public String getEventTypeName() {
        return "Authorised";
    }

    @Override
    public void bookInternal(@NonNull Set<Transaction> transactions) throws BookingException {
        Transaction payment = getTransaction(Transaction.TransactionType.PAYMENT, transactions);
        addBooking(payment.getAcquirerAccount(), RegisterType.RECEIVED, getCreditAmount(payment), payment);
        addBooking(payment.getMerchantAccount(), RegisterType.RECEIVED, getDebitAmount(payment), payment);
        addBooking(payment.getMerchantAccount(), RegisterType.AUTHORISED, getCreditAmount(payment), payment);
        addBooking(payment.getAcquirerAccount(), RegisterType.AUTHORISED, getDebitAmount(payment), payment);
    }
}

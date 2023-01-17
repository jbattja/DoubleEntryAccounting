package com.battja.accounting.events;

import com.battja.accounting.entities.RegisterType;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;

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
    public void bookInternal() throws BookingException {
        Transaction payment = getTransaction(Transaction.TransactionType.PAYMENT);
        addBooking(payment.getPartnerAccount(), RegisterType.RECEIVED, getCreditAmount(payment), payment);
        addBooking(payment.getMerchantAccount(), RegisterType.RECEIVED, getDebitAmount(payment), payment);
        addBooking(payment.getMerchantAccount(), RegisterType.AUTHORISED, getCreditAmount(payment), payment);
        addBooking(payment.getPartnerAccount(), RegisterType.AUTHORISED, getDebitAmount(payment), payment);
        bookFees(payment);
    }
}

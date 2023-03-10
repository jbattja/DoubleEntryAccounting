package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.entities.RegisterType;

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
    public void bookInternal() throws BookingException {
        Transaction payment = getTransaction(Transaction.TransactionType.PAYMENT);
        addBooking(payment.getMerchantAccount(), RegisterType.RECEIVED, getCreditAmount(payment), payment);
        addBooking(payment.getPartnerAccount(), RegisterType.RECEIVED, getDebitAmount(payment), payment);
        bookFees(payment);
    }

}

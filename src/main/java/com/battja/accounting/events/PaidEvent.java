package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.entities.RegisterType;
import org.springframework.lang.NonNull;

import java.util.Set;

public class PaidEvent extends BookingEvent {

    @Override
    public String getEventTypeName() {
        return "Awaiting Settlement";
    }

    @Override
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[] {Transaction.TransactionType.PAYMENT, Transaction.TransactionType.CAPTURE};
    }

    @Override
    public void bookInternal() throws BookingException {
        Transaction payment = getTransaction(Transaction.TransactionType.PAYMENT);
        Transaction capture = getTransaction(Transaction.TransactionType.CAPTURE);
        addBooking(payment.getMerchantAccount(), RegisterType.AUTHORISED, getDebitAmount(capture), payment);
        addBooking(payment.getPartnerAccount(), RegisterType.AUTHORISED, getCreditAmount(capture), payment);
        addBooking(payment.getMerchantAccount(), RegisterType.CAPTURED, getCreditAmount(capture), capture);
        addBooking(payment.getPartnerAccount(), RegisterType.CAPTURED, getDebitAmount(capture), capture);
    }

}

package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.entities.RegisterType;
import org.springframework.lang.NonNull;

import java.util.Set;

public class CancelEvent extends BookingEvent {
    @Override
    public String getEventTypeName() {
        return "Cancelled";
    }

    @Override
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[]{Transaction.TransactionType.PAYMENT};
    }

    @Override
    protected void bookInternal() throws BookingException {
        Transaction payment = getTransaction(Transaction.TransactionType.PAYMENT);
        addBooking(payment.getMerchantAccount(), RegisterType.AUTHORISED, getDebitAmount(payment), payment);
        addBooking(payment.getPartnerAccount(), RegisterType.AUTHORISED, getCreditAmount(payment), payment);
        bookFees(payment);
    }
}

package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.entities.RegisterType;
import org.springframework.lang.NonNull;

import java.util.Set;

public class SettlementFailed extends BookingEvent {

    @Override
    public String getEventTypeName() {
        return "Settlement Failed";
    }

    @Override
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[]{Transaction.TransactionType.PAYMENT, Transaction.TransactionType.CAPTURE};
    }

    @Override
    public void bookInternal(@NonNull Set<Transaction> transactions) throws BookingException {
        Transaction capture = getTransaction(Transaction.TransactionType.CAPTURE, transactions);
        addBooking(capture.getMerchantAccount(), RegisterType.CAPTURED, getDebitAmount(capture), capture);
        addBooking(capture.getAcquirerAccount(), RegisterType.CAPTURED, getCreditAmount(capture), capture);
    }

}

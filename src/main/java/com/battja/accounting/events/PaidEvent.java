package com.battja.accounting.events;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.journals.RegisterType;

import java.util.Set;

public class PaidEvent extends BookingEvent {

    @Override
    public String getEventTypeName() {
        return "Awaiting Settlement";
    }

    @Override
    public boolean validateTransactions(Set<Transaction> transactions) {
        if (transactions.size() != 2) {
            return false;
        }
        if (getTransaction(Transaction.TransactionType.PAYMENT,transactions) == null) {
            return false;
        }
        return getTransaction(Transaction.TransactionType.CAPTURE, transactions) != null;
    }

    @Override
    public void bookInternal(Set<Transaction> transactions) throws BookingException {
        Transaction payment = getTransaction(Transaction.TransactionType.PAYMENT, transactions);
        Transaction capture = getTransaction(Transaction.TransactionType.CAPTURE, transactions);
        addBooking(payment.getMerchantAccount(), RegisterType.AUTHORISED, getDebitAmount(capture), payment);
        addBooking(payment.getAcquirerAccount(), RegisterType.AUTHORISED, getCreditAmount(capture), payment);
        addBooking(payment.getMerchantAccount(), RegisterType.CAPTURED, getCreditAmount(capture), capture);
        addBooking(payment.getAcquirerAccount(), RegisterType.CAPTURED, getDebitAmount(capture), capture);
    }

    private Transaction getTransaction(Transaction.TransactionType type, Set<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(type)) {
                return transaction;
            }
        }
        return null;
    }

}

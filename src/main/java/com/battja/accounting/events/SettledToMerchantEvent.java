package com.battja.accounting.events;

import com.battja.accounting.entities.RegisterType;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;

public class SettledToMerchantEvent extends BookingEvent {
    @Override
    public String getEventTypeName() {
        return "Settled to Merchant";
    }

    @Override
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[]{Transaction.TransactionType.PAYMENT, Transaction.TransactionType.CAPTURE};
    }

    @Override
    protected void bookInternal() throws BookingException {
        Transaction capture = getTransaction(Transaction.TransactionType.CAPTURE);
        addBooking(capture.getMerchantAccount(), RegisterType.CAPTURED, getDebitAmount(capture), capture);
        addBooking(capture.getPartnerAccount(), RegisterType.CAPTURED, getCreditAmount(capture), capture);
        addBooking(capture.getMerchantAccount(), RegisterType.PAYABLE, getCreditAmount(capture), capture);
        if (getAdditionalInfo().getFundingSource() == null) {
            throw new BookingException("Unable to settle to merchant without funding source");
        }
        addBooking(getAdditionalInfo().getFundingSource(), RegisterType.EARLY_SETTLEMENT, getDebitAmount(capture),capture);
    }
}

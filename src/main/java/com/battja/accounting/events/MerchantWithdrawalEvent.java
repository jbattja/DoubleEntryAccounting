package com.battja.accounting.events;

import com.battja.accounting.entities.RegisterType;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;

public class MerchantWithdrawalEvent extends BookingEvent {
    @Override
    public String getEventTypeName() {
        return "MerchantWithdrawal";
    }

    @Override
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[]{Transaction.TransactionType.WITHDRAWAL};
    }

    @Override
    protected void bookInternal() throws BookingException {
        Transaction settlement = getTransaction(Transaction.TransactionType.WITHDRAWAL);
        addBooking(settlement.getMerchantAccount(), RegisterType.PAYABLE, getDebitAmount(settlement), settlement);
        if (getAdditionalInfo().getFromBatch() != null) {
            getBookings().get(0).setBatch(getAdditionalInfo().getFromBatch());
        }
        addBooking(settlement.getPartnerAccount(), RegisterType.PAYOUTS, getCreditAmount(settlement), settlement);
    }
}

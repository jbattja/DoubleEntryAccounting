package com.battja.accounting.events;

import com.battja.accounting.entities.RegisterType;
import com.battja.accounting.entities.ReportLine;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;

public class ReportLineMatchedEvent extends BookingEvent {
    @Override
    public String getEventTypeName() {
        return "Matched";
    }

    @Override
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[]{Transaction.TransactionType.CAPTURE};
    }

    @Override
    protected void bookInternal() throws BookingException {
        ReportLine reportLine = getReportLine();
        Transaction capture = getTransaction(Transaction.TransactionType.CAPTURE);
        if (reportLine == null) {
            throw new BookingException("Incorrect input of report lines for this BookingEvent " + getEventTypeName());
        }
        if (reportLine.getPartnerReport() == null || reportLine.getPartnerReport().getPartner() == null) {
            throw new BookingException("Report line invalid for this BookingEvent " + getEventTypeName());
        }
        addBooking(reportLine.getPartnerReport().getPartner(), RegisterType.REPORT_UNMATCHED, getCreditAmount(reportLine), reportLine);
        addBooking(reportLine.getPartnerReport().getPartner(), RegisterType.REPORT_MATCHED,getDebitAmount(capture),capture);
    }
}

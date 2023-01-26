package com.battja.accounting.events;

import com.battja.accounting.entities.RegisterType;
import com.battja.accounting.entities.ReportLine;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;

public class ReportLineReceivedEvent extends BookingEvent {
    @Override
    public String getEventTypeName() {
        return "Report Line Received";
    }

    @Override
    protected Transaction.TransactionType[] requiredTransactionTypes() {
        return new Transaction.TransactionType[0];
    }

    @Override
    protected void bookInternal() throws BookingException {
        ReportLine reportLine = getReportLine();
        if (reportLine == null) {
            throw new BookingException("Incorrect input of report lines for this BookingEvent " + getEventTypeName());
        }
        if (reportLine.getPartnerReport() == null || reportLine.getPartnerReport().getPartner() == null) {
            throw new BookingException("Report line invalid for this BookingEvent " + getEventTypeName());
        }
        addBooking(reportLine.getPartnerReport().getPartner(), RegisterType.REPORT_RECEIVABLE, getCreditAmount(reportLine), reportLine);
        addBooking(reportLine.getPartnerReport().getPartner(), RegisterType.REPORT_UNMATCHED, getDebitAmount(reportLine), reportLine);
    }
}

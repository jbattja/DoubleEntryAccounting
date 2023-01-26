package com.battja.accounting.entities;

import java.util.Map;
import java.util.Set;

/**
 * Entity can be used for any structured additional info to pass along
 */
public class AdditionalBookingInfo {

    private Account fundingSource;
    private Set<Transaction> transactions;
    private ReportLine reportLine;
    private PartnerReport partnerReport;
    private Map<Account, Fee> fees;

    public AdditionalBookingInfo() {}

    public Account getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(Account fundingSource) {
        this.fundingSource = fundingSource;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    public ReportLine getReportLine() {
        return reportLine;
    }

    public void setReportLine(ReportLine reportLine) {
        this.reportLine = reportLine;
    }

    public PartnerReport getPartnerReport() {
        return partnerReport;
    }

    public void setPartnerReport(PartnerReport partnerReport) {
        this.partnerReport = partnerReport;
    }

    public Map<Account, Fee> getFees() {
        return fees;
    }

    public void setFees(Map<Account, Fee> fees) {
        this.fees = fees;
    }
}

package com.battja.accounting.entities;

import jakarta.persistence.*;

@Entity
public class ReportLine {

    public enum LineType{CAPTURE,REFUND,CHARGEBACK,COST}

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "partner_report_id")
    private PartnerReport partnerReport;
    @Enumerated(EnumType.STRING)
    private LineType lineType;
    private Long grossAmount;
    private Long netAmount;
    @Enumerated(EnumType.STRING)
    private Amount.Currency currency;
    private String reference;

    public ReportLine(){}

    @Override
    public String toString() {
        return String.format(
                "ReportLine[id=%s, lineType='%s', partnerReport='%s', reference='%s', grossAmount='%s' '%d', netAmount='%s' '%d']",
                id, lineType, partnerReport == null ? "" : partnerReport.getId(), reference, currency, grossAmount,currency, netAmount);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PartnerReport getPartnerReport() {
        return partnerReport;
    }

    public void setPartnerReport(PartnerReport partnerReport) {
        this.partnerReport = partnerReport;
    }

    public LineType getLineType() {
        return lineType;
    }

    public void setLineType(LineType lineType) {
        this.lineType = lineType;
    }

    public Long getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(Long grossAmount) {
        this.grossAmount = grossAmount;
    }

    public Long getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(Long netAmount) {
        this.netAmount = netAmount;
    }

    public Amount.Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Amount.Currency currency) {
        this.currency = currency;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}

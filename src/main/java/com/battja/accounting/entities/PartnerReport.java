package com.battja.accounting.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class PartnerReport {

    public enum ReportStatus{UNPAID,PAID,CLOSED}

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Account partner;
    private Date createdDate;
    private String reference;
    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;
    @OneToMany(mappedBy = "partnerReport")
    private List<ReportLine> reportLines;

    public PartnerReport(){}

    public PartnerReport(Account partner, String reference) {
        this.partner = partner;
        this.reference = reference;
        this.createdDate = new Date();
        this.reportStatus = ReportStatus.UNPAID;
    }

    @Override
    public String toString() {
        return String.format(
                "PartnerReport[id=%s, partner='%s', reference='%s', reportStatus='%s']",
                id, partner == null ? "" : partner.getAccountName(), reference, reportStatus);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getPartner() {
        return partner;
    }

    public void setPartner(Account partner) {
        this.partner = partner;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    public List<ReportLine> getReportLines() {
        return reportLines;
    }

    public void setReportLines(List<ReportLine> reportLines) {
        this.reportLines = reportLines;
    }
}

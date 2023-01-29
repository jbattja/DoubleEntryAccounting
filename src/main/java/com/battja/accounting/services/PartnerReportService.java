package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.repositories.PartnerReportRepository;
import com.battja.accounting.repositories.ReportLineRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;

@Service
public class PartnerReportService {

    private static final Logger log = LoggerFactory.getLogger(PartnerReportService.class);

    public PartnerReport getPartnerReport(@NonNull Integer partnerReportId) {
        return partnerReportRepository.findById(partnerReportId).orElse(null);
    }

    @Transactional
    public PartnerReport createPartnerReport(@NonNull PartnerReport report) {
        if (report.getPartner() == null || !report.getPartner().getAccountType().equals(Account.AccountType.PARTNER)) {
            log.warn("No partner linked to the report: " + report);
        }
        report.setReportStatus(PartnerReport.ReportStatus.UNPAID);
        report = partnerReportRepository.save(report);
        List<Batch> batches = new ArrayList<>();
        try {
            batches.add(batchService.createNewUnavailableBatch(report.getPartner(), RegisterType.REPORT_RECEIVABLE));
            batches.add(batchService.createNewUnavailableBatch(report.getPartner(), RegisterType.REPORT_UNMATCHED));
            if (report.getReportLines() != null) {
                for (ReportLine reportLine : report.getReportLines()) {
                    reportLine.setPartnerReport(report);
                    if (!checkReportLine(reportLine)) {
                        log.warn("Cannot create report, invalid report line: " + reportLine);
                        throw new BookingException("Cannot create report, invalid report line");
                    }
                    reportLineRepository.save(reportLine);
                    bookingService.book(reportLine, EventType.REPORT_RECEIVED, batches);
                }
            }
            log.info("Created report: " + report);
        } catch (BookingException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        }
        return report;
    }

    private boolean checkReportLine(ReportLine reportLine) {
        if (reportLine.getCurrency() == null) {
            return false;
        }
        return reportLine.getLineType() != null;
    }

    public List<PartnerReport> listAll() {
        return partnerReportRepository.findAll();
    }

    @Transactional
    public PartnerReport simulatePartnerReport(@NonNull Batch captureBatch) {
        if (captureBatch.getAccount() == null || !(captureBatch.getAccount().getAccountType().equals(Account.AccountType.PARTNER_ACCOUNT)
                || captureBatch.getAccount().getAccountType().equals(Account.AccountType.PARTNER))) {
            log.warn("Cannot simulate partner report: batch is not a partner account batch, but " + captureBatch.getAccount());
            return null;
        }
        if (!captureBatch.getRegister().equals(RegisterType.CAPTURED)) {
            log.warn("Cannot simulate partner report: batch is not a capture batch, but " + captureBatch.getRegister());
            return null;
        }
        if (captureBatch.getStatus().equals(Batch.BatchStatus.CLOSED)) {
            log.warn("Cannot simulate partner report: capture batch is already closed");
            return null;
        }
        List<BatchEntry> batchEntries = batchService.getEntries(captureBatch.getId());
        if (batchEntries == null || batchEntries.isEmpty()) {
            log.warn("Cannot simulate partner report: no batch entries");
            return null;
        }
        List<BatchEntry> openEntries = new ArrayList<>();
        for (BatchEntry entry : batchEntries) {
            if (entry.getOpenAmount() != 0) {
                openEntries.add(entry);
            }
        }
        if (openEntries.isEmpty()) {
            log.warn("Cannot simulate partner report: no open batch entries");
            return null;
        }
        Account partner = captureBatch.getAccount();
        if (partner.getAccountType().equals(Account.AccountType.PARTNER_ACCOUNT)) {
            if (partner.getParent() != null) {
                partner = partner.getParent();
            } else {
                // should not happen... I think...
                log.warn("Partner account not available for " + partner);
                return null;
            }
        }
        PartnerReport report = new PartnerReport(partner, captureBatch.getDisplayName());
        List<ReportLine> reportLines = new ArrayList<>();
        for (BatchEntry entry : openEntries) {
            if (entry.getTransaction() != null) {
                ReportLine line = new ReportLine();
                line.setCurrency(entry.getCurrency());
                line.setGrossAmount(entry.getOpenAmount() * -1);
                switch (entry.getTransaction().getType()) {
                    case CAPTURE -> line.setLineType(ReportLine.LineType.CAPTURE);
                    case REFUND -> line.setLineType(ReportLine.LineType.REFUND);
                    case CHARGEBACK -> line.setLineType(ReportLine.LineType.CHARGEBACK);
                    default -> {
                        continue;
                    }
                }
                line.setReference(entry.getTransaction().getTransactionReference());
                reportLines.add(line);
            }
        }
        report.setReportLines(reportLines);
        return createPartnerReport(report);
    }


    @Autowired
    PartnerReportRepository partnerReportRepository;
    @Autowired
    ReportLineRepository reportLineRepository;
    @Autowired
    BatchService batchService;
    @Autowired
    BookingService bookingService;

}

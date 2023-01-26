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

    @Transactional
    public PartnerReport createPartnerReport(@NonNull PartnerReport report) {
        if (report.getPartner() == null || !report.getPartner().getAccountType().equals(Account.AccountType.PARTNER)) {
            log.warn("No partner linked to the report: " + report);
        }
        report.setReportStatus(PartnerReport.ReportStatus.UNPAID);
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
            report = partnerReportRepository.save(report);
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

    @Autowired
    PartnerReportRepository partnerReportRepository;
    @Autowired
    ReportLineRepository reportLineRepository;
    @Autowired
    BatchService batchService;
    @Autowired
    BookingService bookingService;

}

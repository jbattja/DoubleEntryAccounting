package com.battja.accounting.services;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.repositories.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PartnerReportService {

    private static final Logger log = LoggerFactory.getLogger(PartnerReportService.class);

    public PartnerReport getPartnerReport(@NonNull Integer partnerReportId) {
        return partnerReportRepository.findById(partnerReportId).orElse(null);
    }

    public ReportLine getReportLine(@NonNull Integer reportLineId) {
        return reportLineRepository.findById(reportLineId).orElse(null);
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
    public Journal matchReportLineToTransaction(@NonNull ReportLine reportLine, @NonNull BatchEntry captureEntry) {
        if (!canMatch(reportLine)) {
            log.warn("cannot match, no unmatched balance: " + reportLine);
            return null;
        }
        List<Booking> reportLineBookings = getBookingsByReportLine(reportLine);
        Booking unmatchedRegisterBooking = null;
        for (Booking b : reportLineBookings) {
            if (b.getRegister().equals(RegisterType.REPORT_UNMATCHED)) {
                unmatchedRegisterBooking = b;
                break;
            }
        }
        if (unmatchedRegisterBooking == null) {
            log.warn("cannot match, no unmatched register found for report line: " + reportLine);
            return null;
        }
        if (!captureEntry.getOpenAmount().equals(unmatchedRegisterBooking.getAmount())) {
            log.warn("cannot match, amounts not equal");
            return null;
        }
        if (!captureEntry.getCurrency().equals(unmatchedRegisterBooking.getCurrency())) {
            log.warn("cannot match, currencies not equal");
            return null;
        }
        AdditionalBookingInfo additionalBookingInfo = new AdditionalBookingInfo();
        Set<Transaction> transactions = new HashSet<>();
        transactions.add(captureEntry.getTransaction());
        additionalBookingInfo.setTransactions(transactions);
        additionalBookingInfo.setReportLine(reportLine);
        Set<Batch> batches = new HashSet<>();
        batches.add(unmatchedRegisterBooking.getBatch());
        batches.add(captureEntry.getBatch());
        return bookingService.book(EventType.REPORT_LINE_MATCHED,additionalBookingInfo,batches);
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
                line.setNetAmount(entry.getOpenAmount() * -1);
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

    private long getRemainingBalance(RegisterType registerType, ReportLine reportLine) {
        List<Booking> bookingsList = bookingRepository.findByReportLine(reportLine);
        long balance = 0L;
        for (Booking b : bookingsList) {
            if (b.getRegister().equals(registerType) && b.getAccount().getId().equals(reportLine.getPartnerReport().getPartner().getId())) {
                balance = balance + b.getAmount();
            }
        }
        return balance;
    }

    public boolean canMatch(@NonNull ReportLine reportLine) {
        return getRemainingBalance(RegisterType.REPORT_UNMATCHED, reportLine) != 0L;
    }

    public Set<Journal> getJournalsByReportLine(ReportLine reportLine) {
        List<Booking> bookings = bookingRepository.findByReportLine(reportLine);
        Set<Journal> journals = new HashSet<>();
        for (Booking entry : bookings) {
            journals.add(entry.getJournal());
        }
        return journals;
    }

    public List<Booking> getBookingsByReportLine(ReportLine reportLine) {
        return bookingRepository.findByReportLine(reportLine);
    }

    public List<BatchEntry> findMatchingTransactions(ReportLine reportLine) {
        List<Transaction> transactions = transactionRepository.findByTransactionReference(reportLine.getReference());
        List<BatchEntry> returnList = new ArrayList<>();
        if (transactions != null && !transactions.isEmpty()) {
            List<BatchEntry> batchEntries = batchEntryRepository.findByTransactionIn(transactions);
            for (BatchEntry entry : batchEntries) {
                if (!entry.getBatch().getAccount().getAccountType().equals(Account.AccountType.PARTNER_ACCOUNT)) {
                    continue;
                }
                returnList.add(entry);
            }
        }
        return returnList;
    }

    @Autowired
    PartnerReportRepository partnerReportRepository;
    @Autowired
    ReportLineRepository reportLineRepository;
    @Autowired
    BatchService batchService;
    @Autowired
    BookingService bookingService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    BatchEntryRepository batchEntryRepository;

}

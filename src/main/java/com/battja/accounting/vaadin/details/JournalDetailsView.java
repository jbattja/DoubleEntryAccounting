package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.ReportLine;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.services.JournalService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.CustomDetailsForm;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="journal-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Journal Details")
public class JournalDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(JournalDetailsView.class);

    private final JournalService journalService;
    private Integer journalId;
    private Journal journal;
    private Set<Transaction> transactions;
    private Set<ReportLine> reportLines;

    public JournalDetailsView(JournalService journalService) {
        this.journalService = journalService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            journalId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        journal = journalService.getJournalWithBookings(journalId);
        if (journal != null) {
            add(new H3("Journal details:"));
            add(createDetailsView());
            Grid<Transaction> transactionGrid = createTransactionsGrid();
            if (!transactions.isEmpty()) {
                add(new H4("Transactions"));
                add(transactionGrid);
            }
            Grid<ReportLine> reportLinesGrid = createReportLinesGrid();
            if (!reportLines.isEmpty()) {
                add(new H4("Report Lines"));
                add(reportLinesGrid);
            }
            if (!journal.getBookings().isEmpty()) {
                add(new H4("Bookings"));
                add(GridCreator.createBookingGrid(journal.getBookings()));
            }
        }
    }

    private CustomDetailsForm createDetailsView() {
        CustomDetailsForm form = new CustomDetailsForm();
        form.addField("Event type", journal.getEventType());
        form.addField("Date", journal.getDate().toString());
        return form;
    }

    private Grid<Transaction> createTransactionsGrid() {
        Grid<Transaction> transactionGrid = new Grid<>(Transaction.class);
        transactionGrid.removeAllColumns();
        transactions = new HashSet<>();
        reportLines = new HashSet<>();
        for (Booking b: journal.getBookings()) {
            if (b.getTransaction() != null) {
                transactions.add(b.getTransaction());
            }
            if (b.getReportLine() != null) {
                reportLines.add(b.getReportLine());
            }
        }
        transactionGrid.setItems(transactions);
        transactionGrid.addColumn(Transaction::getType).setHeader("Type");
        transactionGrid.addColumn(Transaction::getTransactionReference).setHeader("Reference");
        transactionGrid.addItemClickListener(transactionItemClickEvent -> transactionGrid.getUI().ifPresent(
                ui -> ui.navigate(
                        transactionItemClickEvent.getItem().getType().equals(Transaction.TransactionType.PAYMENT) ? PaymentDetailsView.class : ModificationDetailsView.class,
                        String.valueOf(transactionItemClickEvent.getItem().getId()))
        ));
        transactionGrid.setAllRowsVisible(true);
        return transactionGrid;
    }

    private Grid<ReportLine> createReportLinesGrid() {
        Grid<ReportLine> reportLineGrid = new Grid<>(ReportLine.class);
        reportLineGrid.removeAllColumns();
        reportLines = new HashSet<>();
        for (Booking b: journal.getBookings()) {
            if (b.getReportLine() != null) {
                reportLines.add(b.getReportLine());
            }
        }
        reportLineGrid.setItems(reportLines);
        reportLineGrid.addColumn(ReportLine::getLineType).setHeader("Type");
        reportLineGrid.addColumn(ReportLine::getReference).setHeader("Reference");
        reportLineGrid.addItemClickListener(reportLineItemClickEvent -> reportLineGrid.getUI().ifPresent(
                ui -> ui.navigate(
                        ReportLineDetailsView.class,
                        String.valueOf(reportLineItemClickEvent.getItem().getId()))
        ));
        reportLineGrid.setAllRowsVisible(true);
        return reportLineGrid;
    }


}

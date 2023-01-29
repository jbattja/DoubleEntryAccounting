package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.*;
import com.battja.accounting.services.PartnerReportService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.CustomDetailsForm;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="report-line-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Report Line Details")
public class ReportLineDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(ReportLineDetailsView.class);

    private final PartnerReportService partnerReportService;
    private Integer reportLineId;
    private ReportLine reportLine;
    private List<BatchEntry> matchingTransactions;

    public ReportLineDetailsView(PartnerReportService partnerReportService) {
        this.partnerReportService = partnerReportService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            reportLineId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        reportLine = partnerReportService.getReportLine(reportLineId);
        if (reportLine != null) {
            add(new H3("Report Line: " + reportLine.getReference()));
            add(createBackButton());
            add(createDetailsView());
            Set<Journal> journals = partnerReportService.getJournalsByReportLine(reportLine);
            if (!journals.isEmpty()) {
                add(new H4("Journals"));
                add(GridCreator.createJournalGrid(journals));
            }
            if (partnerReportService.canMatch(reportLine)) {
                Button findMatchesButton = new Button("Find Matches");
                findMatchesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                findMatchesButton.addClickListener(buttonClickEvent -> findMatches());
                add(findMatchesButton);
            }
            if (matchingTransactions != null && !matchingTransactions.isEmpty()) {
                add(createMatchingTransactionsGrid());
            }
            Collection<Booking> bookingList = partnerReportService.getBookingsByReportLine(reportLine);
            if (!bookingList.isEmpty()) {
                add(new H4("Bookings"));
                add(GridCreator.createBookingGrid(bookingList));
            }
        }
    }

    private HorizontalLayout createBackButton() {
        HorizontalLayout layout = new HorizontalLayout();
        Button backButton = new Button("Back to report", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(buttonClickEvent -> navigateBackToReport());
        layout.add(backButton);
        return layout;
    }

    private void navigateBackToReport() {
        if (reportLine.getPartnerReport() == null) {
            return;
        }
        getUI().ifPresent(
                ui -> ui.navigate(PartnerReportDetailsView.class,String.valueOf(reportLine.getPartnerReport().getId()))
        );
    }

    private CustomDetailsForm createDetailsView() {
        CustomDetailsForm form = new CustomDetailsForm();
        form.addField("Partner", reportLine.getPartnerReport() != null && reportLine.getPartnerReport().getPartner() != null ?
                reportLine.getPartnerReport().getPartner().getDisplayName() : "");
        form.addField("Reference",reportLine.getReference());
        form.addField("Type",reportLine.getLineType() != null ? reportLine.getLineType().toString() : "");
        String currency = reportLine.getCurrency() != null ? reportLine.getCurrency().toString() : "";
        long grossAmount = reportLine.getGrossAmount() != null ? reportLine.getGrossAmount() : 0;
        long netAmount = reportLine.getNetAmount() != null ? reportLine.getNetAmount() : 0;
        form.addField("Gross amount",currency + " " + grossAmount);
        form.addField("Net Amount",currency + " " + netAmount);
        form.addField("Commission",currency + " " + (grossAmount- netAmount));
        return form;
    }

    private void findMatches() {
        matchingTransactions = partnerReportService.findMatchingTransactions(reportLine);
        updateView();
    }

    private Button matchSelectionButton;
    private Grid<BatchEntry> matchGrid;

    private VerticalLayout createMatchingTransactionsGrid() {
        VerticalLayout layout = new VerticalLayout();
        matchGrid = new Grid<>();
        matchGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        matchGrid.addSelectionListener(selectionEvent -> selectionMade(selectionEvent.getAllSelectedItems()));
        matchGrid.removeAllColumns();
        matchGrid.setItems(matchingTransactions);
        matchGrid.addColumn(new ComponentRenderer<>(batchEntry -> createMatchForm(batchEntry,reportLine))).setHeader("Match");
        matchGrid.setAllRowsVisible(true);
        layout.add(matchGrid);
        matchSelectionButton = new Button("Match");
        matchSelectionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        matchSelectionButton.addClickListener(buttonClickEvent -> doMatch());
        matchSelectionButton.setEnabled(false);
        layout.add(matchSelectionButton);
        return layout;
    }

    private void selectionMade(Set<BatchEntry> selectedMatches) {
        matchSelectionButton.setEnabled(!selectedMatches.isEmpty());
    }

    private static CustomDetailsForm createMatchForm(BatchEntry entry, ReportLine reportLine) {
        Transaction match = entry.getTransaction();
        CustomDetailsForm form = new CustomDetailsForm();
        TextField amountField = form.addField("Amount","" + match.getAmount());
        if (!match.getCurrency().equals(reportLine.getCurrency())) {
            amountField.focus();
        }
        if (!match.getAmount().equals(reportLine.getGrossAmount())) {
            amountField.focus();
        }
        form.addField("Reference",match.getTransactionReference());
        form.addField("Status",match.getStatus());
        TextField typeField = form.addField("Type",match.getType().toString());
        if (!typematch(match.getType(),reportLine.getLineType())) {
            typeField.focus();
        }
        Account partner = match.getPartnerAccount();
        if (partner != null && partner.getAccountType().equals(Account.AccountType.PARTNER_ACCOUNT)) {
            partner = partner.getParent();
        }
        TextField partnerField = form.addField("Partner",partner != null ? partner.getDisplayName() : "");
        if(partner != null && !partner.equals(reportLine.getPartnerReport().getPartner())) {
            partnerField.focus();
        }
        return form;
    }

    private void doMatch() {
        if (matchGrid.getSelectedItems().isEmpty()) {
            Notification notification = new NotificationWithCloseButton("Please select a match", false);
            notification.open();
            return;
        }
        if (matchGrid.getSelectedItems().size() > 1) {
            Notification notification = new NotificationWithCloseButton("Please select only one match", false);
            notification.open();
            return;
        }
        BatchEntry match = matchGrid.getSelectedItems().stream().toList().get(0);
        Journal matchedJournal = partnerReportService.matchReportLineToTransaction(reportLine,match);
        if (matchedJournal == null) {
            Notification notification = new NotificationWithCloseButton("Unable to match line", false);
            notification.open();
        } else {
            Notification notification = new NotificationWithCloseButton("Successfully matched report line", true);
            notification.open();
            matchingTransactions = null;
            updateView();
        }
    }

    private static boolean typematch(Transaction.TransactionType transactionType, ReportLine.LineType lineType) {
        switch (lineType) {
            case REFUND -> {
                return transactionType.equals(Transaction.TransactionType.REFUND);
            }
            case CAPTURE -> {
                return transactionType.equals(Transaction.TransactionType.CAPTURE);
            }
            case CHARGEBACK -> {
                return transactionType.equals(Transaction.TransactionType.CHARGEBACK);
            }
            default -> {
                return false;
            }
        }
    }

}

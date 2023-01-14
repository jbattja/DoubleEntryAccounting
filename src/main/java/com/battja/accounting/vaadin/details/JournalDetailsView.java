package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.services.JournalService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
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
                add(new H4("Trasactions"));
                add(transactionGrid);
            }
            if (!journal.getBookings().isEmpty()) {
                add(new H4("Bookings"));
                add(GridCreator.createBookingGrid(journal.getBookings()));
            }
        }
    }

    private ReadOnlyForm createDetailsView() {
        ReadOnlyForm form = new ReadOnlyForm();
        form.addField("Event type", journal.getEventType());
        form.addField("Date", journal.getDate().toString());
        return form;
    }

    private Grid<Transaction> createTransactionsGrid() {
        Grid<Transaction> transactionGrid = new Grid<>(Transaction.class);
        transactionGrid.removeAllColumns();
        transactions = new HashSet<>();
        for (Booking b: journal.getBookings()) {
            transactions.add(b.getTransaction());
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

}

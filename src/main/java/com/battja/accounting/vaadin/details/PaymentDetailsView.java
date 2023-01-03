package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.journals.Amount;
import com.battja.accounting.services.BookingService;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Route(value="payment-details", layout = MainLayout.class)
@PageTitle("BattjaPay | Payment Details")
public class PaymentDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(PaymentDetailsView.class);

    private final TransactionService transactionService;
    private final BookingService bookingService;
    private Integer paymentId;
    private Transaction payment;
    private IntegerField captureAmount;
    List<Transaction> modifications;

    public PaymentDetailsView(TransactionService transactionService, BookingService bookingService) {
        this.transactionService = transactionService;
        this.bookingService = bookingService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            paymentId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        payment = transactionService.getTransaction(paymentId);
        if (payment != null) {
            add(new H3("Payment Details"));
            add(createDetailsForm());
            add(createButtonsLayout());
            Grid<Transaction> modificationsGrid = createModificationGrid();
            if (!modifications.isEmpty()) {
                add(new H4("Capture / Refunds"));
                add(modificationsGrid);
            }
            add(new H4("Journals"));
            add(createJournalsGrid());
            add(new H4("Bookings"));
            add(createBookingGrid());
        }
    }

    private ReadOnlyForm createDetailsForm() {
        ReadOnlyForm detailsForm = new ReadOnlyForm();
        detailsForm.addField("Merchant", payment.getType().toString());
        detailsForm.addField("Reference", payment.getTransactionReference());
        detailsForm.addField("Amount", payment.getCurrency() + " " + payment.getAmount());
        detailsForm.addField("Status", transactionService.getLatestJournal(payment));
        detailsForm.addField("Merchant", payment.getMerchantAccount().getAccountName());
        detailsForm.addField("Acquirer account", payment.getAcquirerAccount().getAccountName());
        return detailsForm;
    }

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        Button authorizeButton = new Button("Authorize");
        authorizeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        authorizeButton.addClickListener(buttonClickEvent -> authorizePayment());
        if (!transactionService.canTransactionAuthorize(payment)) {
            authorizeButton.setEnabled(false);
        }
        buttonsLayout.add(authorizeButton);

        Div captureDiv = new Div();
        Button captureButton = new Button("Capture");
        captureButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        captureButton.addClickListener(buttonClickEvent -> capturePayment());
        boolean canCapture = transactionService.canTransactionCapture(payment);
        if (!canCapture) {
            captureButton.setEnabled(false);
        }
        captureDiv.add(captureButton);
        if (canCapture) {
            captureAmount = new IntegerField();
            captureAmount.setPlaceholder("Amount");
            captureDiv.add(captureAmount);
        }
        buttonsLayout.add(captureDiv);
        return buttonsLayout;
    }

    private void authorizePayment() {
        bookingService.book(payment, EventType.AUTHORISED);
        updateView();
    }

    private void capturePayment() {
        captureAmount.setHelperComponent(null);
        if(captureAmount.getValue()  == null) {
            captureAmount.setHelperComponent(new Span("Field is required"));
            captureAmount.focus();
            return;
        }
        if(captureAmount.getValue() <= 0) {
            captureAmount.setHelperComponent(new Span("Amount needs to be more than 0"));
            captureAmount.focus();
            return;
        }
        try {
            transactionService.newCapture(payment, new Amount(payment.getCurrency(), captureAmount.getValue()));
        } catch (BookingException e) {
            log.warn("Capture failed: " + e.getMessage());
        }
        updateView();
    }

    private Grid<Transaction> createModificationGrid() {
        Grid<Transaction> modificationGrid = new Grid<>(Transaction.class);
        modifications = transactionService.listAllModifications(payment);
        if (modifications.isEmpty()) {
            modificationGrid.setVisible(false);
            return modificationGrid;
        }
        modificationGrid.setItems(modifications);
        modificationGrid.removeAllColumns();
        modificationGrid.addColumn(transaction -> transaction.getType().toString()).setHeader("Type");
        modificationGrid.addColumn(Transaction::getTransactionReference).setHeader("Reference");
        modificationGrid.addColumn(transactionService::getLatestJournal).setHeader("Status");
        modificationGrid.addColumn(Transaction::getCurrency).setHeader("Currency");
        modificationGrid.addColumn(Transaction::getAmount).setHeader("Amount");
        modificationGrid.addColumn(transaction -> transaction.getAcquirerAccount().getAccountName()).setHeader("Acquirer Account");
        modificationGrid.setAllRowsVisible(true);
        return modificationGrid;
    }

    private Grid<Journal> createJournalsGrid() {
        Grid<Journal> journalGrid = new Grid<>(Journal.class);
        journalGrid.removeAllColumns();
        journalGrid.setItems(transactionService.getJournalsByTransaction(payment));
        journalGrid.addColumn(Journal::getDate).setHeader("Date");
        journalGrid.addColumn(Journal::getEventType).setHeader("Event");
        journalGrid.setAllRowsVisible(true);
        return journalGrid;
    }

    private Grid<Booking> createBookingGrid() {
        Grid<Booking> bookingGrid = new Grid<>(Booking.class);
        bookingGrid.removeAllColumns();
        bookingGrid.setItems(transactionService.getBookingsByTransaction(payment));
        bookingGrid.addColumn(booking -> booking.getAccount().getAccountName()).setHeader("Account");
        bookingGrid.addColumn(Booking::getRegister).setHeader("Register");
        bookingGrid.addColumn(Booking::getCurrency).setHeader("Currency");
        bookingGrid.addColumn(booking -> booking.getAmount() < 0 ? booking.getAmount() * -1 : "").setHeader("Debit Amount");
        bookingGrid.addColumn(booking -> booking.getAmount() >= 0 ? booking.getAmount() : "").setHeader("Credit Amount");
        bookingGrid.addColumn(booking -> booking.getBatch().getBatchNumber()).setHeader("BatchNumber");
        bookingGrid.setAllRowsVisible(true);
        return bookingGrid;
    }

}

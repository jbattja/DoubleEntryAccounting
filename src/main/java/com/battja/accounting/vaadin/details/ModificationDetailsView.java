package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value="modification-details", layout = MainLayout.class)
@PageTitle("BattjaPay | Modification Details")
public class ModificationDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(ModificationDetailsView.class);

    private final TransactionService transactionService;
    private Integer modificationId;
    private Transaction modification;


    public ModificationDetailsView(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            modificationId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        modification = transactionService.getTransaction(modificationId);
        if (modification != null) {
            add(new H3("Modification Details"));
            add(createBackButton());
            add(createDetailsForm());
            add(createButtonsLayout());
            add(new H4("Journals"));
            add(createJournalsGrid());
            add(new H4("Bookings"));
            add(createBookingGrid());
        }
    }

    private HorizontalLayout createBackButton() {
        HorizontalLayout layout = new HorizontalLayout();
        Button backButton = new Button("Back to payment", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(buttonClickEvent -> navigateBackToPayment());
        layout.add(backButton);
        return layout;
    }

    private void navigateBackToPayment() {
        Transaction payment = transactionService.getByReference(modification.getOriginalReference());
        if (payment == null) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to go back to payment", false);
            notification.open();
            return;
        }
        getUI().ifPresent(
                ui -> ui.navigate(PaymentDetailsView.class,String.valueOf(payment.getId()))
        );
    }

    private ReadOnlyForm createDetailsForm() {
        ReadOnlyForm detailsForm = new ReadOnlyForm();
        detailsForm.addField("Type", modification.getType().toString());
        detailsForm.addField("Reference", modification.getOriginalReference());
        detailsForm.addField("Payment Reference", modification.getTransactionReference());
        detailsForm.addField("Amount", modification.getCurrency() + " " + modification.getAmount());
        detailsForm.addField("Status", modification.getStatus());
        detailsForm.addField("Merchant", modification.getMerchantAccount().getAccountName());
        detailsForm.addField("Acquirer account", modification.getAcquirerAccount().getAccountName());
        return detailsForm;
    }

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        Button captureFailButton = new Button("Book Settlement Failed");
        captureFailButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        captureFailButton.addClickListener(buttonClickEvent -> failCapture());
        if (!transactionService.isCaptureStillOpen(modification)) {
            captureFailButton.setEnabled(false);
        }
        buttonsLayout.add(captureFailButton);
        return buttonsLayout;
    }

    private void failCapture() {
        try {
            transactionService.bookSettlementFailed(modification);
        } catch (BookingException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(), false);
            notification.open();
            return;
        }
        NotificationWithCloseButton notification = new NotificationWithCloseButton("Booked Settlement Failed", true);
        notification.open();
        updateView();
    }

    private Grid<Journal> createJournalsGrid() {
        Grid<Journal> journalGrid = new Grid<>(Journal.class);
        journalGrid.removeAllColumns();
        journalGrid.setItems(transactionService.getJournalsByTransaction(modification));
        journalGrid.addColumn(Journal::getDate).setHeader("Date");
        journalGrid.addColumn(Journal::getEventType).setHeader("Event");
        journalGrid.setAllRowsVisible(true);
        return journalGrid;
    }

    private Grid<Booking> createBookingGrid() {
        Grid<Booking> bookingGrid = new Grid<>(Booking.class);
        bookingGrid.removeAllColumns();
        bookingGrid.setItems(transactionService.getBookingsByTransaction(modification));
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

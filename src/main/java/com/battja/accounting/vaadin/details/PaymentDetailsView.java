package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Amount;
import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.CustomDetailsForm;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="payment-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Payment Details")
public class PaymentDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(PaymentDetailsView.class);

    private final TransactionService transactionService;
    private Integer paymentId;
    private Transaction payment;
    private IntegerField captureAmount;
    List<Transaction> modifications;

    public PaymentDetailsView(TransactionService transactionService) {
        this.transactionService = transactionService;
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
            Set<Journal> journals = transactionService.getJournalsByTransaction(payment);
            if (!journals.isEmpty()) {
                add(new H4("Journals"));
                add(GridCreator.createJournalGrid(journals));
            }
            List<Transaction> paymentAndModifications = new ArrayList<>(modifications);
            paymentAndModifications.add(payment);
            List<Booking> bookings = transactionService.getBookingsByTransactions(paymentAndModifications);
            if (!bookings.isEmpty()) {
                add(new H4("Bookings"));
                add(GridCreator.createBookingGrid(bookings));
            }
        }
    }

    private CustomDetailsForm createDetailsForm() {
        CustomDetailsForm detailsForm = new CustomDetailsForm();
        detailsForm.addField("Reference", payment.getTransactionReference());
        detailsForm.addField("Amount", payment.getCurrency() + " " + payment.getAmount());
        detailsForm.addField("Payment Method", payment.getPaymentMethod().toString());
        detailsForm.addField("Status", payment.getStatus());
        detailsForm.addClickableField("Merchant", payment.getMerchantAccount().getAccountName(),
                AccountDetailsView.class,String.valueOf(payment.getMerchantAccount().getId()));
        detailsForm.addClickableField("Partner account", payment.getPartnerAccount().getAccountName(),
                AccountDetailsView.class,String.valueOf(payment.getPartnerAccount().getId()));
        return detailsForm;
    }

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        Button authorizeButton = new Button("Authorize");
        authorizeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        authorizeButton.addClickListener(buttonClickEvent -> authorizePayment(true));
        Button rejectButton = new Button("Refuse");
        rejectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        rejectButton.addClickListener(buttonClickEvent -> authorizePayment(false));
        if (!transactionService.canTransactionAuthorize(payment)) {
            authorizeButton.setEnabled(false);
            rejectButton.setEnabled(false);
        }
        buttonsLayout.add(authorizeButton);
        buttonsLayout.add(rejectButton);

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
        Button cancelButton = new Button("Cancel Remaining Amount");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addClickListener(buttonClickEvent -> cancelPayment());
        if (!transactionService.canTransactionCapture(payment)) {
            cancelButton.setEnabled(false);
        }
        buttonsLayout.add(cancelButton);

        return buttonsLayout;
    }
    private void authorizePayment(boolean authoriseSuccess) {
        try {
            transactionService.authorizeOrRefusePayment(payment, authoriseSuccess);
        } catch (BookingException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(), false);
            notification.open();
            return;
        }
        NotificationWithCloseButton notification = new NotificationWithCloseButton(authoriseSuccess ? "Payment authorized" : "Payment refused", true);
        notification.open();
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
            Transaction capture = transactionService.newCapture(payment, new Amount(payment.getCurrency(), captureAmount.getValue()));
            NotificationWithCloseButton notification;
            if (capture == null) {
                notification = new NotificationWithCloseButton("Failed to capture payment", false);
            } else {
                notification = new NotificationWithCloseButton("Successfully captured payment", true);
            }
            notification.open();
        } catch (BookingException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(),false);
            notification.open();
        }
        updateView();
    }

    private void cancelPayment() {
        try {
            transactionService.cancelRemainingAmount(payment);
        } catch (BookingException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(), false);
            notification.open();
            return;
        }
        NotificationWithCloseButton notification = new NotificationWithCloseButton("Payment cancelled", true);
        notification.open();
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
        modificationGrid.addColumn(Transaction::getStatus).setHeader("Status");
        modificationGrid.addColumn(Transaction::getCurrency).setHeader("Currency");
        modificationGrid.addColumn(Transaction::getAmount).setHeader("Amount");
        modificationGrid.addColumn(transaction -> transaction.getPartnerAccount().getAccountName()).setHeader("Partner Account");
        modificationGrid.setAllRowsVisible(true);
        modificationGrid.addItemClickListener(itemClickEvent -> modificationGrid.getUI().ifPresent(
                ui -> ui.navigate(ModificationDetailsView.class,String.valueOf(itemClickEvent.getItem().getId()))
        ));
        return modificationGrid;
    }

}

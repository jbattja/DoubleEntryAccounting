package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.setup.SetupService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.entryforms.CreatePaymentForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Collection;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="payments", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Payments")
public class PaymentsList extends VerticalLayout {

    private final TransactionService transactionService;
    private final SetupService setupService;

    public PaymentsList(TransactionService transactionService,SetupService setupService) {
        this.transactionService = transactionService;
        this.setupService = setupService;
        updateView();
    }

    private void updateView() {
        removeAll();
        add(new H4("Payments"));
        Collection<Transaction> payments = transactionService.listAllPayments();
        add(GridCreator.createPaymentsGrid(payments));
        add(createButtonsLayout());
    }

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        Button createPaymentButton = new Button("Create Payment");
        createPaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPaymentButton.addClickListener(buttonClickEvent -> createPaymentButton.getUI().ifPresent(ui -> ui.navigate(CreatePaymentForm.class)));
        layout.add(createPaymentButton);

        if (setupService.demoDataEnabled()) {
            Div simulatePaymentsDiv = new Div();
            Button simulatePaymentButton = new Button("Create demo payments");
            simulatePaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            simulatePaymentButton.addClickListener(buttonClickEvent -> simulatePayments());
            simulatePaymentsDiv.add(simulatePaymentButton);
            simulatePaymentAmount = new IntegerField();
            simulatePaymentAmount.setPlaceholder("How many");
            simulatePaymentsDiv.add(simulatePaymentAmount);
            layout.add(simulatePaymentsDiv);
        }
        return layout;
    }

    private IntegerField simulatePaymentAmount;

    private void simulatePayments() {
        simulatePaymentAmount.setHelperComponent(null);
        if(simulatePaymentAmount.getValue()  == null) {
            simulatePaymentAmount.setHelperComponent(new Span("Field is required"));
            simulatePaymentAmount.focus();
            return;
        }
        if(simulatePaymentAmount.getValue() > 100) {
            simulatePaymentAmount.setHelperComponent(new Span("Maximum is 100 per click"));
            simulatePaymentAmount.focus();
            return;
        }
        int amount = simulatePaymentAmount.getValue();
        simulatePaymentAmount.setValue(null);
        try {
            setupService.createDemoTransactions(amount);
        } catch (BookingException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(
                    "Error while creating demo transactions: " + e.getMessage(),false);
            notification.open();
            return;
        }
        NotificationWithCloseButton notification = new NotificationWithCloseButton(
                "Successfully created " + amount + " payments",true);
        notification.open();
        updateView();
    }
}

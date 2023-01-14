package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.entryforms.CreatePaymentForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Collection;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="payments", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Payments")
public class PaymentsList extends VerticalLayout {

    private final TransactionService transactionService;

    public PaymentsList(TransactionService transactionService) {
        this.transactionService = transactionService;
        updateView();
    }

    private void updateView() {
        add(new H4("Payments"));
        Collection<Transaction> payments = transactionService.listAllPayments();
        add(GridCreator.createPaymentsGrid(payments));
        Button createPaymentButton = new Button("Create Payment");
        createPaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPaymentButton.addClickListener(buttonClickEvent -> createPaymentButton.getUI().ifPresent(ui -> ui.navigate(CreatePaymentForm.class)));
        add(createPaymentButton);
    }

}

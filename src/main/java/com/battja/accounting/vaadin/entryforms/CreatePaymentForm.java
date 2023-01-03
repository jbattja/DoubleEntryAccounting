package com.battja.accounting.vaadin.entryforms;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.journals.Amount;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.details.PaymentDetailsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="create-payment", layout = MainLayout.class)
@PageTitle("BattjaPay | Create Payment")
public class CreatePaymentForm extends VerticalLayout {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private Select<Account> merchant;
    private Select<Account> acquirerAccount;
    private Select<String> currency;
    private IntegerField amount;

    public CreatePaymentForm(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        updateView();
    }

    private void updateView() {
        add(createInputForm());

        Button createPaymentButton = new Button("Create");
        createPaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPaymentButton.addClickListener(buttonClickEvent -> createPayment());
        add(createPaymentButton);
    }

    private FormLayout createInputForm() {
        FormLayout layout = new FormLayout();

        merchant = new Select<>();
        merchant.setItems(accountService.listMerchants());
        merchant.setRenderer(new TextRenderer<>(Account::getAccountName));
        layout.add(merchant);

        acquirerAccount = new Select<>();
        acquirerAccount.setItems(accountService.listAcquirerAccounts());
        acquirerAccount.setRenderer(new TextRenderer<>(Account::getAccountName));
        layout.add(acquirerAccount);

        currency = new Select<>();
        currency.setLabel("Currency");
        currency.setItems("IDR","PHP","SGD","USD");
        currency.setValue("IDR");
        layout.add(currency);

        amount = new IntegerField();
        amount.setLabel("Amount");
        layout.add(amount);
        return layout;
    }

    private void createPayment() {
        merchant.setHelperComponent(null);
        acquirerAccount.setHelperComponent(null);
        currency.setHelperComponent(null);
        amount.setHelperComponent(null);
        if(merchant.getValue() == null) {
            merchant.setHelperComponent(new Span("Field is required"));
            merchant.focus();
            return;
        }
        if(acquirerAccount.getValue()  == null) {
            acquirerAccount.setHelperComponent(new Span("Field is required"));
            merchant.focus();
            return;
        }
        if(currency.getValue()  == null) {
            currency.setHelperComponent(new Span("Field is required"));
            merchant.focus();
            return;
        }
        if(amount.getValue()  == null) {
            amount.setHelperComponent(new Span("Field is required"));
            merchant.focus();
            return;
        }
        if(amount.getValue() <= 0) {
            amount.setHelperComponent(new Span("Amount needs to be more than 0"));
            merchant.focus();
            return;
        }
        Transaction transaction = transactionService.newPayment(new Amount(currency.getValue(),amount.getValue()),merchant.getValue(),acquirerAccount.getValue());
        if (transaction != null) {
            getUI().ifPresent(ui -> ui.navigate(PaymentDetailsView.class,String.valueOf(transaction.getId())));
        }
    }
}

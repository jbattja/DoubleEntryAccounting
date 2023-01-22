package com.battja.accounting.vaadin.entryforms;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.PaymentMethod;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.entities.Amount;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
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

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="create-payment", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Create Payment")
public class CreatePaymentForm extends VerticalLayout {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private Select<Account> merchant;
    private Select<Account> partnerAccount;
    private Select<PaymentMethod> paymentMethod;
    private Select<Amount.Currency> currency;
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
        merchant.setLabel("Merchant Account");
        merchant.setItems(accountService.listAccountsByType(Account.AccountType.MERCHANT));
        merchant.setRenderer(new TextRenderer<>(Account::getAccountName));
        layout.add(merchant);

        partnerAccount = new Select<>();
        partnerAccount.setLabel("Partner Account");
        partnerAccount.setItems(accountService.listAccountsByType(Account.AccountType.PARTNER_ACCOUNT));
        partnerAccount.setRenderer(new TextRenderer<>(Account::getAccountName));
        layout.add(partnerAccount);

        paymentMethod = new Select<>();
        paymentMethod.setLabel("Payment Method");
        paymentMethod.setItems(PaymentMethod.values());
        layout.add(paymentMethod);

        currency = new Select<>();
        currency.setLabel("Currency");
        currency.setItems(Amount.Currency.values());
        layout.add(currency);

        amount = new IntegerField();
        amount.setLabel("Amount");
        layout.add(amount);
        return layout;
    }

    private void createPayment() {
        merchant.setHelperComponent(null);
        partnerAccount.setHelperComponent(null);
        currency.setHelperComponent(null);
        amount.setHelperComponent(null);
        if(merchant.getValue() == null) {
            merchant.setHelperComponent(new Span("Field is required"));
            merchant.focus();
            return;
        }
        if(partnerAccount.getValue()  == null) {
            partnerAccount.setHelperComponent(new Span("Field is required"));
            partnerAccount.focus();
            return;
        }
        if(paymentMethod.getValue()  == null) {
            paymentMethod.setHelperComponent(new Span("Field is required"));
            paymentMethod.focus();
            return;
        }
        if(currency.getValue()  == null) {
            currency.setHelperComponent(new Span("Field is required"));
            currency.focus();
            return;
        }
        if(amount.getValue()  == null) {
            amount.setHelperComponent(new Span("Field is required"));
            amount.focus();
            return;
        }
        if(amount.getValue() <= 0) {
            amount.setHelperComponent(new Span("Amount needs to be more than 0"));
            amount.focus();
            return;
        }
        try {
            Transaction transaction = transactionService.newPayment(new Amount(currency.getValue(), amount.getValue()),paymentMethod.getValue(), merchant.getValue(), partnerAccount.getValue());
            if (transaction != null) {
                getUI().ifPresent(ui -> ui.navigate(PaymentDetailsView.class, String.valueOf(transaction.getId())));
            }
        } catch (IllegalArgumentException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(),false);
            notification.open();
        }
    }
}

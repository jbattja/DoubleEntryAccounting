package com.battja.accounting.vaadin.entryforms;

import com.battja.accounting.entities.Account;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.details.AccountDetailsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="create-account", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Create Account")
public class CreateAccountForm extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(CreateAccountForm.class);

    private final AccountService accountService;
    private TextField accountName;
    private Select<Account.AccountType> type;
    private ComboBox<Account> parent;
    private Integer parentId;

    public CreateAccountForm(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            parentId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        add(createInputForm());
        Button createAccountButton = new Button("Create");
        createAccountButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createAccountButton.addClickListener(buttonClickEvent -> createAccount());
        add(createAccountButton);
    }

    private FormLayout createInputForm() {
        Account parentAccount = null;
        if (parentId != null && parentId != 0) {
            parentAccount = accountService.getAccount(parentId);
        }
        FormLayout layout = new FormLayout();

        accountName = new TextField();
        accountName.setLabel("Account Name");
        layout.add(accountName);

        type = new Select<>();
        type.setLabel("Account Type");
        type.setItems(Account.AccountType.values());
        if (parentAccount != null) {
            if (parentAccount.getAccountType().equals(Account.AccountType.COMPANY)) {
                type.setValue(Account.AccountType.MERCHANT);
            }
            if (parentAccount.getAccountType().equals(Account.AccountType.PARTNER)) {
                type.setValue(Account.AccountType.PARTNER_ACCOUNT);
            }
        }
        layout.add(type);

        parent = new ComboBox<>();
        parent.setLabel("Parent");
        parent.setItems(getAvailableAccounts());
        parent.setRenderer(new TextRenderer<>(Account::getAccountName));
        parent.setItemLabelGenerator(Account::getAccountName);
        if (parentAccount != null) {
            parent.setValue(parentAccount);
        }
        layout.add(parent);
        return layout;
    }

    private Collection<Account> getAvailableAccounts() {
        return accountService.listAvailableParentAccounts(type.getValue());
    }

    private void createAccount() {
        accountName.setHelperComponent(null);
        type.setHelperComponent(null);
        parent.setHelperComponent(null);
        if (accountName.getValue() == null || accountName.isEmpty()) {
            accountName.setHelperComponent(new Span("Field is required"));
            accountName.focus();
            return;
        }
        if (type.getValue() == null ) {
            type.setHelperComponent(new Span("Field is required"));
            type.focus();
            return;
        }
        if (parent.getValue() == null ) {
            parent.setHelperComponent(new Span("Field is required"));
            parent.focus();
            return;
        }
        if (!parent.getValue().getAccountType().equals(type.getValue().getParent())) {
            type.setHelperComponent(new Span("Incorrect type for this parent"));
            type.focus();
            return;
        }
        Account account = new Account(accountName.getValue(),type.getValue(),parent.getValue());
        try {
            Account finalAccount = accountService.createAccount(account);
            if (finalAccount != null) {
                getUI().ifPresent(ui -> ui.navigate(AccountDetailsView.class, String.valueOf(finalAccount.getId())));
            }
        } catch (DuplicateNameException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(),false);
            notification.open();
        }
    }
}
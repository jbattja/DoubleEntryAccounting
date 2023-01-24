package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Contract;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.FeeService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.components.CustomDetailsForm;
import com.battja.accounting.vaadin.entryforms.CreateAccountForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="account-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Account Details")
public class AccountDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(AccountDetailsView.class);

    private final AccountService accountService;
    private final FeeService feeService;
    private Integer accountId;
    private Account account;

    public AccountDetailsView(AccountService accountService, FeeService feeService) {
        this.accountService = accountService;
        this.feeService = feeService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            accountId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        account = accountService.getAccount(accountId);
        if (account != null) {
            add(new H3("Account: " + account.getAccountName()));
            add(createDetailsView());
            add(createButtonsLayout());
            Collection<Account> children = accountService.listChildren(account);
            if (!children.isEmpty()) {
                add(new H4("Child Accounts"));
                add(GridCreator.createAccountGrid(children));
            }
            if (Account.AccountType.canHaveChildren(account.getAccountType())) {
                Button createAccountButton = new Button("Create Child Account");
                createAccountButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                createAccountButton.addClickListener(buttonClickEvent -> createAccountButton.getUI().ifPresent(
                        ui -> ui.navigate(CreateAccountForm.class,String.valueOf(account.getId()))));
                add(createAccountButton);
            }
        }
    }

    private CustomDetailsForm form;
    private Collection<Contract> contracts;
    TextField accountNameField;
    Select<Contract> contractSelect;

    private CustomDetailsForm createDetailsView() {
        form = new CustomDetailsForm();
        accountNameField = form.addEditableField("Name", account.getAccountName());
        form.addField("Type", account.getAccountType().toString());
        if (account.getParent() != null) {
            form.addClickableField("Parent", account.getParent().getAccountName(),AccountDetailsView.class,String.valueOf(account.getParent().getId()));
        }
        if(contracts == null || contracts.isEmpty()) {
            contracts = feeService.listAllContracts();
        }
        contractSelect = form.addSelectableField("Contract", account.getContract(), contracts);
        return form;
    }

    private Button editButton;
    private Button saveButton;
    private Button cancelButton;

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        editButton = new Button("Edit");
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(buttonClickEvent -> edit());
        buttonsLayout.add(editButton);
        cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(buttonClickEvent -> cancel());
        cancelButton.setVisible(false);
        buttonsLayout.add(cancelButton);
        saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(buttonClickEvent -> save());
        saveButton.setVisible(false);
        buttonsLayout.add(saveButton);
        return buttonsLayout;
    }

    private void edit() {
        form.setEditable(true);
        editButton.setVisible(false);
        cancelButton.setVisible(true);
        saveButton.setVisible(true);
    }

    private void cancel() {
        form.setEditable(false);
        editButton.setVisible(true);
        cancelButton.setVisible(false);
        saveButton.setVisible(false);
    }

    private void save() {
        account.setAccountName(accountNameField.getValue());
        account.setContract(contractSelect.getValue());
        try {
            Account updatedAccount = accountService.updateAccount(account);
            if (updatedAccount == null) {
                NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to update account",false);
                notification.open();
            } else {
                NotificationWithCloseButton notification = new NotificationWithCloseButton("Account updated",true);
                notification.open();
                updateView();
            }
        } catch (DuplicateNameException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(),false);
            notification.open();
        }
    }

}

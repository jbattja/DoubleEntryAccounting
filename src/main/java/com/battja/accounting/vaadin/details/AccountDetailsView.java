package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Account;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
    private Integer accountId;
    private Account account;

    public AccountDetailsView(AccountService accountService) {
        this.accountService = accountService;
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
            Collection<Account> children = accountService.listChildren(account);
            if (!children.isEmpty()) {
                add(new H4("Child Accounts"));
                add(GridCreator.createAccountGrid(children));
            }
        }
    }

    private ReadOnlyForm createDetailsView() {
        ReadOnlyForm form = new ReadOnlyForm();
        form.addField("Name", account.getAccountName());
        form.addField("Type", account.getAccountType().toString());
        if (account.getParent() != null) {
            form.addClickableField("Parent", account.getParent().getAccountName(),AccountDetailsView.class,String.valueOf(account.getParent().getId()));
        }
        return form;
    }

}

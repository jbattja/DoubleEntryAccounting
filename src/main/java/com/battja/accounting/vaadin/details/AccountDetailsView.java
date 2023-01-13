package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Account;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value="account-details", layout = MainLayout.class)
@PageTitle("BattjaPay | Account Details")
public class AccountDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(AccountDetailsView.class);

    private final AccountService accountService;
    private Integer accountId;

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
        Account account = accountService.getAccount(accountId);
        if (account != null) {
            add(new H3("Account: " + account.getAccountName()));

            ReadOnlyForm form = new ReadOnlyForm();
            form.addField("Name", account.getAccountName());
            form.addField("Type", account.getAccountType().toString());
            if (account.getParentId() != null) {
                Account parent = accountService.getParent(account);
                if (parent != null) {
                    form.addField("Parent",parent.getAccountName());
                }
            }
            add(form);
        }
    }

}

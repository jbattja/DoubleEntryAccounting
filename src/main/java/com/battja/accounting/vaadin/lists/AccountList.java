package com.battja.accounting.vaadin.lists;

import com.battja.accounting.services.AccountService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="accounts", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Accounts")
public class AccountList extends VerticalLayout {

    private final AccountService accountService;

    public AccountList(AccountService accountService) {
        this.accountService = accountService;
        add(new H4("Accounts"));
        add(GridCreator.createAccountGrid(accountService.listAll()));
    }

}
package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Account;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.vaadin.components.FilterHeader;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.details.AccountDetailsView;
import com.battja.accounting.vaadin.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="accounts", layout = MainLayout.class)
@PageTitle("BattjaPay | Accounts")
public class AccountList extends VerticalLayout {

    private final AccountService accountService;

    public AccountList(AccountService accountService) {
        this.accountService = accountService;
        add(new H4("Accounts"));
        add(GridCreator.createAccountGrid(accountService.listAll()));
    }

}
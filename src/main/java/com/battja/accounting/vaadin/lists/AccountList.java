package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Account;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.vaadin.components.FilterHeader;
import com.battja.accounting.vaadin.details.AccountDetailsView;
import com.battja.accounting.vaadin.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="accounts", layout = MainLayout.class)
@PageTitle("BattjaPay | Accounts")
public class AccountList extends VerticalLayout {

    private final AccountService accountService;
    final Grid<Account> grid;

    public AccountList(AccountService accountService) {
        this.accountService = accountService;
        this.grid = new Grid<>(Account.class);
        add(grid);
        listAccounts();
    }

    private void listAccounts(){
        GridListDataView<Account> dataView = grid.setItems(accountService.listAll());
        grid.removeAllColumns();
        Grid.Column<Account> accountNameColumn = grid.addColumn(Account::getAccountName).setHeader("Account");
        Grid.Column<Account> accountTypeColumn = grid.addColumn(account -> account.getAccountType().toString()).setHeader("Type");
        grid.addColumn(this::getParentAccountName).setHeader("Parent");
        grid.addItemClickListener(accountItemClickEvent -> grid.getUI().ifPresent(
                ui -> ui.navigate(AccountDetailsView.class,String.valueOf(accountItemClickEvent.getItem().getId()))));

        AccountFilter accountFilter = new AccountFilter(dataView);
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        headerRow.getCell(accountNameColumn).setComponent(new FilterHeader(accountFilter::setAccountName));
        headerRow.getCell(accountTypeColumn).setComponent(new FilterHeader(accountFilter::setAccountType));
    }

    private String getParentAccountName(Account account) {
        if (account.getParent() != null) {
            return account.getParent().getAccountName();
        }
        return "";
    }

    public static class AccountFilter {
        private final GridListDataView<Account> dataView;

        private String accountName;
        private String accountType;

        public AccountFilter(GridListDataView<Account> dataView) {
            this.dataView = dataView;
            this.dataView.setFilter(this::test);
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
            this.dataView.refreshAll();
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
            this.dataView.refreshAll();
        }

        public boolean test(Account account) {
            return (
                    matches(account.getAccountName(), accountName)
                    && matches(account.getAccountType().toString(),accountType)
                    );
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }
}
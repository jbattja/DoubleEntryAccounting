package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Account;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.FilterHeader;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
import com.battja.accounting.vaadin.lists.AccountList;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Route(value="account-details", layout = MainLayout.class)
@PageTitle("BattjaPay | Account Details")
public class AccountDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(AccountDetailsView.class);

    private final AccountService accountService;
    private Integer accountId;
    private Account account;
    private List<Account> children;


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
            Grid<Account> childrenList = createChildrenList();
            if (!children.isEmpty()) {
                add(new H4("Child Accounts"));
                add(childrenList);
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

    private Grid<Account> createChildrenList() {
        Grid<Account> childrenList = new Grid<>(Account.class);
        children = accountService.listChildren(account);
        if (children.isEmpty()) {
            childrenList.setVisible(false);
            return childrenList;
        }
        GridListDataView<Account> dataView = childrenList.setItems(children);
        childrenList.removeAllColumns();
        Grid.Column<Account> accountNameColumn = childrenList.addColumn(Account::getAccountName).setHeader("Account");
        Grid.Column<Account> accountTypeColumn = childrenList.addColumn(account -> account.getAccountType().toString()).setHeader("Type");
        childrenList.addItemClickListener(accountItemClickEvent -> childrenList.getUI().ifPresent(
                ui -> ui.navigate(AccountDetailsView.class,String.valueOf(accountItemClickEvent.getItem().getId()))));

        AccountList.AccountFilter accountFilter = new AccountList.AccountFilter(dataView);
        childrenList.getHeaderRows().clear();
        HeaderRow headerRow = childrenList.appendHeaderRow();
        headerRow.getCell(accountNameColumn).setComponent(new FilterHeader(accountFilter::setAccountName));
        headerRow.getCell(accountTypeColumn).setComponent(new FilterHeader(accountFilter::setAccountType));
        return childrenList;
    }

}

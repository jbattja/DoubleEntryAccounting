package com.battja.accounting.vaadin.components;

import com.battja.accounting.entities.*;
import com.battja.accounting.vaadin.details.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;

import java.util.Collection;

public class GridCreator {

    public static Grid<Booking> createBookingGrid(Collection<Booking> bookings) {
        Grid<Booking> bookingGrid = new Grid<>(Booking.class);
        bookingGrid.removeAllColumns();
        bookingGrid.setItems(bookings);
        bookingGrid.addColumn(booking -> booking.getAccount().getAccountName()).setHeader("Account");
        bookingGrid.addColumn(Booking::getRegister).setHeader("Register");
        bookingGrid.addColumn(Booking::getCurrency).setHeader("Currency");
        bookingGrid.addColumn(booking -> booking.getAmount() < 0 ? booking.getAmount() * -1 : "").setHeader("Debit Amount");
        bookingGrid.addColumn(booking -> booking.getAmount() >= 0 ? booking.getAmount() : "").setHeader("Credit Amount");
        bookingGrid.addColumn(booking -> booking.getBatch().getBatchNumber()).setHeader("BatchNumber");
        bookingGrid.setAllRowsVisible(true);
        bookingGrid.addItemClickListener(journalItemClickEvent -> bookingGrid.getUI().ifPresent(
                ui -> ui.navigate(BatchDetailsView.class,String.valueOf(journalItemClickEvent.getItem().getBatch().getId()))
        ));

        return bookingGrid;
    }

    public static Grid<Journal> createJournalGrid(Collection<Journal> journals) {
        Grid<Journal> journalGrid = new Grid<>(Journal.class);
        journalGrid.removeAllColumns();
        journalGrid.setItems(journals);
        journalGrid.addColumn(Journal::getDate).setHeader("Date");
        journalGrid.addColumn(Journal::getEventType).setHeader("Event");
        journalGrid.setAllRowsVisible(true);
        journalGrid.addItemClickListener(journalItemClickEvent -> journalGrid.getUI().ifPresent(
                ui -> ui.navigate(JournalDetailsView.class,String.valueOf(journalItemClickEvent.getItem().getId()))
        ));
        return journalGrid;
    }

    public static Grid<BatchEntry> createBatchEntryGrid(Collection<BatchEntry> batchEntries) {
        Grid<BatchEntry> batchEntryGrid = new Grid<>(BatchEntry.class);
        batchEntryGrid.removeAllColumns();
        batchEntryGrid.setItems(batchEntries);
        batchEntryGrid.addColumn(batchEntry -> batchEntry.getTransaction().getTransactionReference()).setHeader("Transaction");
        batchEntryGrid.addColumn(BatchEntry::getCurrency).setHeader("Currency");
        batchEntryGrid.addColumn(BatchEntry::getOriginalAmount).setHeader("Original amount");
        batchEntryGrid.addColumn(BatchEntry::getOpenAmount).setHeader("Open amount");
        batchEntryGrid.addItemClickListener(batchEntryItemClickEvent -> batchEntryGrid.getUI().ifPresent(
                ui -> ui.navigate(batchEntryItemClickEvent.getItem().getTransaction().getType().equals(Transaction.TransactionType.PAYMENT) ? PaymentDetailsView.class : ModificationDetailsView.class,
                        String.valueOf(batchEntryItemClickEvent.getItem().getTransaction().getId()))
        ));
        return batchEntryGrid;
    }


    public static Grid<Account> createAccountGrid(Collection<Account> accounts) {
        Grid<Account> accountGrid = new Grid<>(Account.class);
        GridListDataView<Account> dataView = accountGrid.setItems(accounts);
        accountGrid.removeAllColumns();
        Grid.Column<Account> accountNameColumn = accountGrid.addColumn(Account::getAccountName).setHeader("Account");
        Grid.Column<Account> accountTypeColumn = accountGrid.addColumn(account -> account.getAccountType().toString()).setHeader("Type");
        Grid.Column<Account> parentColumn = accountGrid.addColumn(GridCreator::getParentName);
        accountGrid.addItemClickListener(accountItemClickEvent -> accountGrid.getUI().ifPresent(
                ui -> ui.navigate(AccountDetailsView.class,String.valueOf(accountItemClickEvent.getItem().getId()))));

        AccountFilter accountFilter = new AccountFilter(dataView);
        accountGrid.getHeaderRows().clear();
        HeaderRow headerRow = accountGrid.appendHeaderRow();
        headerRow.getCell(accountNameColumn).setComponent(new FilterHeader(accountFilter::setAccountName));
        headerRow.getCell(accountTypeColumn).setComponent(new FilterHeader(accountFilter::setAccountType));
        headerRow.getCell(parentColumn).setComponent(new FilterHeader(accountFilter::setParent));
        return accountGrid;
    }

    private static class AccountFilter {
        private final GridListDataView<Account> dataView;

        private String accountName;
        private String accountType;
        private String parent;

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

        public void setParent(String parent) {
            this.parent = parent;
            this.dataView.refreshAll();
        }

        public boolean test(Account account) {
            return (
                    matches(account.getAccountName(), accountName)
                            && matches(account.getAccountType().toString(),accountType)
                            && matches(getParentName(account),parent)
            );
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }

    private static String getParentName(Account account) {
        if (account.getParent() != null) {
            return account.getParent().getAccountName();
        }
        return "";
    }

}

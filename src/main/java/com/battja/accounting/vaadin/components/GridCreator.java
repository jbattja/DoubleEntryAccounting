package com.battja.accounting.vaadin.components;

import com.battja.accounting.entities.*;
import com.battja.accounting.util.JournalUtil;
import com.battja.accounting.vaadin.details.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dataview.GridListDataView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GridCreator {

    public static Grid<Booking> createBookingGrid(Collection<Booking> bookings) {
        Grid<Booking> bookingGrid = new Grid<>(Booking.class);
        bookingGrid.removeAllColumns();
        bookingGrid.setItems(bookings);
        bookingGrid.addColumn(booking -> booking.getAccount().getAccountName()).setHeader("Account");
        bookingGrid.addColumn(Booking::getRegister).setHeader("Register");
        bookingGrid.addColumn(booking -> booking.getJournal().getDate()).setHeader("Date");
        bookingGrid.addColumn(booking -> booking.getJournal().getEventType()).setHeader("Event");
        bookingGrid.addColumn(Booking::getCurrency).setHeader("Currency");
        bookingGrid.addColumn(booking -> booking.getAmount() < 0 ? booking.getAmount() * -1 : "").setHeader("Debit Amount");
        bookingGrid.addColumn(booking -> booking.getAmount() >= 0 ? booking.getAmount() : "").setHeader("Credit Amount");
        bookingGrid.addColumn(booking -> booking.getBatch().getBatchNumber()).setHeader("BatchNumber");
        bookingGrid.setAllRowsVisible(true);
        bookingGrid.addItemClickListener(bookingItemClickEvent -> bookingGrid.getUI().ifPresent(
                ui -> ui.navigate(BatchDetailsView.class,String.valueOf(bookingItemClickEvent.getItem().getBatch().getId()))
        ));

        return bookingGrid;
    }

    public static Grid<Journal> createJournalGrid(Collection<Journal> journals) {
        List<Journal> journalList = new ArrayList<>(journals);
        Collections.sort(journalList);
        Grid<Journal> journalGrid = new Grid<>(Journal.class);
        journalGrid.removeAllColumns();
        journalGrid.setItems(journalList);
        journalGrid.addColumn(Journal::getDate).setHeader("Date");
        journalGrid.addColumn(Journal::getEventType).setHeader("Event");
        journalGrid.setAllRowsVisible(true);
        journalGrid.addItemClickListener(journalItemClickEvent -> journalGrid.getUI().ifPresent(
                ui -> ui.navigate(JournalDetailsView.class,String.valueOf(journalItemClickEvent.getItem().getId()))
        ));
        return journalGrid;
    }

    public static Grid<BatchEntry> createBatchEntryGrid(Collection<BatchEntry> batchEntries, boolean showOpenAndClosed) {
        Grid<BatchEntry> batchEntryGrid = new Grid<>(BatchEntry.class);
        batchEntryGrid.removeAllColumns();
        batchEntryGrid.setItems(batchEntries);
        batchEntryGrid.addColumn(JournalUtil::getReference).setHeader("Reference");
        batchEntryGrid.addColumn(BatchEntry::getCurrency).setHeader("Currency");
        if (showOpenAndClosed) {
            batchEntryGrid.addColumn(BatchEntry::getOriginalAmount).setHeader("Original amount");
            batchEntryGrid.addColumn(BatchEntry::getOpenAmount).setHeader("Open amount");
        } else {
            batchEntryGrid.addColumn(BatchEntry::getOriginalAmount).setHeader("Amount");
        }
        batchEntryGrid.addItemClickListener(batchEntryItemClickEvent -> onBatchEntryClick(batchEntryItemClickEvent,batchEntryGrid));
        return batchEntryGrid;
    }

    private static void onBatchEntryClick(ItemClickEvent<BatchEntry> batchEntryItemClickEvent, Grid<BatchEntry> batchEntryGrid) {
        final Journal journal = JournalUtil.getFirstJournal(batchEntryItemClickEvent.getItem());
        if (journal != null) {
            batchEntryGrid.getUI().ifPresent(
                    ui -> ui.navigate(
                            JournalDetailsView.class,
                            String.valueOf(journal.getId())
                    )
            );
        }
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

    public static Grid<Transaction> createPaymentsGrid(Collection<Transaction> payments) {
        Grid<Transaction> grid = new Grid<>(Transaction.class);
        GridListDataView<Transaction> dataView = grid.setItems(payments);
        grid.removeAllColumns();
        Grid.Column<Transaction> merchantColumn = grid.addColumn(transaction -> transaction.getMerchantAccount().getAccountName()).setHeader("Merchant");
        Grid.Column<Transaction> referenceColumn = grid.addColumn(Transaction::getTransactionReference).setHeader("Reference");
        Grid.Column<Transaction> paymentMethod =  grid.addColumn(Transaction::getPaymentMethod).setHeader("Payment Method");
        Grid.Column<Transaction> statusColumn =  grid.addColumn(Transaction::getStatus).setHeader("Status");
        Grid.Column<Transaction> currencyColumn = grid.addColumn(Transaction::getCurrency).setHeader("Currency");
        Grid.Column<Transaction> amountColumn = grid.addColumn(Transaction::getAmount).setHeader("Amount");
        Grid.Column<Transaction> acquirerAccountColumn = grid.addColumn(transaction -> transaction.getPartnerAccount().getAccountName()).setHeader("Partner Account");

        TransactionFilter transactionFilter = new TransactionFilter(dataView);
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        headerRow.getCell(merchantColumn).setComponent(new FilterHeader(transactionFilter::setMerchant));
        headerRow.getCell(referenceColumn).setComponent(new FilterHeader(transactionFilter::setReference));
        headerRow.getCell(paymentMethod).setComponent(new FilterHeader(transactionFilter::setPaymentMethod));
        headerRow.getCell(statusColumn).setComponent(new FilterHeader(transactionFilter::setStatus));
        headerRow.getCell(currencyColumn).setComponent(new FilterHeader(transactionFilter::setCurrency));
        headerRow.getCell(amountColumn).setComponent(new FilterHeader(transactionFilter::setAmount));
        headerRow.getCell(acquirerAccountColumn).setComponent(new FilterHeader(transactionFilter::setPartnerAccount));

        grid.addItemClickListener(transactionItemClickEvent -> grid.getUI().ifPresent(
                ui -> ui.navigate(PaymentDetailsView.class,String.valueOf(transactionItemClickEvent.getItem().getId()))
        ));
        return grid;
    }

    public static class TransactionFilter {
        private final GridListDataView<Transaction> dataView;

        private String merchant;
        private String reference;
        private String paymentMethod;
        private String status;
        private String currency;
        private String amount;
        private String partnerAccount;

        public TransactionFilter(GridListDataView<Transaction> dataView) {
            this.dataView = dataView;
            this.dataView.setFilter(this::test);
        }

        public void setMerchant(String merchant) {
            this.merchant = merchant;
            this.dataView.refreshAll();
        }

        public void setReference(String reference) {
            this.reference = reference;
            this.dataView.refreshAll();
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            this.dataView.refreshAll();
        }

        public void setStatus(String status) {
            this.status = status;
            this.dataView.refreshAll();
        }

        public void setCurrency(String currency) {
            this.currency = currency;
            this.dataView.refreshAll();
        }

        public void setAmount(String amount) {
            this.amount = amount;
            this.dataView.refreshAll();
        }

        public void setPartnerAccount(String partnerAccount) {
            this.partnerAccount = partnerAccount;
            this.dataView.refreshAll();
        }

        public boolean test(Transaction transaction) {
            return (
                    matches(transaction.getMerchantAccount().getAccountName(), merchant)
                            && matches(transaction.getTransactionReference(), reference)
                            && matches(transaction.getPaymentMethod().toString(), paymentMethod)
                            && matches(transaction.getStatus(), status)
                            && matches((transaction.getCurrency()),currency)
                            && matches((transaction.getAmount().toString()),amount)
                            && matches((transaction.getPartnerAccount().getAccountName()), partnerAccount)
            );
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }

    }

}

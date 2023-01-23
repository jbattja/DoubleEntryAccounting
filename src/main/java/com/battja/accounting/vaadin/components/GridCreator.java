package com.battja.accounting.vaadin.components;

import com.battja.accounting.entities.*;
import com.battja.accounting.util.JournalUtil;
import com.battja.accounting.vaadin.details.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dataview.GridListDataView;

import java.util.*;

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
        headerRow.getCell(accountNameColumn).setComponent(new MultiSelectFilterHeader<>(accountFilter::setAccountName, accounts));
        headerRow.getCell(accountTypeColumn).setComponent(new MultiSelectFilterHeader<>(accountFilter::setAccountType, Arrays.stream(Account.AccountType.values()).toList()));
        headerRow.getCell(parentColumn).setComponent(new MultiSelectFilterHeader<>(accountFilter::setParent, accounts));
        return accountGrid;
    }

    private static class AccountFilter {
        private final GridListDataView<Account> dataView;

        private Set<Account> accounts;
        private Set<Account.AccountType> accountTypes;
        private Set<Account> parents;

        public AccountFilter(GridListDataView<Account> dataView) {
            this.dataView = dataView;
            this.dataView.setFilter(this::test);
        }

        public void setAccountName(Set<Account> accounts) {
            this.accounts = accounts;
            this.dataView.refreshAll();
        }

        public void setAccountType(Set<Account.AccountType>  accountTypes) {
            this.accountTypes = accountTypes;
            this.dataView.refreshAll();
        }

        public void setParent(Set<Account> parents) {
            this.parents = parents;
            this.dataView.refreshAll();
        }

        public boolean test(Account account) {
            if(accounts != null && !accounts.isEmpty() && !accounts.contains(account)) {
                return false;
            }
            if(parents != null && !parents.isEmpty() && !parents.contains(account.getParent())) {
                return false;
            }
            return accountTypes == null || accountTypes.isEmpty() || accountTypes.contains(account.getAccountType());
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
        Set<Account> merchantAccounts = new HashSet<>();
        Set<Account> partnerAccounts = new HashSet<>();
        Set<SelectableString> statuses = new HashSet<>();
        for (Transaction payment : payments) {
            merchantAccounts.add(payment.getMerchantAccount());
            statuses.add(new SelectableString(payment.getStatus()));
            partnerAccounts.add(payment.getPartnerAccount());
        }

        HeaderRow headerRow = grid.appendHeaderRow();
        headerRow.getCell(merchantColumn).setComponent(new MultiSelectFilterHeader<>(transactionFilter::setMerchant,merchantAccounts));
        headerRow.getCell(referenceColumn).setComponent(new FilterHeader(transactionFilter::setReference));
        headerRow.getCell(paymentMethod).setComponent(new MultiSelectFilterHeader<>(transactionFilter::setPaymentMethod, Arrays.stream(PaymentMethod.values()).toList()));
        headerRow.getCell(statusColumn).setComponent(new MultiSelectFilterHeader<>(transactionFilter::setStatus,statuses));
        headerRow.getCell(currencyColumn).setComponent(new MultiSelectFilterHeader<>(transactionFilter::setCurrency, Arrays.stream(Amount.Currency.values()).toList()));
        headerRow.getCell(amountColumn).setComponent(new FilterHeader(transactionFilter::setAmount));
        headerRow.getCell(acquirerAccountColumn).setComponent(new MultiSelectFilterHeader<>(transactionFilter::setPartnerAccount,partnerAccounts));

        grid.addItemClickListener(transactionItemClickEvent -> grid.getUI().ifPresent(
                ui -> ui.navigate(PaymentDetailsView.class,String.valueOf(transactionItemClickEvent.getItem().getId()))
        ));
        return grid;
    }

    public static class TransactionFilter {
        private final GridListDataView<Transaction> dataView;

        private Set<Account> merchants;
        private String reference;
        private Set<PaymentMethod> paymentMethods;
        private Set<SelectableString> status;
        private Set<Amount.Currency> currencies;
        private String amount;
        private Set<Account> partnerAccounts;

        public TransactionFilter(GridListDataView<Transaction> dataView) {
            this.dataView = dataView;
            this.dataView.setFilter(this::test);
        }

        public void setMerchant(Set<Account> merchants) {
            this.merchants = merchants;
            this.dataView.refreshAll();
        }

        public void setReference(String reference) {
            this.reference = reference;
            this.dataView.refreshAll();
        }

        public void setPaymentMethod(Set<PaymentMethod> paymentMethods) {
            this.paymentMethods = paymentMethods;
            this.dataView.refreshAll();
        }

        public void setStatus(Set<SelectableString> status) {
            this.status = status;
            this.dataView.refreshAll();
        }

        public void setCurrency(Set<Amount.Currency> currencies) {
            this.currencies = currencies;
            this.dataView.refreshAll();
        }

        public void setAmount(String amount) {
            this.amount = amount;
            this.dataView.refreshAll();
        }

        public void setPartnerAccount(Set<Account> partnerAccounts) {
            this.partnerAccounts = partnerAccounts;
            this.dataView.refreshAll();
        }

        public boolean test(Transaction transaction) {
            if(merchants != null && !merchants.isEmpty() && !merchants.contains(transaction.getMerchantAccount())) {
                return false;
            }
            if(paymentMethods != null && !paymentMethods.isEmpty() && !paymentMethods.contains(transaction.getPaymentMethod())) {
                return false;
            }
            if(currencies != null && !currencies.isEmpty() && !currencies.contains(transaction.getCurrency())) {
                return false;
            }
            if(status != null && !status.isEmpty() && !status.contains(new SelectableString(transaction.getStatus()))) {
                return false;
            }
            if(partnerAccounts != null && !partnerAccounts.isEmpty() && !partnerAccounts.contains(transaction.getPartnerAccount())) {
                return false;
            }
            return (
                            matches(transaction.getTransactionReference(), reference)
                            && matches((transaction.getAmount().toString()),amount)
            );
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }

    }

}

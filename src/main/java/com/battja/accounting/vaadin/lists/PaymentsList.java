package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Transaction;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.FilterHeader;
import com.battja.accounting.vaadin.details.PaymentDetailsView;
import com.battja.accounting.vaadin.entryforms.CreatePaymentForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="payments", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Payments")
public class PaymentsList extends VerticalLayout {

    private final TransactionService transactionService;
    final Grid<Transaction> grid;

    public PaymentsList(TransactionService transactionService) {
        this.transactionService = transactionService;
        Button createPaymentButton = new Button("Create Payment");
        createPaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPaymentButton.addClickListener(buttonClickEvent -> createPaymentButton.getUI().ifPresent(ui -> ui.navigate(CreatePaymentForm.class)));
        add(createPaymentButton);
        this.grid = new Grid<>(Transaction.class);
        add(grid);
        listPayments();
    }

    private void listPayments(){
        GridListDataView<Transaction> dataView = grid.setItems(transactionService.listAllPayments());
        grid.removeAllColumns();
        Grid.Column<Transaction> merchantColumn = grid.addColumn(transaction -> transaction.getMerchantAccount().getAccountName()).setHeader("Merchant");
        Grid.Column<Transaction> referenceColumn = grid.addColumn(Transaction::getTransactionReference).setHeader("Reference");
        Grid.Column<Transaction> statusColumn =  grid.addColumn(Transaction::getStatus).setHeader("Status");
        Grid.Column<Transaction> currencyColumn = grid.addColumn(Transaction::getCurrency).setHeader("Currency");
        Grid.Column<Transaction> amountColumn = grid.addColumn(Transaction::getAmount).setHeader("Amount");
        Grid.Column<Transaction> acquirerAccountColumn = grid.addColumn(transaction -> transaction.getAcquirerAccount().getAccountName()).setHeader("Acquirer Account");

        TransactionFilter transactionFilter = new TransactionFilter(dataView);
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        headerRow.getCell(merchantColumn).setComponent(new FilterHeader(transactionFilter::setMerchant));
        headerRow.getCell(referenceColumn).setComponent(new FilterHeader(transactionFilter::setReference));
        headerRow.getCell(statusColumn).setComponent(new FilterHeader(transactionFilter::setStatus));
        headerRow.getCell(currencyColumn).setComponent(new FilterHeader(transactionFilter::setCurrency));
        headerRow.getCell(amountColumn).setComponent(new FilterHeader(transactionFilter::setAmount));
        headerRow.getCell(acquirerAccountColumn).setComponent(new FilterHeader(transactionFilter::setAcquirerAccount));

        grid.addItemClickListener(transactionItemClickEvent -> grid.getUI().ifPresent(
                ui -> ui.navigate(PaymentDetailsView.class,String.valueOf(transactionItemClickEvent.getItem().getId()))
        ));

    }

    public static class TransactionFilter {
        private final GridListDataView<Transaction> dataView;

        private String merchant;
        private String reference;
        private String status;
        private String currency;
        private String amount;
        private String acquirerAccount;

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

        public void setAcquirerAccount(String acquirerAccount) {
            this.acquirerAccount = acquirerAccount;
            this.dataView.refreshAll();
        }

        public boolean test(Transaction transaction) {
            return (
                    matches(transaction.getMerchantAccount().getAccountName(), merchant)
                            && matches(transaction.getTransactionReference(), reference)
                            && matches(transaction.getStatus(), status)
                            && matches((transaction.getCurrency()),currency)
                            && matches((transaction.getAmount().toString()),amount)
                            && matches((transaction.getAcquirerAccount().getAccountName()),acquirerAccount)
            );
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }

    }
}

package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Batch;
import com.battja.accounting.repositories.BatchRepository;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.FilterHeader;
import com.battja.accounting.vaadin.details.BatchDetailsView;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="batches", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Batches")
public class BatchList extends VerticalLayout {

    private final BatchRepository repository;
    final Grid<Batch> grid;

    public BatchList(BatchRepository repository) {
        this.repository = repository;
        this.grid = new Grid<>(Batch.class);
        add(grid);
        listBatches();
    }

    private void listBatches(){
        GridListDataView<Batch> dataView = grid.setItems(repository.findAll());
        grid.removeAllColumns();
        Grid.Column<Batch> accountNameColumn = grid.addColumn(batch -> batch.getAccount().getAccountName()).setHeader("Account");
        Grid.Column<Batch> registerColumn = grid.addColumn(Batch::getRegister).setHeader("Register");
        Grid.Column<Batch> batchNumberColumn = grid.addColumn(Batch::getBatchNumber).setHeader("Number");
        Grid.Column<Batch> statusColumn = grid.addColumn(batch -> batch.getStatus().toString()).setHeader("Status");
        grid.addColumn(Batch::getOpenDate).setHeader("Open Date");
        grid.addColumn(Batch::getCloseDate).setHeader("Close Date");

        grid.addItemClickListener(batchItemClickEvent -> grid.getUI().ifPresent(
                ui -> ui.navigate(BatchDetailsView.class,String.valueOf(batchItemClickEvent.getItem().getId()))
        ));

        BatchFilter batchFilter = new BatchFilter(dataView);
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        headerRow.getCell(accountNameColumn).setComponent(new FilterHeader(batchFilter::setAccountName));
        headerRow.getCell(registerColumn).setComponent(new FilterHeader(batchFilter::setRegister));
        headerRow.getCell(batchNumberColumn).setComponent(new FilterHeader(batchFilter::setBatchNumber));
        headerRow.getCell(statusColumn).setComponent(new FilterHeader(batchFilter::setStatus));
    }

    public static class BatchFilter {
        private final GridListDataView<Batch> dataView;

        private String accountName;
        private String register;
        private String batchNumber;
        private String status;

        public BatchFilter(GridListDataView<Batch> dataView) {
            this.dataView = dataView;
            this.dataView.setFilter(this::test);
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
            this.dataView.refreshAll();
        }

        public void setRegister(String register) {
            this.register = register;
            this.dataView.refreshAll();
        }

        public void setBatchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
            this.dataView.refreshAll();
        }

        public void setStatus(String status) {
            this.status = status;
            this.dataView.refreshAll();
        }

        public boolean test(Batch batch) {
            return (
                    matches(batch.getAccount().getAccountName(), accountName)
                    && matches(batch.getRegister().toString(),register)
                    && matches(batch.getBatchNumber().toString(),batchNumber)
                    && matches((batch.getStatus().toString()),status)
            );
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }

    }
}

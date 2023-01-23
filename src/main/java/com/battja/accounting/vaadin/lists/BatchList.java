package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Batch;
import com.battja.accounting.entities.RegisterType;
import com.battja.accounting.repositories.BatchRepository;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.BatchService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.FilterHeader;
import com.battja.accounting.vaadin.components.MultiSelectFilterHeader;
import com.battja.accounting.vaadin.details.BatchDetailsView;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="batches", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Batches")
public class BatchList extends VerticalLayout {

    private final BatchService batchService;
    final Grid<Batch> grid;

    public BatchList(BatchService batchService) {
        this.batchService = batchService;
        this.grid = new Grid<>(Batch.class);
        add(grid);
        listBatches();
    }

    private void listBatches(){
        List<Batch> batches = batchService.listAll();
        GridListDataView<Batch> dataView = grid.setItems(batches);
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
        Set<Account> accountList = new HashSet<>();
        for (Batch batch : batches) {
            accountList.add(batch.getAccount());
        }
        HeaderRow headerRow = grid.appendHeaderRow();
        headerRow.getCell(accountNameColumn).setComponent(new MultiSelectFilterHeader<>(batchFilter::setAccountName,accountList));
        headerRow.getCell(registerColumn).setComponent(new MultiSelectFilterHeader<>(batchFilter::setRegister, Arrays.stream(RegisterType.values()).toList()));
        headerRow.getCell(batchNumberColumn).setComponent(new FilterHeader(batchFilter::setBatchNumber));
        headerRow.getCell(statusColumn).setComponent(new MultiSelectFilterHeader<>(batchFilter::setStatus, Arrays.stream(Batch.BatchStatus.values()).toList()));
    }

    public static class BatchFilter {
        private final GridListDataView<Batch> dataView;

        private Set<Integer> accountIds;
        private Set<RegisterType> registers;
        private String batchNumber;
        private Set<Batch.BatchStatus> statuses;

        public BatchFilter(GridListDataView<Batch> dataView) {
            this.dataView = dataView;
            this.dataView.setFilter(this::test);
        }

        public void setAccountName(Set<Account> accounts) {
            accountIds = new HashSet<>();
            for (Account account : accounts) {
                accountIds.add(account.getId());
            }
            this.dataView.refreshAll();
        }

        public void setRegister(Set<RegisterType> registers) {
            this.registers = registers;
            this.dataView.refreshAll();
        }

        public void setBatchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
            this.dataView.refreshAll();
        }

        public void setStatus(Set<Batch.BatchStatus> statuses) {
            this.statuses = statuses;
            this.dataView.refreshAll();
        }

        public boolean test(Batch batch) {
            if(accountIds != null && !accountIds.isEmpty() && !accountIds.contains(batch.getAccount().getId())) {
                return false;
            }
            if(registers != null && !registers.isEmpty() && !registers.contains(batch.getRegister())) {
                return false;
            }
            if(statuses != null && !statuses.isEmpty() && !statuses.contains(batch.getStatus())) {
                return false;
            }
            return (
                    matches(batch.getBatchNumber().toString(),batchNumber)
            );
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }

    }
}

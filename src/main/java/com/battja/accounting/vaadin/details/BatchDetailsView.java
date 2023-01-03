package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Batch;
import com.battja.accounting.entities.BatchEntry;
import com.battja.accounting.services.BatchService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value="batch-details", layout = MainLayout.class)
@PageTitle("BattjaPay | Batch Details")
public class BatchDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(BatchDetailsView.class);

    private final BatchService batchService;
    private Batch batch;
    private Integer batchId;
    private List<BatchEntry> batchEntryList;

    public BatchDetailsView(BatchService batchService) {
        this.batchService = batchService;
        this.setMargin(false);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            batchId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        batch = batchService.getBatch(batchId);

        if (batch != null) {
            add(new H3("Batch Details"));

            ReadOnlyForm form = new ReadOnlyForm();
            form.addField("Account",batch.getAccount().getAccountName());
            form.addField("Register",batch.getRegister().toString());
            form.addField("Batch number",batch.getBatchNumber().toString());
            form.addField("Status",batch.getStatus().toString());
            form.addField("Open Date",batch.getOpenDate().toString());
            form.addField("End Date",batch.getEndDate() != null ? batch.getEndDate().toString() : "");
            form.addField("Close Date",batch.getCloseDate() != null ? batch.getCloseDate().toString() : "");

            add(form);
            Grid<BatchEntry> grid = createBatchEntryGrid();
            if (batchEntryList != null) {
                add(new H4("Summary"));
                add(createSummaryForm());
                add(createButtonsLayout());
                add(new H4("Entries"));
                add(grid);
            }
        }
    }

    private ReadOnlyForm createSummaryForm() {
        ReadOnlyForm summaryForm = new ReadOnlyForm();
        int openItems = 0;
        Map<String,Long> currencyMap = new HashMap<>();
        Map<String,Long> currencyOpenMap = new HashMap<>();
        for (BatchEntry b : batchEntryList) {
            String currency = b.getCurrency();
            if (b.getOpenAmount() != 0) {
                openItems++;
            }
            currencyMap.merge(currency, b.getOriginalAmount(), Long::sum);
            currencyOpenMap.merge(currency,b.getOpenAmount(),Long::sum);
        }

        for (Map.Entry<String,Long> entry : currencyMap.entrySet()) {
            summaryForm.addField("Open / total Amount (" + entry.getKey() + ")",
                    currencyOpenMap.get(entry.getKey()) + " / " + entry.getValue().toString());
        }
        summaryForm.addField("Open / Closed",openItems +  "/" + batchEntryList.size());
        return summaryForm;
    }

    private Grid<BatchEntry> createBatchEntryGrid() {
        Grid<BatchEntry> batchEntryGrid = new Grid<>(BatchEntry.class);
        if (batch != null) {
            batchEntryList = batchService.getEntries(batch.getId());
            batchEntryGrid.removeAllColumns();
            batchEntryGrid.setItems(batchEntryList);
            batchEntryGrid.addColumn(batchEntry -> batchEntry.getTransaction().getTransactionReference()).setHeader("Transaction");
            batchEntryGrid.addColumn(BatchEntry::getCurrency).setHeader("Currency");
            batchEntryGrid.addColumn(BatchEntry::getOriginalAmount).setHeader("Original amount");
            batchEntryGrid.addColumn(BatchEntry::getOpenAmount).setHeader("Open amount");
        }
        return batchEntryGrid;
    }

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        Button endBatchPeriodButton = new Button("End period");
        endBatchPeriodButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        endBatchPeriodButton.addClickListener(buttonClickEvent -> endBatchPeriod());
        if (batch.getEndDate() != null) {
            endBatchPeriodButton.setEnabled(false);
        }
        buttonsLayout.add(endBatchPeriodButton);

        Button closeBatchButton = new Button("Close");
        closeBatchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeBatchButton.addClickListener(buttonClickEvent -> closeBatch());
        if (!batchService.canBatchClose(batch)) {
            closeBatchButton.setEnabled(false);
        }
        buttonsLayout.add(closeBatchButton);
        return buttonsLayout;
    }

    private void endBatchPeriod() {
        boolean success = batchService.endBatchPeriod(batch.getId());
        if (!success) {
            //TODO some notification
        }
        updateView();
    }

    private void closeBatch() {
        boolean success = batchService.closeBatch(batch.getId());
        if (!success) {
            //TODO some notification
        }
        updateView();
    }
}

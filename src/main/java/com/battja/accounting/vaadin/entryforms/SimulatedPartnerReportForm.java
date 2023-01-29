package com.battja.accounting.vaadin.entryforms;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Batch;
import com.battja.accounting.entities.PartnerReport;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.BatchService;
import com.battja.accounting.services.PartnerReportService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="simulate-partner-form", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Simulate Partner Report")
public class SimulatedPartnerReportForm extends VerticalLayout {

    private final PartnerReportService partnerReportService;
    private final AccountService accountService;
    private final BatchService batchService;

    public SimulatedPartnerReportForm(PartnerReportService partnerReportService, AccountService accountService, BatchService batchService) {
        this.partnerReportService = partnerReportService;
        this.accountService = accountService;
        this.batchService = batchService;
        updateView();
    }

    private void updateView() {
        add(new H3("Create Simulated Partner Report"));
        add(createInputForm());
        Button createPartnerReportButton = new Button("Create");
        createPartnerReportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPartnerReportButton.addClickListener(buttonClickEvent -> createPartnerReport());
        add(createPartnerReportButton);
    }

    private Select<Account> partnerSelect;
    private Select<Batch> batchSelect;
    private List<Batch> captureBatches;

    private FormLayout createInputForm() {
        FormLayout layout = new FormLayout();
        if (captureBatches == null) {
            captureBatches = new ArrayList<>();
        }
        partnerSelect = new Select<>();
        partnerSelect.setLabel("Partner");
        partnerSelect.setItems(accountService.listAccountsByType(Account.AccountType.PARTNER));
        partnerSelect.setRenderer(new TextRenderer<>(Account::getAccountName));
        partnerSelect.addValueChangeListener(selectAccountComponentValueChangeEvent -> retrieveCaptureBatches(selectAccountComponentValueChangeEvent.getValue()));
        layout.add(partnerSelect);
        batchSelect = new Select<>();
        batchSelect.setLabel("Capture Batch");
        batchSelect.setItems(captureBatches);
        batchSelect.setRenderer(new TextRenderer<>(Batch::getDisplayName));
        layout.add(batchSelect);
        return layout;
    }

    private void retrieveCaptureBatches(Account partner) {
        batchSelect.setValue(null);
        List<Batch.BatchStatus> statuses = new ArrayList<>();
        statuses.add(Batch.BatchStatus.AVAILABLE);
        statuses.add(Batch.BatchStatus.ENDED);
        captureBatches = batchService.findCaptureBatches(partner, statuses);
        batchSelect.setItems(captureBatches);
    }

    private void createPartnerReport() {
        partnerSelect.setHelperComponent(null);
        batchSelect.setHelperComponent(null);
        if(partnerSelect.getValue() == null) {
            partnerSelect.setHelperComponent(new Span("Field is required"));
            partnerSelect.focus();
            return;
        }
        if(batchSelect.getValue()  == null) {
            batchSelect.setHelperComponent(new Span("Field is required"));
            batchSelect.focus();
            return;
        }
        PartnerReport report = partnerReportService.simulatePartnerReport(batchSelect.getValue());
        if (report == null) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to simulate partner report",false);
            notification.open();
        } else {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Created a simulated partner report",true);
            notification.open();
            partnerSelect.setValue(null);
            batchSelect.setValue(null);
        }
    }



}

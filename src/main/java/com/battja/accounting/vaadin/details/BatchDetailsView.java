package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Batch;
import com.battja.accounting.entities.BatchEntry;
import com.battja.accounting.entities.Booking;
import com.battja.accounting.services.BatchService;
import com.battja.accounting.services.BookingService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.components.ReadOnlyForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
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

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="batch-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Batch Details")
public class BatchDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(BatchDetailsView.class);

    private final BatchService batchService;
    private final BookingService bookingService;
    private Batch batch;
    private Integer batchId;
    private List<BatchEntry> batchEntryList;

    public BatchDetailsView(BatchService batchService, BookingService bookingService) {
        this.batchService = batchService;
        this.bookingService = bookingService;
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
            batchEntryList = batchService.getEntries(batch.getId());
            if (batchEntryList != null) {
                add(new H4("Summary"));
                add(createSummaryForm());
                add(createButtonsLayout());
                add(new H4("Entries"));
                add(GridCreator.createBatchEntryGrid(batchEntryList,batch.getRegister().requiresEntryReconciliation()));
            }
            List<Booking> bookings = batchService.getBookings(batch.getId());
            if(bookings != null && !bookings.isEmpty()) {
                add(new H4("Bookings"));
                add(GridCreator.createBookingGrid(bookings));
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
            if (batch.getRegister().requiresEntryReconciliation()) {
                summaryForm.addField("Open / total Amount (" + entry.getKey() + ")",
                        currencyOpenMap.get(entry.getKey()) + " / " + entry.getValue().toString());
            } else {
                summaryForm.addField("Total Amount (" + entry.getKey() + ")",entry.getValue().toString());
            }
        }
        if (batch.getRegister().requiresEntryReconciliation()) {
            summaryForm.addField("Open / Closed", openItems + "/" + batchEntryList.size());
        } else {
            summaryForm.addField("# Entries", String.valueOf(batchEntryList.size()));
        }
        return summaryForm;
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
        if (!batch.getRegister().requiresEntryReconciliation()) {
            Button bookBalanceTransferButton = new Button("Book Balance Transfer");
            bookBalanceTransferButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            bookBalanceTransferButton.addClickListener(buttonClickEvent -> bookBalanceTransfer());
            if (!batchService.canBookBalanceTransfer(batch)) {
                bookBalanceTransferButton.setEnabled(false);
            }
            buttonsLayout.add(bookBalanceTransferButton);
        }
        return buttonsLayout;
    }

    private void bookBalanceTransfer() {
        boolean success = bookingService.bookBalanceTransfer(batch.getId());
        if (!success) {
            Notification notification = new NotificationWithCloseButton("Unable to book balance transfer",false);
            notification.open();
            return;
        }
        Notification notification = new NotificationWithCloseButton("Balance transfer booked", true);
        notification.open();
        updateView();
    }

    private void endBatchPeriod() {
        boolean success = batchService.endBatchPeriod(batch.getId());
        if (!success) {
            Notification notification = new NotificationWithCloseButton("Unable to end batch period",false);
            notification.open();
            return;
        }
        Notification notification = new NotificationWithCloseButton("Batch period ended", true);
        notification.open();
        updateView();
    }

    private void closeBatch() {
        boolean success = batchService.closeBatch(batch.getId());
        if (!success) {
            Notification notification = new NotificationWithCloseButton("Unable to close batch",false);
            notification.open();
            return;
        }
        Notification notification = new NotificationWithCloseButton("Batch closed", true);
        notification.open();
        updateView();
    }
}

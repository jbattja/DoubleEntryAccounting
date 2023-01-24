package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.services.FeeService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.CustomDetailsForm;
import com.battja.accounting.vaadin.components.EditSaveCancelButtonsLayout;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="fee-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Fee Details")
public class FeeDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(FeeDetailsView.class);

    private final FeeService feeService;
    private Integer feeId;
    private Fee fee;

    public FeeDetailsView(FeeService feeService) {
        this.feeService = feeService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            feeId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        fee = feeService.getFee(feeId);
        if (fee != null && fee.getContract() != null) {
            add(new H3("Fee details: " + fee.getContract().getContractName()));
            add(createBackButton());
            add(createDetailsView());
            add(new EditSaveCancelButtonsLayout<>(form,fee,this::saveFee,this::deleteFee));
        }
    }

    private HorizontalLayout createBackButton() {
        HorizontalLayout layout = new HorizontalLayout();
        Button backButton = new Button("Back to contract", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(buttonClickEvent -> navigateBackToContract());
        layout.add(backButton);
        return layout;
    }

    private void navigateBackToContract() {
        Contract contract = fee.getContract();
        if (contract == null || contract.getId() == null) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to go back to contract", false);
            notification.open();
            return;
        }
        getUI().ifPresent(
                ui -> ui.navigate(ContractDetailsView.class,String.valueOf(contract.getId()))
        );
    }

    private CustomDetailsForm form;
    private Select<EventType> eventTypeSelect;
    private Select<PaymentMethod> paymentMethodSelect;
    private Select<Amount.Currency> currencySelect;
    private IntegerField fixedAmount;
    private IntegerField basisPoints;

    private CustomDetailsForm createDetailsView() {
        form = new CustomDetailsForm();
        eventTypeSelect = form.addSelectableField("Event", fee.getEventType(), Arrays.stream(EventType.values()).toList());
        paymentMethodSelect = form.addSelectableField("Payment Method", fee.getPaymentMethod(), Arrays.stream(PaymentMethod.values()).toList());
        currencySelect = form.addSelectableField("Currency", fee.getCurrency(), Arrays.stream(Amount.Currency.values()).toList());
        fixedAmount = form.addEditableIntegerField("Fixed Amount",(int)fee.getFixedAmount());
        basisPoints = form.addEditableIntegerField("Basis points",fee.getBasisPoints());
        return form;
    }

    private Boolean saveFee(Fee fee) {
        eventTypeSelect.setHelperComponent(null);
        currencySelect.setHelperComponent(null);
        basisPoints.setHelperComponent(null);
        if (eventTypeSelect.getValue() == null) {
            eventTypeSelect.setHelperComponent(new Span("Field is required"));
            eventTypeSelect.focus();
            return false;
        }
        if(currencySelect.getValue() == null && fixedAmount.getValue() != null) {
            currencySelect.setHelperComponent(new Span("Specify currency when using fixed amount"));
            currencySelect.focus();
            return false;
        }
        if(basisPoints.getValue() == null && fixedAmount.getValue() == null) {
            basisPoints.setHelperComponent(new Span("Specify either basis points or fixed amount"));
            basisPoints.focus();
            return false;
        }

        fee.setEventType(eventTypeSelect.getValue());
        fee.setPaymentMethod(paymentMethodSelect.getValue());
        fee.setCurrency(currencySelect.getValue());
        fee.setFixedAmount(fixedAmount.getValue());
        fee.setBasisPoints(basisPoints.getValue());
        Fee updatedFee = feeService.updateFeeLine(fee);
        if (updatedFee == null) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to update fee",false);
            notification.open();
            return false;
        } else {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Fee line updated",true);
            notification.open();
            updateView();
            return true;
        }
    }

    private Boolean deleteFee(Fee fee) {
        if (feeService.removeFeeLine(fee)) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Fee line removed",false);
            notification.open();
            navigateBackToContract();
            return true;
        }
        NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to remove fee",false);
        notification.open();
        return false;
    }

}

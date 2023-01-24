package com.battja.accounting.vaadin.entryforms;

import com.battja.accounting.entities.Amount;
import com.battja.accounting.entities.Contract;
import com.battja.accounting.entities.Fee;
import com.battja.accounting.entities.PaymentMethod;
import com.battja.accounting.events.EventType;
import com.battja.accounting.services.FeeService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.details.ContractDetailsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
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

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="create-fee", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Create Fee")
public class CreateFeeForm extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(CreateAccountForm.class);

    private final FeeService feeService;

    private Integer contractId;
    private Contract contract;

    private Select<EventType> eventTypeSelect;
    private Select<PaymentMethod> paymentMethodSelect;
    private Select<Amount.Currency> currencySelect;
    private IntegerField fixedAmount;
    private IntegerField basisPoints;


    public CreateFeeForm(FeeService feeService) {
        this.feeService = feeService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            contractId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        add(new H3("Create Fee"));
        contract = feeService.getContract(contractId);
        add(createBackButton());
        if (contract == null) {
            log.warn("Cannot create form, incorrect contract id");
            return;
        }
        add(createInputForm());
        Button createFeeButton = new Button("Create");
        createFeeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createFeeButton.addClickListener(buttonClickEvent -> createFee());
        add(createFeeButton);
    }

    private HorizontalLayout createBackButton() {
        HorizontalLayout layout = new HorizontalLayout();
        Button backButton = new Button("Back to contract", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(buttonClickEvent -> navigateBackToContract());
        layout.add(backButton);
        return layout;
    }

    private void navigateBackToContract() {
        if (contract == null || contract.getId() == null) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to go back to contract", false);
            notification.open();
            return;
        }
        getUI().ifPresent(
                ui -> ui.navigate(ContractDetailsView.class,String.valueOf(contract.getId()))
        );
    }

    private FormLayout createInputForm() {
        FormLayout layout = new FormLayout();
        eventTypeSelect = new Select<>();
        eventTypeSelect.setLabel("Event Type");
        eventTypeSelect.setItems(EventType.values());
        layout.add(eventTypeSelect);
        paymentMethodSelect = new Select<>();
        paymentMethodSelect.setLabel("Payment Method");
        paymentMethodSelect.setItems(PaymentMethod.values());
        layout.add(paymentMethodSelect);
        currencySelect = new Select<>();
        currencySelect.setLabel("Currency");
        currencySelect.setItems(Amount.Currency.values());
        layout.add(currencySelect);
        fixedAmount = new IntegerField();
        fixedAmount.setLabel("Fixed Amount");
        layout.add(fixedAmount);
        basisPoints = new IntegerField();
        basisPoints.setLabel("BasisPoints");
        layout.add(basisPoints);
        return layout;
    }

    private void createFee() {
        eventTypeSelect.setHelperComponent(null);
        currencySelect.setHelperComponent(null);
        basisPoints.setHelperComponent(null);
        if(eventTypeSelect.getValue() == null) {
            eventTypeSelect.setHelperComponent(new Span("Field is required"));
            eventTypeSelect.focus();
            return;
        }
        if(currencySelect.getValue() == null && fixedAmount.getValue() != null) {
            currencySelect.setHelperComponent(new Span("Specify currency when using fixed amount"));
            currencySelect.focus();
            return;
        }
        if(basisPoints.getValue() == null && fixedAmount.getValue() == null) {
            basisPoints.setHelperComponent(new Span("Specify either basis points or fixed amount"));
            basisPoints.focus();
            return;
        }
        Fee fee = new Fee();
        fee.setContract(contract);
        fee.setEventType(eventTypeSelect.getValue());
        fee.setPaymentMethod(paymentMethodSelect.getValue());
        fee.setCurrency(currencySelect.getValue());
        if (fixedAmount.getValue() != null) {
            fee.setFixedAmount(fixedAmount.getValue());
        }
        if (basisPoints.getValue() != null) {
            fee.setBasisPoints(basisPoints.getValue());
        }
        fee = feeService.createFeeLine(fee);
        if (fee == null) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to create fee line",false);
            notification.open();
        } else {
            NotificationWithCloseButton notification = new NotificationWithCloseButton("New fee line created",true);
            notification.open();
            navigateBackToContract();
        }
    }
}

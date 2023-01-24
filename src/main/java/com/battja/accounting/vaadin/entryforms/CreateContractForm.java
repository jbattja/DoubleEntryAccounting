package com.battja.accounting.vaadin.entryforms;

import com.battja.accounting.entities.Contract;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.services.FeeService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.details.ContractDetailsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="create-contract", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Create Contract")
public class CreateContractForm extends VerticalLayout {

    private final FeeService feeService;
    private TextField contractName;

    public CreateContractForm(FeeService feeService) {
        this.feeService = feeService;
        updateView();
    }

    private void updateView() {
        add(new H3("Create Contract"));
        add(createInputForm());
        Button createPaymentButton = new Button("Create");
        createPaymentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPaymentButton.addClickListener(buttonClickEvent -> createContract());
        add(createPaymentButton);
    }

    private FormLayout createInputForm() {
        FormLayout layout = new FormLayout();
        contractName = new TextField();
        contractName.setLabel("Contract Name");
        layout.add(contractName);
        return layout;
    }


    private void createContract() {
        contractName.setHelperComponent(null);
        if (contractName.getValue() == null || contractName.isEmpty()) {
            contractName.setHelperComponent(new Span("Field is required"));
            contractName.focus();
            return;
        }
        Contract contract = new Contract(contractName.getValue());
        try {
            Contract finalContract = feeService.createContract(contract);
            if (finalContract != null) {
                NotificationWithCloseButton notification = new NotificationWithCloseButton("Created new contract",true);
                notification.open();
                getUI().ifPresent(ui -> ui.navigate(ContractDetailsView.class, String.valueOf(finalContract.getId())));
            } else {
                NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to create contract",false);
                notification.open();
            }
        } catch (DuplicateNameException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(),false);
            notification.open();
        }

    }


}

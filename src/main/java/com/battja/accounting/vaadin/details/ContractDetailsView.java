package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Contract;
import com.battja.accounting.entities.Fee;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.services.FeeService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.CustomDetailsForm;
import com.battja.accounting.vaadin.components.EditSaveCancelButtonsLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.components.NotificationWithCloseButton;
import com.battja.accounting.vaadin.entryforms.CreateFeeForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="contract-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Contract Details")
public class ContractDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(ContractDetailsView.class);

    private Integer contractId;
    private Contract contract;
    private final FeeService feeService;

    public ContractDetailsView(FeeService feeService) {
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
        removeAll();
        contract = feeService.getContract(contractId);
        if (contract != null) {
            add(new H3("Contract: " + contract.getContractName()));
            add(createDetailsView());
            add(new EditSaveCancelButtonsLayout<>(form, contract, this::saveContract));
            Collection<Fee> feeLines= feeService.getFeeLines(contract);
            if (feeLines != null) {
                add(new H4("Fees"));
                add(GridCreator.createFeeGrid(feeLines));
                Button createFeeButton = new Button("Create Fee Line");
                createFeeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                createFeeButton.addClickListener(buttonClickEvent -> createFeeButton.getUI().ifPresent(
                        ui -> ui.navigate(CreateFeeForm.class,String.valueOf(contractId))));
                add(createFeeButton);
            }
        }
    }

    private CustomDetailsForm form;
    private TextField contractNameField;

    private CustomDetailsForm createDetailsView() {
        form = new CustomDetailsForm();
        contractNameField = form.addEditableField("Name", contract.getContractName());
        form.addField("Start Date",contract.getStartDate().toString());
        return form;
    }

    private boolean saveContract(Contract contract) {
        contract.setContractName(contractNameField.getValue());
        try {
            Contract updatedContract = feeService.updateContract(contract);
            if (updatedContract == null) {
                NotificationWithCloseButton notification = new NotificationWithCloseButton("Unable to update contract",false);
                notification.open();
                return false;
            } else {
                NotificationWithCloseButton notification = new NotificationWithCloseButton("Contract updated",true);
                notification.open();
                updateView();
                return true;
            }
        } catch (DuplicateNameException e) {
            NotificationWithCloseButton notification = new NotificationWithCloseButton(e.getMessage(),false);
            notification.open();
        }
        return false;
    }


}

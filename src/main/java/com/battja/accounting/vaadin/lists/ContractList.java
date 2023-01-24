package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Contract;
import com.battja.accounting.services.FeeService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.entryforms.CreateContractForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Collection;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="contracts", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Contracts")
public class ContractList extends VerticalLayout {

    private final FeeService feeService;

    public ContractList(FeeService feeService) {
        this.feeService = feeService;
        updateView();
    }

    private void updateView() {
        removeAll();
        add(new H4("Contracts"));
        Collection<Contract> contracts = feeService.listAllContracts();
        add(GridCreator.createContractsGrid(contracts));
        add(createButtonsLayout());
    }

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        Button createContract = new Button("New Contract");
        createContract.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createContract.addClickListener(buttonClickEvent -> createContract.getUI().ifPresent(ui -> ui.navigate(CreateContractForm.class)));
        layout.add(createContract);
        return layout;
    }

}

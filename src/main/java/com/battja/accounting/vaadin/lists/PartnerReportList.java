package com.battja.accounting.vaadin.lists;

import com.battja.accounting.services.PartnerReportService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.battja.accounting.vaadin.entryforms.SimulatedPartnerReportForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="partner-reports", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Partner Reports")
public class PartnerReportList extends VerticalLayout {

    private final PartnerReportService partnerReportService;

    public PartnerReportList(PartnerReportService partnerReportService) {
        this.partnerReportService = partnerReportService;
        updateView();
    }

    private void updateView() {
        add(new H4("Partner Reports"));
        add(GridCreator.createPartnerReportGrid(partnerReportService.listAll()));
        Button createPartnerReport = new Button("Create Partner Report (simulation)");
        createPartnerReport.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPartnerReport.addClickListener(buttonClickEvent -> createPartnerReport.getUI().ifPresent(
                ui -> ui.navigate(SimulatedPartnerReportForm.class)));
        add(createPartnerReport);
    }


}

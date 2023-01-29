package com.battja.accounting.vaadin.details;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.PartnerReport;
import com.battja.accounting.entities.ReportLine;
import com.battja.accounting.services.PartnerReportService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.CustomDetailsForm;
import com.battja.accounting.vaadin.components.GridCreator;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="partner-report-details", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Partner Report Details")
public class PartnerReportDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    final static Logger log = LoggerFactory.getLogger(PartnerReportDetailsView.class);

    private final PartnerReportService partnerReportService;
    private Integer partnerReportId;
    private PartnerReport partnerReport;

    public PartnerReportDetailsView(PartnerReportService partnerReportService) {
        this.partnerReportService = partnerReportService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String parameter) {
        try {
            partnerReportId = Integer.valueOf(parameter);
            updateView();
        } catch (NumberFormatException e) {
            log.warn("Invalid parameter: " + parameter);
        }
    }

    private void updateView() {
        removeAll();
        partnerReport = partnerReportService.getPartnerReport(partnerReportId);
        if (partnerReport != null) {
            add(new H3("Partner Report: " + partnerReport.getReference()));
            add(createDetailsView());
            Collection<ReportLine> reportLines = partnerReport.getReportLines();
            if (!reportLines.isEmpty()) {
                add(new H4("Report Lines"));
                add(GridCreator.createReportLineGrid(reportLines));
            }
        }
    }

    private CustomDetailsForm createDetailsView() {
        CustomDetailsForm form = new CustomDetailsForm();
        form.addField("Partner", partnerReport.getPartner() != null ? partnerReport.getPartner().getAccountName() : "");
        form.addField("Created Date", partnerReport.getCreatedDate() != null ? partnerReport.getCreatedDate().toString() : "");
        form.addField("Reference", partnerReport.getReference());
        form.addField("Status", partnerReport.getReportStatus() != null ? partnerReport.getReportStatus().toString() : "");
        return form;
    }

}

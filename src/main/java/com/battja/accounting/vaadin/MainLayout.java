package com.battja.accounting.vaadin;

import com.battja.accounting.vaadin.lists.AccountList;
import com.battja.accounting.vaadin.lists.BatchList;
import com.battja.accounting.vaadin.lists.PaymentsList;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }


    private void createHeader() {
        H1 logo = new H1("BattjaPay");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink accountList = new RouterLink("Accounts", AccountList.class);
        accountList.setHighlightCondition(HighlightConditions.sameLocation());
        addToDrawer(new VerticalLayout(accountList));
        RouterLink batchList = new RouterLink("Batches", BatchList.class);
        batchList.setHighlightCondition(HighlightConditions.sameLocation());
        addToDrawer(new VerticalLayout(batchList));
        RouterLink transactionsList = new RouterLink("Payments", PaymentsList.class);
        transactionsList.setHighlightCondition(HighlightConditions.sameLocation());
        addToDrawer(new VerticalLayout(transactionsList));
    }

}

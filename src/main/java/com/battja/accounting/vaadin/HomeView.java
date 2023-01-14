package com.battja.accounting.vaadin;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H4("Home"));
        Div div = new Div();
        Paragraph p1 = new Paragraph("Welcome to "+BRAND_NAME+".\n\n");
        Paragraph p2 = new Paragraph(BRAND_NAME+" is a demo for double entry bookkeeping.");
        Paragraph p3 = new Paragraph("You can navigate to the different menu's on the left to find Accounts, Batches and Payments.");
        Paragraph p4 = new Paragraph("Whenever we create a Payment or change a Payment's state, this event will be booked in different Batches using a Journal. " +
                "Batches are created for each Account and divided into Registers and you are able to navigate through the different batches. " +
                "Batches can also have an end date (e.g. a typical use case would be for a batch to end by end-of-day), which make sure that new events will be booked in new batches.");
        Paragraph p5 = new Paragraph("As long as a Batch is not balanced, it cannot close. Typically this means that we still expect a certain event, " +
                "e.g. an authorised payment needs to be captured or to expire for the Authorised register to balance out. " +
                "Or a captured payment expects settlement before the associated Capture batch can be closed.");
        Paragraph p6 = new Paragraph("Please go ahead and create or change payments. " +
                "You'll be able to navigate through the associated Journals, Bookings and Batches to understand what is happening within the double entry bookkeeping");
        div.add(p1);
        div.add(p2);
        div.add(p3);
        div.add(p4);
        div.add(p5);
        div.add(p6);
        add(div);
    }
}

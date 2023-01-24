package com.battja.accounting.vaadin.lists;

import com.battja.accounting.entities.Batch;
import com.battja.accounting.services.BatchService;
import com.battja.accounting.vaadin.MainLayout;
import com.battja.accounting.vaadin.components.GridCreator;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

import static com.battja.accounting.vaadin.MainLayout.BRAND_NAME;

@Route(value="batches", layout = MainLayout.class)
@PageTitle(BRAND_NAME + " | Batches")
public class BatchList extends VerticalLayout {

    private final BatchService batchService;

    public BatchList(BatchService batchService) {
        this.batchService = batchService;
        updateView();
    }

    private void updateView(){
        List<Batch> batches = batchService.listAll();
        add(GridCreator.createBatchesGrid(batches));
    }

}

package com.battja.accounting.vaadin.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.Function;

public class EditSaveCancelButtonsLayout<T> extends HorizontalLayout {

    private final Button editButton;
    private final Button saveButton;
    private final Button deleteButton;
    private final Button cancelButton;
    private final CustomDetailsForm form;

    public EditSaveCancelButtonsLayout(CustomDetailsForm form, T t, Function<T, Boolean> saveMethod) {
        this(form, t, saveMethod, null);
    }

    public EditSaveCancelButtonsLayout(CustomDetailsForm form, T t, Function<T, Boolean> saveMethod, Function<T, Boolean> deleteMethod) {
        this.form = form;
        editButton = new Button("Edit");
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(buttonClickEvent -> edit());
        add(editButton);
        cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(buttonClickEvent -> cancel());
        cancelButton.setVisible(false);
        add(cancelButton);
        saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(buttonClickEvent -> save(t, saveMethod));
        saveButton.setVisible(false);
        add(saveButton);
        deleteButton = new Button("Delete");
        if (deleteMethod != null) {
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(buttonClickEvent -> delete(t, deleteMethod));
            deleteButton.setVisible(true);
            add(deleteButton);
        }
    }

    private void edit() {
        form.setEditable(true);
        editButton.setVisible(false);
        cancelButton.setVisible(true);
        saveButton.setVisible(true);
        deleteButton.setVisible(false);
    }

    private void cancel() {
        form.setEditable(false);
        editButton.setVisible(true);
        cancelButton.setVisible(false);
        saveButton.setVisible(false);
        deleteButton.setVisible(true);
    }

    private void save(T t, Function<T, Boolean> method) {
        if(method.apply(t)) {
            form.setEditable(false);
            editButton.setVisible(true);
            cancelButton.setVisible(false);
            saveButton.setVisible(false);
            deleteButton.setVisible(true);
        }
    }

    private void delete(T t, Function<T, Boolean> method) {
        if(method.apply(t)) {
            form.setEditable(false);
            editButton.setVisible(true);
            cancelButton.setVisible(false);
            saveButton.setVisible(false);
            deleteButton.setVisible(true);
        }
    }

}

package com.battja.accounting.vaadin.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.HasUrlParameter;

import java.util.Collection;

public class CustomDetailsForm extends FormLayout {

    public TextField addField(String label, String value) {
        TextField field = new TextField();
        field.setLabel(label);
        field.setValue(value);
        field.setReadOnly(true);
        add(field);
        return field;
    }

    public <T extends DisplayableEntity> Select<T> addSelectableField(String label, T value, Collection<T> items) {
        Select<T> field = new Select<>();
        field.setLabel(label);
        field.setItems(items);
        field.setRenderer(new TextRenderer<>(DisplayableEntity::getDisplayName));
        field.setValue(value);
        field.setReadOnly(true);
        field.setClassName("allow-edits");
        add(field);
        return field;
    }

    public TextField addEditableField(String label, String value) {
        TextField field = new TextField();
        field.setLabel(label);
        field.setValue(value);
        field.setReadOnly(true);
        field.setClassName("allow-edits");
        add(field);
        return field;
    }

    public <T, C extends Component & HasUrlParameter<T>> void addClickableField(String label, String value, Class<? extends C> navigationTarget, T parameter) {
        TextField field = new TextField();
        field.setLabel(label);
        field.setValue(value);
        field.setReadOnly(true);
        field.addFocusListener(textFieldFocusEvent -> field.getUI().ifPresent(
                ui -> ui.navigate(navigationTarget,parameter)
        ));
        add(field);
    }

    public void setEditable(boolean editable) {
        for (Component childView : this.getChildren().toList()) {
            if (childView instanceof TextField) {
                if (((TextField) childView).getClassName() != null && ((TextField) childView).getClassName().contains("allow-edits")) {
                    ((TextField) childView).setReadOnly(!editable);
                }
            }
            if (childView instanceof Select) {
                if (((Select<?>) childView).getClassName() != null && ((Select<?>) childView).getClassName().contains("allow-edits")) {
                    ((Select<?>) childView).setReadOnly(!editable);
                }
            }
        }
    }

}

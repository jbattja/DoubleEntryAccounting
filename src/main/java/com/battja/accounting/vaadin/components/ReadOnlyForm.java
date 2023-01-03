package com.battja.accounting.vaadin.components;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

public class ReadOnlyForm extends FormLayout {

    public void addField(String label, String value) {
        TextField accountName = new TextField();
        accountName.setLabel(label);
        accountName.setValue(value);
        accountName.setReadOnly(true);
        add(accountName);
    }

}

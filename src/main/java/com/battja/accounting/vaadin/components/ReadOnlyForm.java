package com.battja.accounting.vaadin.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasUrlParameter;

public class ReadOnlyForm extends FormLayout {

    public void addField(String label, String value) {
        TextField accountName = new TextField();
        accountName.setLabel(label);
        accountName.setValue(value);
        accountName.setReadOnly(true);
        add(accountName);
    }

    public <T, C extends Component & HasUrlParameter<T>> void addClickableField(String label, String value, Class<? extends C> navigationTarget, T parameter) {
        TextField accountName = new TextField();
        accountName.setLabel(label);
        accountName.setValue(value);
        accountName.setReadOnly(true);
        accountName.addFocusListener(textFieldFocusEvent -> accountName.getUI().ifPresent(
                ui -> ui.navigate(navigationTarget,parameter)
        ));
        add(accountName);
    }


}

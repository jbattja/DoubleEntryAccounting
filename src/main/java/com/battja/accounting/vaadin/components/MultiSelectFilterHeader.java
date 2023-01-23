package com.battja.accounting.vaadin.components;

import com.battja.accounting.entities.Account;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBoxVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextFieldVariant;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class MultiSelectFilterHeader<T extends MultiSelectFilterable> extends VerticalLayout {

    public MultiSelectFilterHeader(Consumer<Set<T>> filterChangeConsumer, Collection<T> items) {
        MultiSelectComboBox<T> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(items);
        comboBox.setClearButtonVisible(true);
        comboBox.setItemLabelGenerator(MultiSelectFilterable::getFilterName);
        comboBox.addThemeVariants(MultiSelectComboBoxVariant.LUMO_SMALL);
        comboBox.setWidthFull();
        comboBox.getStyle().set("max-width", "100%");
        comboBox.setPlaceholder("Search");
        comboBox.addValueChangeListener(e -> filterChangeConsumer.accept(e.getValue()));
        add(comboBox);
        getThemeList().clear();
        getThemeList().add("spacing-xs");
    }
}

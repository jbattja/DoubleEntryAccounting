package com.battja.accounting.entities;

import com.battja.accounting.vaadin.components.MultiSelectFilterable;

public enum RegisterType implements MultiSelectFilterable {

    RECEIVED(true),
    AUTHORISED(true),
    CAPTURED(true),
    PAYABLE(false),
    FEES(true),
    REVENUE(false),
    EARLY_SETTLEMENT(false),
    PAYOUTS(true);

    private final boolean requiresEntryReconciliation;

    RegisterType(boolean requiresEntryReconciliation) {
        this.requiresEntryReconciliation = requiresEntryReconciliation;
    }

    public boolean requiresEntryReconciliation() {
        return requiresEntryReconciliation;
    }

    @Override
    public String getFilterName() {
        return this.name();
    }
}

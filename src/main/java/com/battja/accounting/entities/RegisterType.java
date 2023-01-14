package com.battja.accounting.entities;

public enum RegisterType {

    RECEIVED(true),
    AUTHORISED(true),
    CAPTURED(true),
    PAYABLE(false);

    private final boolean requiresEntryReconciliation;

    RegisterType(boolean requiresEntryReconciliation) {
        this.requiresEntryReconciliation = requiresEntryReconciliation;
    }

    public boolean requiresEntryReconciliation() {
        return requiresEntryReconciliation;
    }
}

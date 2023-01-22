package com.battja.accounting.entities;

public enum RegisterType {

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
}

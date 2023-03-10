package com.battja.accounting.entities;

import com.battja.accounting.util.CommonUtil;
import com.battja.accounting.vaadin.components.DisplayableEntity;

public enum PaymentMethod implements DisplayableEntity {

    VISA, MASTERCARD, OVO, GRABPAY, GCASH, ALFAMART;

    @Override
    public String toString() {
        return CommonUtil.enumNameToString(this.name());
    }

    @Override
    public String getDisplayName() {
        return this.toString();
    }
}

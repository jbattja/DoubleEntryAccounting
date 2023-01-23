package com.battja.accounting.entities;

import com.battja.accounting.util.CommonUtil;
import com.battja.accounting.vaadin.components.MultiSelectFilterable;

public enum PaymentMethod implements MultiSelectFilterable {

    VISA, MASTERCARD, OVO, GRABPAY, GCASH, ALFAMART;

    @Override
    public String toString() {
        return CommonUtil.enumNameToString(this.name());
    }

    @Override
    public String getFilterName() {
        return this.toString();
    }
}

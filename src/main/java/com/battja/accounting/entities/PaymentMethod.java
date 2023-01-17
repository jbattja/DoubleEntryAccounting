package com.battja.accounting.entities;

import com.battja.accounting.util.CommonUtil;

public enum PaymentMethod {

    VISA, MASTERCARD, OVO, GRABPAY, GCASH, ALFAMART;


    @Override
    public String toString() {
        return CommonUtil.enumNameToString(this.name());
    }

}

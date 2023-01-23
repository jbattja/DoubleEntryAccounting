package com.battja.accounting.entities;

import com.battja.accounting.vaadin.components.MultiSelectFilterable;

public class Amount {

    public enum Currency implements MultiSelectFilterable

    {IDR,USD,SGD,MYR,EUR,PHP,THB;

        @Override
        public String getFilterName() {
            return this.name();
        }
    }

    private Currency currency;
    private Long value;

    protected Amount() {}

    public Amount(Currency currency, long value) {
        this.currency = currency;
        this.value = value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format(
                "Amount[value=%d, currency='%s']",
                value, currency);
    }

}

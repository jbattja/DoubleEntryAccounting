package com.battja.accounting.entities;

public class Amount {

    private String currency;
    private Long value;

    protected Amount() {}

    public Amount(String currency, long value) {
        this.currency = currency;
        this.value = value;
    }

    public String getCurrency() {
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

package com.battja.accounting.entities;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

@Entity
public class Route {

    public enum RoutingType {PAYMENT,SETTLEMENT,PAYOUT}

    @Id
    @GeneratedValue
    private Integer id;
    @Enumerated(EnumType.STRING)
    private RoutingType routingType;
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Account merchant;
    @Enumerated(EnumType.STRING)
    private Amount.Currency currency;
    private PaymentMethod paymentMethod;
    @ManyToOne
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;

    protected Route() {}

    public Route(@NonNull RoutingType routingType, Account merchant, Amount.Currency currency, PaymentMethod paymentMethod, @NonNull Account targetAccount) {
        this.routingType = routingType;
        this.merchant = merchant;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.targetAccount = targetAccount;
    }

    @Override
    public String toString() {
        return String.format(
                "Route[id=%d, merchant='%s', currency='%s', paymentMethod='%s', targetAccount='%s']",
                id, merchant.getAccountName(), currency, paymentMethod, targetAccount.getAccountName());
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RoutingType getRoutingType() {
        return routingType;
    }

    public void setRoutingType(RoutingType routingType) {
        this.routingType = routingType;
    }

    public Account getMerchant() {
        return merchant;
    }

    public void setMerchant(Account merchant) {
        this.merchant = merchant;
    }

    public Amount.Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Amount.Currency currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(Account targetAccount) {
        this.targetAccount = targetAccount;
    }
}

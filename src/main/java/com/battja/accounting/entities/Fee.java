package com.battja.accounting.entities;

import com.battja.accounting.events.EventType;
import jakarta.persistence.*;

@Entity
public class Fee {

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String currency;
    private long fixedAmount;
    private int basisPoints;

    public Fee() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(long fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public int getBasisPoints() {
        return basisPoints;
    }

    public void setBasisPoints(int basisPoints) {
        this.basisPoints = basisPoints;
    }
}

package com.battja.accounting.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Date;
import java.util.List;

@Entity
public class Contract {

    public static final String DEFAULT_MERCHANT_CONTRACT_NAME = "defaultMerchantContract";

    @Id
    @GeneratedValue
    private Integer id;
    private String contractName;
    private Date startDate;
    private Date endDate;
    @OneToMany(mappedBy = "contract")
    private List<Fee> fees;

    public Contract() {
    }

    public Contract(String contractName) {
        this.contractName = contractName;
        this.startDate = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }
}

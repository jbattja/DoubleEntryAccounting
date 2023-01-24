package com.battja.accounting.entities;

import com.battja.accounting.vaadin.components.DisplayableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Date;
import java.util.List;

@Entity
public class Contract implements DisplayableEntity {

    public static final String DEFAULT_MERCHANT_CONTRACT_NAME = "defaultMerchantContract";

    @Id
    @GeneratedValue
    private Integer id;
    private String contractName;
    private Date startDate;
    @OneToMany(mappedBy = "contract")
    private List<Fee> fees;

    public Contract() {
    }

    public Contract(String contractName) {
        this.contractName = contractName;
        this.startDate = new Date();
    }

    @Override
    public String toString() {
        return String.format(
                "Contract[id=%s, contractName='%s']",
                id, contractName);
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (id != null) {
            result = 31 * id.hashCode() + result;
        }
        if (contractName != null) {
            result = 31 * contractName.hashCode() + result;
        }
        if (startDate != null) {
            result = 31 * startDate.hashCode() + result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Contract other)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.contractName == null) {
            if (other.getContractName() != null) {
                return false;
            }
        } else if (!this.contractName.equals(other.getContractName())) {
            return false;
        }
        if (this.startDate == null) {
            if (other.startDate != null) {
                return false;
            }
        } else if (!this.startDate.equals(other.getStartDate())) {
            return false;
        }
        return this.id.equals(other.getId());
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

    public List<Fee> getFees() {
        return fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    @Override
    public String getDisplayName() {
        return this.getContractName();
    }
}

package com.battja.accounting.entities;

import com.battja.accounting.util.CommonUtil;
import jakarta.persistence.*;

@Entity
public class Transaction {

    public enum TransactionType {PAYMENT, CAPTURE, REFUND, CHARGEBACK;

        @Override
        public String toString() {
            return CommonUtil.enumNameToString(this.name());
        }
    }

    @Id
    @GeneratedValue
    private Integer id;
    private String transactionReference;
    private String originalReference;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @ManyToOne
    private Account merchantAccount;
    @ManyToOne
    private Account partnerAccount;
    private Long amount;
    private String currency;
    private String status;

    public Transaction() {}

    @Override
    public String toString() {
        return String.format(
                "'%s'[id=%d, transactionReference='%s', modificationReference='%s', merchantAccount='%s', acquirerAccount='%s', amount='%s' '%d']",
                type, id, transactionReference, originalReference, merchantAccount.getAccountName(), partnerAccount.getAccountName(), currency, amount);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getOriginalReference() {
        return originalReference;
    }

    public void setOriginalReference(String originalReference) {
        this.originalReference = originalReference;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Account getMerchantAccount() {
        return merchantAccount;
    }

    public void setMerchantAccount(Account merchantAccount) {
        this.merchantAccount = merchantAccount;
    }

    public Account getPartnerAccount() {
        return partnerAccount;
    }

    public void setPartnerAccount(Account partnerAccount) {
        this.partnerAccount = partnerAccount;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

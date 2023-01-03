package com.battja.accounting.entities;

import com.battja.accounting.journals.Amount;
import com.battja.accounting.util.CommonUtil;
import jakarta.persistence.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

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
    private String modificationReference;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @ManyToOne
    private Account merchantAccount;
    @ManyToOne
    private Account acquirerAccount;
    private Long amount;
    private String currency;

    public Transaction() {}

    @Override
    public String toString() {
        return String.format(
                "'%s'[id=%d, transactionReference='%s', modificationReference='%s', merchantAccount='%s', acquirerAccount='%s', amount='%s' '%d']",
                type, id, transactionReference, modificationReference, merchantAccount.getAccountName(), acquirerAccount.getAccountName(), currency, amount);
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

    public String getModificationReference() {
        return modificationReference;
    }

    public void setModificationReference(String modificationReference) {
        this.modificationReference = modificationReference;
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

    public Account getAcquirerAccount() {
        return acquirerAccount;
    }

    public void setAcquirerAccount(Account acquirerAccount) {
        this.acquirerAccount = acquirerAccount;
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

}

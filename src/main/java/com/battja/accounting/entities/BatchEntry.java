package com.battja.accounting.entities;

import com.battja.accounting.journals.Amount;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class BatchEntry {

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;
    @ManyToMany
    @JoinTable(
            name = "journals_to_batch_entries",
            joinColumns = @JoinColumn(name = "batch_entry_id"),
            inverseJoinColumns = @JoinColumn(name = "journal_id"))
    private Set<Journal> journals;
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    private String currency;
    private Long originalAmount;
    private Long openAmount;

    protected BatchEntry() {}

    public BatchEntry(Batch batch, Set<Journal> journals, Transaction transaction, Amount amount) {
        this.batch = batch;
        this.journals = journals;
        this.transaction = transaction;
        this.openAmount = amount.getValue();
        this.originalAmount = amount.getValue();
        this.currency = amount.getCurrency();
    }

    @Override
    public String toString() {
        return String.format(
                "BatchEntry[id=%d, batch='%s', transaction='%s', originalAmount='%s' '%d', openAmount='%s' '%d']",
                id, batch, transaction, currency, originalAmount, currency, openAmount);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Set<Journal> getJournals() {
        return journals;
    }

    public void setJournals(Set<Journal> journals) {
        this.journals = journals;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(Long originalAmount) {
        this.originalAmount = originalAmount;
    }

    public Long getOpenAmount() {
        return openAmount;
    }

    public void setOpenAmount(Long openAmount) {
        this.openAmount = openAmount;
    }
}

package com.battja.accounting.entities;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

@Entity
public class Booking {

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "journal_id")
    private Journal journal;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @Enumerated(EnumType.STRING)
    private RegisterType register;
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;
    private Long amount;
    @Enumerated(EnumType.STRING)
    private Amount.Currency currency;
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    protected Booking() {}

    public Booking(@NonNull Account account, @NonNull RegisterType register, @NonNull Long amount, @NonNull Amount.Currency currency,
                   Batch batch, Transaction transaction) {
        this.account = account;
        this.register = register;
        this.amount = amount;
        this.currency = currency;
        this.batch = batch;
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return String.format(
                "Booking[id=%d, accountName='%s', register='%s', batchNumber='%d', journal='%s', amount='%s' '%d']",
                id, account.getAccountName(), register, batch.getBatchNumber(), journal.getEventType(), currency, amount);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public RegisterType getRegister() {
        return register;
    }

    public void setRegister(RegisterType register) {
        this.register = register;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Amount.Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Amount.Currency currency) {
        this.currency = currency;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }


}

package com.battja.accounting.entities;

import com.battja.accounting.util.CommonUtil;
import jakarta.persistence.*;

@Entity
public class Account {

    public final static Integer DEFAULT_PSP_ACCOUNT_ID = 1;
    public final static String DEFAULT_PSP_ACCOUNT_NAME = "BattjaPay";

    public enum AccountType {
        PSP(null), ACQUIRER(PSP), ACQUIRER_ACCOUNT(ACQUIRER), COMPANY(PSP), MERCHANT(COMPANY), BANK(PSP), BANK_ACCOUNT(BANK);
        final AccountType parent;
        AccountType(AccountType parent) {
            this.parent =parent;
        }

        @Override
        public String toString() {
            return CommonUtil.enumNameToString(this.name());
        }

        public AccountType getParent() {
            return parent;
        }
    }

    @Id
    @GeneratedValue
    private Integer id;
    private String accountName;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Account parent;

    protected Account() {}

    public Account(String accountName, AccountType accountType) {
        this.accountName = accountName;
        this.accountType = accountType;
        if (this.accountType.parent == AccountType.PSP) {
            this.parent = new Account(DEFAULT_PSP_ACCOUNT_NAME,AccountType.PSP,null);
            this.parent.id = DEFAULT_PSP_ACCOUNT_ID;
        } else {
            throw new IllegalArgumentException("Account needs to have a parent");
        }
    }

    public Account(String accountName, AccountType accountType, Account parent) {
        this.accountName = accountName;
        this.accountType = accountType;
        this.parent = parent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Account getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return String.format(
                "Account[id=%s, accountName='%s', AccountType='%s']",
                id, accountName, accountType);
    }

}

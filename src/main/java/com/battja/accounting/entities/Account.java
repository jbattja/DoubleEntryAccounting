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
    }

    @Id
    @GeneratedValue
    private Integer id;
    private String accountName;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private Integer parentId;

    protected Account() {}

    public Account(String accountName, AccountType accountType) {
        this.accountName = accountName;
        this.accountType = accountType;
        if (this.accountType.parent == AccountType.PSP) {
            this.parentId = DEFAULT_PSP_ACCOUNT_ID;
        } else {
            throw new IllegalArgumentException("Account needs to have a parent");
        }
    }

    public Account(String accountName, AccountType accountType, Account parent) {
        this.accountName = accountName;
        this.accountType = accountType;
        this.setParentId(parent);
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Account parent) {
        if (parent == null && this.accountType == AccountType.PSP) {
            return;
        }
        if (parent == null) {
            throw new IllegalArgumentException("Account needs to have a parent");
        }
        if (parent.accountType != this.accountType.parent) {
            throw new IllegalArgumentException("Incorrect Account Type");
        }
        this.parentId = parent.getId();
    }

    @Override
    public String toString() {
        return String.format(
                "Account[id=%s, accountName='%s', AccountType='%s', parentId='%s']",
                id, accountName, accountType, parentId);
    }

}

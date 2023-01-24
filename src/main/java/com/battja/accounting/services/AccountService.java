package com.battja.accounting.services;

import com.battja.accounting.entities.Account;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.repositories.AccountRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public Account getAccount(@NonNull Integer accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public Account getAccount(@NonNull String accountName) {
        List<Account> accountList = accountRepository.findByAccountName(accountName);
        if (accountList.isEmpty()) {
            return null;
        }
        if (accountList.size() > 1) {
            log.warn("More than 1 account with name " + accountName);
        }
        return accountList.get(0);
    }

    public List<Account> listAll() {
        return accountRepository.findAll();
    }

    public List<Account> listChildren(Account parent) {
        return accountRepository.findByParent(parent);
    }

    public List<Account> listAccountsByType(Account.AccountType type) {
        return accountRepository.findByAccountType(type);
    }

    public List<Account> listAvailableParentAccounts(Account.AccountType type) {
        Set<Account.AccountType> accountTypes = new HashSet<>();
        if (type == null) {
            for (Account.AccountType accountType : Account.AccountType.values()) {
                accountTypes.add(accountType.getParent());
            }
        } else {
            accountTypes.add(type.getParent());
        }
        return accountRepository.findByAccountTypeIn(accountTypes.stream().toList());
    }

    @Transactional
    public Account createAccount(@NonNull Account account) throws DuplicateNameException {
        if (account.getAccountName() == null) {
            log.warn("Tried to create an account without specifying account name");
            return null;
        }
        if (getAccount(account.getAccountName()) != null) {
            log.warn("Account with name " + account.getAccountName() + " already exists");
            throw new DuplicateNameException("Account with name " + account.getAccountName() + " already exists");
        }
        account = accountRepository.save(account);
        log.info("Created account:" + account);
        return account;
    }

    @Transactional
    public Account updateAccount(@NonNull Account account) throws DuplicateNameException {
        if (account.getAccountName() == null) {
            log.warn("Tried to update an account without specifying account name");
            return null;
        }
        Account existingAccount = getAccount(account.getId());
        if (existingAccount == null) {
            log.warn("Cannot update account: existing account not found");
            return null;
        }
        if (!existingAccount.getAccountName().equals(account.getAccountName())) {
            if (getAccount(account.getAccountName()) != null) {
                log.warn("Cannot update account: account with name " + account.getAccountName() + " already exists");
                throw new DuplicateNameException("Account with name " + account.getAccountName() + " already exists");
            }
        }
        existingAccount.setAccountName(account.getAccountName());
        if (account.getContract() != null && !account.getContract().equals(existingAccount.getContract())) {
            existingAccount.setContract(account.getContract());
        }
        accountRepository.save(existingAccount);
        log.info("Account updated:" + existingAccount);
        return existingAccount;
    }

    @Autowired
    AccountRepository accountRepository;

}

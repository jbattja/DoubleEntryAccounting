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

import java.util.List;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public Account getAccount(@NonNull Integer accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public Account getParent(@NonNull Account account) {
        if (account.getParentId() == null) {
            return null;
        }
        return accountRepository.findById(account.getParentId()).orElse(null);
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

    public List<Account> listMerchants() {
        return accountRepository.findByAccountType(Account.AccountType.MERCHANT);
    }

    public List<Account> listAcquirerAccounts() {
        return accountRepository.findByAccountType(Account.AccountType.ACQUIRER_ACCOUNT);
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
        return accountRepository.save(account);
    }

    @Autowired
    AccountRepository accountRepository;

}

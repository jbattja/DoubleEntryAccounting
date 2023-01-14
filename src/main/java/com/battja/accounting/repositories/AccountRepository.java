package com.battja.accounting.repositories;


import com.battja.accounting.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account,Integer> {

    List<Account> findByAccountName(String accountName);
    List<Account> findByAccountType(Account.AccountType accountType);
    List<Account> findByParent(Account parent);

}

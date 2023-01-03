package com.battja.accounting.accounts;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.journals.Amount;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.BookingService;
import com.battja.accounting.services.TransactionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InitialSetup {

    final static Logger log = LoggerFactory.getLogger(InitialSetup.class);

    /**
    To reset the database:
     - drop all db tables or;
        - set 'spring.jpa.hibernate.ddl-auto' to 'create-drop' and restart once
        - set 'spring.jpa.hibernate.ddl-auto' back to 'update' afterwards
     - run this method
     */
    @Transactional
    public void init() {
        log.info("doing init for all accounts");

        try {
            // SETUP Default PSP
            Account pspAccount = new Account(Account.DEFAULT_PSP_ACCOUNT_NAME, Account.AccountType.PSP, null);
            pspAccount.setId(Account.DEFAULT_PSP_ACCOUNT_ID);
            pspAccount = accountService.createAccount(pspAccount);
            if (!Objects.equals(pspAccount.getId(), Account.DEFAULT_PSP_ACCOUNT_ID)) {
                throw new IllegalArgumentException("PSP Account should be number 1");
            }

            // SETUP Company Facebook
            Account fb = new Account("Facebook", Account.AccountType.COMPANY);
            fb = accountService.createAccount(fb);
            Account fbGames = accountService.createAccount(new Account("Facebook_Games", Account.AccountType.MERCHANT, fb));
            Account fbAds = accountService.createAccount(new Account("Facebook_Ads", Account.AccountType.MERCHANT, fb));

            // SETUP Company Netflix
            Account netflix = new Account("Netflix", Account.AccountType.COMPANY);
            netflix = accountService.createAccount(netflix);
            Account nfId = accountService.createAccount(new Account("Netflix_Indonesia", Account.AccountType.MERCHANT, netflix));
            Account nfPh = accountService.createAccount(new Account("Netflix_Philippines", Account.AccountType.MERCHANT, netflix));

            // SETUP Acquirers OVO, GCash, Visa, MC
            Account ovo = accountService.createAccount(new Account("Ovo", Account.AccountType.ACQUIRER));
            Account gcash = accountService.createAccount(new Account("Gcash", Account.AccountType.ACQUIRER));
            Account visa = accountService.createAccount(new Account("Visa", Account.AccountType.ACQUIRER));
            Account mc = accountService.createAccount(new Account("Mastercard", Account.AccountType.ACQUIRER));

            Account ovoAggr = accountService.createAccount(new Account("OVO_Aggr", Account.AccountType.ACQUIRER_ACCOUNT, ovo));
            Account gcashAggr = accountService.createAccount(new Account("GCASH_Aggr", Account.AccountType.ACQUIRER_ACCOUNT, gcash));
            Account visaIdNf= accountService.createAccount(new Account("VISA_ID_Netflix", Account.AccountType.ACQUIRER_ACCOUNT, visa));
            Account visaIdFb = accountService.createAccount(new Account("VISA_ID_Facebook", Account.AccountType.ACQUIRER_ACCOUNT, visa));
            Account visaPhNf = accountService.createAccount(new Account("VISA_PH_Netflix", Account.AccountType.ACQUIRER_ACCOUNT, visa));
            Account visaPhFb = accountService.createAccount(new Account("VISA_PH_Facebook", Account.AccountType.ACQUIRER_ACCOUNT, visa));
            Account mcId = accountService.createAccount(new Account("MC_ID", Account.AccountType.ACQUIRER_ACCOUNT, mc));
            Account mcPh = accountService.createAccount(new Account("MC_PH", Account.AccountType.ACQUIRER_ACCOUNT, mc));

            log.info("Creating a couple of transactions");
            Transaction transaction1 = transactionService.newPayment(new Amount("IDR",100000L),nfId,ovoAggr);
            Transaction transaction2 = transactionService.newPayment(new Amount("PHP",200L),nfPh,visaPhFb);
            Transaction transaction3 = transactionService.newPayment(new Amount("IDR",400000L),fbGames,visaIdFb);
            Transaction transaction4 = transactionService.newPayment(new Amount("PHP",300L),fbGames,gcashAggr);

            log.info("Creating some bookings");
            bookingService.book(transaction1, EventType.REFUSED);
            bookingService.book(transaction2, EventType.AUTHORISED);
            bookingService.book(transaction3, EventType.AUTHORISED);
            bookingService.book(transaction4, EventType.AUTHORISED);
        } catch (DuplicateNameException e) {
            log.error("Error during initial setup: " + e.getMessage());
        }

    }

    @Autowired
    AccountService accountService;
    @Autowired
    BookingService bookingService;
    @Autowired
    TransactionService transactionService;

}

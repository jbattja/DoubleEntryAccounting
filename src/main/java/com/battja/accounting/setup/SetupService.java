package com.battja.accounting.setup;

import com.battja.accounting.entities.*;
import com.battja.accounting.events.EventType;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.exceptions.DuplicateNameException;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.FeeService;
import com.battja.accounting.services.RoutingService;
import com.battja.accounting.services.TransactionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SetupService {

    final static Logger log = LoggerFactory.getLogger(SetupService.class);

    @Value("${setup.createdemodata}")
    boolean createDemoData;

    @Transactional
    public void init() {
        if (firstSetup()) {
            setupInitialData();
            if (createDemoData) {
                setupDemoAccounts();
                try {
                    createDemoTransactions(100);
                } catch (BookingException e) {
                    log.error("Error while creating demo transactions: " + e.getMessage());
                }
            }
        }
    }

    private boolean firstSetup() {
        return (accountService.getAccount(Account.DEFAULT_PSP_ACCOUNT_ID) == null) ;
    }


    @Transactional
    private void setupInitialData() {
        log.info("doing initial setup");

        try {
            // SETUP Default PSP
            Account pspAccount = new Account(Account.DEFAULT_PSP_ACCOUNT_NAME, Account.AccountType.PSP, null);
            pspAccount.setId(Account.DEFAULT_PSP_ACCOUNT_ID);
            pspAccount = accountService.createAccount(pspAccount);
            if (!Objects.equals(pspAccount.getId(), Account.DEFAULT_PSP_ACCOUNT_ID)) {
                throw new IllegalArgumentException("PSP Account should be number 1");
            }

            Contract defaultMerchantContract = new Contract(Contract.DEFAULT_MERCHANT_CONTRACT_NAME);
            List<Fee> fees = new ArrayList<>();
            for (PaymentMethod paymentMethod : PaymentMethod.values()) {
                Fee fee = new Fee();
                fee.setPaymentMethod(paymentMethod);
                fee.setEventType(EventType.SETTLED_TO_MERCHANT);
                fees.add(fee);
                switch (paymentMethod) {
                    case OVO -> fee.setBasisPoints(150);
                    case VISA, MASTERCARD -> {
                        fee.setBasisPoints(300);
                        fee.setFixedAmount(20);
                        fee.setCurrency(Amount.Currency.PHP);
                    }
                    case GCASH -> fee.setBasisPoints(250);
                    case GRABPAY -> fee.setBasisPoints(200);
                    case ALFAMART -> {
                        fee.setFixedAmount(4000);
                        fee.setCurrency(Amount.Currency.IDR);
                    }
                }
            }
            defaultMerchantContract.setFees(fees);
            feeService.storeContract(defaultMerchantContract);
        } catch (DuplicateNameException e) {
            log.error("Error during initial setup: " + e.getMessage());
        }
    }


    @Transactional
    private void setupDemoAccounts() {
        log.info("setting up demo accounts");

        try {
            Contract defaultMerchantContract = feeService.getDefaultMerchantContract();
            if (defaultMerchantContract == null) {
                throw new IllegalArgumentException("Default contract not found");
            }

            // SETUP Company TechInc
            Account techInc = new Account("TechInc", Account.AccountType.COMPANY);
            techInc = accountService.createAccount(techInc);
            Account techIncGames = accountService.createAccount(
                    new Account("TechInc_Games", Account.AccountType.MERCHANT, techInc, defaultMerchantContract));
            accountService.createAccount(
                    new Account("TechInc_Ads", Account.AccountType.MERCHANT, techInc, defaultMerchantContract));

            // SETUP Company StreamingInc
            Account streamingInc = new Account("StreamingInc", Account.AccountType.COMPANY);
            streamingInc = accountService.createAccount(streamingInc);
            accountService.createAccount(new Account("StreamingInc_Indonesia",
                    Account.AccountType.MERCHANT, streamingInc, defaultMerchantContract));
            accountService.createAccount(new Account("StreamingInc_Philippines",
                    Account.AccountType.MERCHANT, streamingInc, defaultMerchantContract));

            // SETUP Acquirers OVO, GCash, Visa, MC
            Account ovo = accountService.createAccount(new Account("Ovo", Account.AccountType.PARTNER));
            Account gcash = accountService.createAccount(new Account("Gcash", Account.AccountType.PARTNER));
            Account visa = accountService.createAccount(new Account("Visa", Account.AccountType.PARTNER));
            Account mc = accountService.createAccount(new Account("Mastercard", Account.AccountType.PARTNER));

            accountService.createAccount(new Account("Ovo_Aggregator", Account.AccountType.PARTNER_ACCOUNT, ovo));
            accountService.createAccount(new Account("Gcash_Aggregator", Account.AccountType.PARTNER_ACCOUNT, gcash));
            accountService.createAccount(new Account("Visa_Indonesia", Account.AccountType.PARTNER_ACCOUNT, visa));
            accountService.createAccount(new Account("Visa_Philippines", Account.AccountType.PARTNER_ACCOUNT, visa));
            accountService.createAccount(new Account("Visa_Malaysia", Account.AccountType.PARTNER_ACCOUNT, visa));
            accountService.createAccount(new Account("MC_ID", Account.AccountType.PARTNER_ACCOUNT, mc));
            accountService.createAccount(new Account("MC_PH", Account.AccountType.PARTNER_ACCOUNT, mc));

            // SETUP Banks VillageBank, BankOfAsia and IndoBank
            Account villageBank = accountService.createAccount(new Account("VillageBank", Account.AccountType.BANK));
            Account bankOfAsia = accountService.createAccount(new Account("BankOfAsia", Account.AccountType.BANK));
            Account indoBank = accountService.createAccount(new Account("IndoBank", Account.AccountType.BANK));

            Account villageBankEur = accountService.createAccount(new Account("VillageBank-EUR", Account.AccountType.BANK_ACCOUNT, villageBank));
            Account villageBankUsd = accountService.createAccount(new Account("VillageBank-USD", Account.AccountType.BANK_ACCOUNT, villageBank));
            Account villageBankSgd = accountService.createAccount(new Account("VillageBank-SGD", Account.AccountType.BANK_ACCOUNT, villageBank));
            Account bankOfAsiaSgd = accountService.createAccount(new Account("BankOfAsia-SGD", Account.AccountType.BANK_ACCOUNT, bankOfAsia));
            accountService.createAccount(new Account("BankOfAsia-HKD", Account.AccountType.BANK_ACCOUNT, bankOfAsia));
            Account bankOfAsiaPhp = accountService.createAccount(new Account("BankOfAsia-PHP", Account.AccountType.BANK_ACCOUNT, bankOfAsia));
            Account indoBankIdr = accountService.createAccount(new Account("IndoBank-IDR", Account.AccountType.BANK_ACCOUNT, indoBank));
            Account indoBankUsd = accountService.createAccount(new Account("IndoBank-USD", Account.AccountType.BANK_ACCOUNT, indoBank));

            // Set routing table for settlement
            routingService.addRoute(new Route(Route.RoutingType.SETTLEMENT,null, Amount.Currency.EUR,null,villageBankEur));
            routingService.addRoute(new Route(Route.RoutingType.SETTLEMENT,techIncGames, Amount.Currency.USD,null,villageBankUsd));
            routingService.addRoute(new Route(Route.RoutingType.SETTLEMENT,null, Amount.Currency.SGD,null,villageBankSgd));
            routingService.addRoute(new Route(Route.RoutingType.SETTLEMENT,null, Amount.Currency.SGD,null,bankOfAsiaSgd));
            routingService.addRoute(new Route(Route.RoutingType.SETTLEMENT,null, Amount.Currency.PHP,null,bankOfAsiaPhp));
            routingService.addRoute(new Route(Route.RoutingType.SETTLEMENT,null, Amount.Currency.IDR,null,indoBankIdr));
            routingService.addRoute(new Route(Route.RoutingType.SETTLEMENT,null, Amount.Currency.USD,null,indoBankUsd));

        } catch (DuplicateNameException e) {
            log.error("Error while creating demo accounts: " + e.getMessage());
        }
    }

    public void createDemoTransactions(int number) throws BookingException {
        List<Account> partnerAccounts = accountService.listAccountsByType(Account.AccountType.PARTNER_ACCOUNT);
        List<Account> merchantAccounts = accountService.listAccountsByType(Account.AccountType.MERCHANT);
        log.info("Creating " + number + " random demo transactions");
        List<Transaction> transactionList = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            Amount.Currency currency = Amount.Currency.values()[(int) (Math.random() * Amount.Currency.values().length)];
            long amountValue = (long) (Math.random() * 1000);
            if (currency == Amount.Currency.IDR) {
                amountValue *= 1000;
            }
            Amount amount = new Amount(currency, amountValue);
            Account acquirerAccount = partnerAccounts.get((int) (Math.random() * partnerAccounts.size()));
            Account merchantAccount = merchantAccounts.get((int) (Math.random() * merchantAccounts.size()));
            PaymentMethod paymentMethod = PaymentMethod.values()[(int) (Math.random() * PaymentMethod.values().length)];
            transactionList.add(transactionService.newPayment(amount, paymentMethod,merchantAccount,acquirerAccount));
        }

        log.info("Creating random bookings for the demo transactions");
        for (Transaction t : transactionList) {
            int scenario = (int) (Math.random() * 8);
            Transaction capture;
            switch (scenario) {
                case 1 -> transactionService.authorizeOrRefusePayment(t, true);
                case 2 -> transactionService.authorizeOrRefusePayment(t, false);
                case 3 -> {
                    transactionService.authorizeOrRefusePayment(t, true);
                    transactionService.newCapture(t, new Amount(t.getCurrency(), t.getAmount()));
                }
                case 4 -> {
                    transactionService.authorizeOrRefusePayment(t, true);
                    transactionService.newCapture(t, new Amount(t.getCurrency(), t.getAmount() / 2));
                }
                case 5 -> {
                    transactionService.authorizeOrRefusePayment(t, true);
                    transactionService.cancelRemainingAmount(t);
                }
                case 6 -> {
                    transactionService.authorizeOrRefusePayment(t, true);
                    capture = transactionService.newCapture(t, new Amount(t.getCurrency(), t.getAmount()));
                    transactionService.bookEarlySettlementToMerchant(capture, capture.getAmount());
                }
                case 7 -> {
                    transactionService.authorizeOrRefusePayment(t, true);
                    capture = transactionService.newCapture(t, new Amount(t.getCurrency(), t.getAmount()));
                    transactionService.bookSettlementFailed(capture);
                }
            }
        }
    }

    @Autowired
    AccountService accountService;
    @Autowired
    RoutingService routingService;
    @Autowired
    TransactionService transactionService;
    @Autowired
    FeeService feeService;

    public boolean demoDataEnabled() {
        return createDemoData;
    }
}

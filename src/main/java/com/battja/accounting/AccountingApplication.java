package com.battja.accounting;

import com.battja.accounting.accounts.InitialSetup;
import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Journal;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.events.EventType;
import com.battja.accounting.services.AccountService;
import com.battja.accounting.services.BatchService;
import com.battja.accounting.services.BookingService;
import com.battja.accounting.services.TransactionService;
import com.battja.accounting.journals.Amount;
import com.battja.accounting.repositories.AccountRepository;
import com.battja.accounting.repositories.BatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Set;

@SpringBootApplication
@EnableTransactionManagement
public class AccountingApplication {

	private static final Logger log = LoggerFactory.getLogger(AccountingApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AccountingApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(InitialSetup initialSetup, BatchService batchService, AccountService accountService,
								  BookingService bookingService, TransactionService transactionService, BatchRepository batchRepository) {
		return(args) -> {
			// initialSetup.init();

			/**
			Account merchant = accountService.getAccount("Netflix_Indonesia");
			Account AcqAccount = accountService.getAccount("VISA_ID_Facebook");
			Amount amount = new Amount("IDR",100000L);

			Transaction transaction = transactionService.newPayment(amount,merchant,AcqAccount);
			bookingService.book(transaction, EventType.RECEIVED);
			bookingService.book(transaction, EventType.REFUSED);

			Set<Journal> journals = transactionService.getJournalsByTransaction(transactionService.getByReference(transaction.getTransactionReference()));
			log.info(journals.size() + " journals found:");
			for (Journal b : journals) {
				log.info(b.toString());
			}
			 **/
		};

	}

}

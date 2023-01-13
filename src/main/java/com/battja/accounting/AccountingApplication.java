package com.battja.accounting;

import com.battja.accounting.accounts.InitialSetup;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class AccountingApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountingApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(InitialSetup initialSetup) {
		return(args) -> {
			initialSetup.init();
		};

	}

}

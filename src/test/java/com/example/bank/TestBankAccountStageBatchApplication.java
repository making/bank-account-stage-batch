package com.example.bank;

import org.springframework.boot.SpringApplication;

public class TestBankAccountStageBatchApplication {

	public static void main(String[] args) {
		SpringApplication.from(BankAccountStageBatchApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

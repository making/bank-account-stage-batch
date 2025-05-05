package com.example.bank;

import org.springframework.boot.SpringApplication;

public class TestBankAccountStageBatchApplication {

	public static void main(String[] args) {
		SpringApplication.from(BankAccountStageBatchApplication::main)
			.with(TestcontainersConfiguration.class)
			.run("--spring.docker.compose.enabled=false", "inputFile=file:./scripts/bank_account_stage_test_data.csv",
					"chunkSize=1000");
	}

}

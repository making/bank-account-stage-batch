package com.example.bank.job;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class BankAccountStageItemWriter implements ItemWriter<ProcessResult> {

	@Override
	public void write(Chunk<? extends ProcessResult> chunk) throws Exception {
		// TODO CHANGE ME
	}

}

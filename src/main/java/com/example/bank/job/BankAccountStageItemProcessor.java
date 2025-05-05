package com.example.bank.job;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class BankAccountStageItemProcessor implements ItemProcessor<BankAccountStageInput, ProcessResult> {

	@Override
	public ProcessResult process(BankAccountStageInput input) throws Exception {
		// TODO CHANGE ME
		return new ProcessResult();
	}

}

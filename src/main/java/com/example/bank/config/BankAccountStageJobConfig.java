package com.example.bank.config;

import com.example.bank.job.BankAccountStageInput;
import com.example.bank.job.BankAccountStageItemProcessor;
import com.example.bank.job.BankAccountStageItemWriter;
import com.example.bank.job.ProcessResult;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.converter.StringToLocalDateConverter;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.RecordFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
public class BankAccountStageJobConfig {

	@Bean
	@StepScope
	public FlatFileItemReader<BankAccountStageInput> bankAccountStageInputItemReader(
			@Value("#{jobParameters['inputFile'] ?: 'file://${PWD}/src/test/resources/bank_account_stage_usecase_data.csv'}") Resource resource) {
		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new StringToLocalDateConverter());
		return new FlatFileItemReaderBuilder<BankAccountStageInput>().name("BankAccountStageInputItemReader")
			.resource(resource)
			.linesToSkip(1) // skip header
			.delimited()
			.names("customerId", "currentStageCode", "monthEndDate", "totalBalance", "foreignCurrencyBalance",
					"investmentTrustBalance", "monthlyForeignCurrencyPurchase", "monthlyInvestmentTrustPurchase",
					"housingLoanBalance", "monthlyFxTradingVolume")
			.fieldSetMapper(new RecordFieldSetMapper<>(BankAccountStageInput.class, conversionService))
			.build();
	}

	@Bean
	@JobScope
	public Step bankAccountStageStep(@Value("#{jobParameters['chunkSize'] ?: 200}") int chunkSize,
			JobRepository jobRepository, PlatformTransactionManager transactionManager,
			FlatFileItemReader<BankAccountStageInput> itemReader, BankAccountStageItemProcessor itemProcessor,
			BankAccountStageItemWriter itemWriter) {
		return new StepBuilder("BankAccountStage", jobRepository)
			.<BankAccountStageInput, ProcessResult>chunk(chunkSize, transactionManager)
			.reader(itemReader)
			.processor(itemProcessor)
			.writer(itemWriter)
			.build();
	}

	@Bean
	public Job bankAccountStageJob(JobRepository jobRepository, Step bankAccountStageStep) {
		return new JobBuilder("BankAccountStage", jobRepository).incrementer(new RunIdIncrementer())
			.start(bankAccountStageStep)
			.build();
	}

}

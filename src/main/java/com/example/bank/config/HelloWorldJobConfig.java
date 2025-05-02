package com.example.bank.config;

import com.example.bank.hello.HelloTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
public class HelloWorldJobConfig {

	@Bean
	public Step helloWorldStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			HelloTasklet tasklet) {
		return new StepBuilder("HelloWorld", jobRepository).tasklet(tasklet, transactionManager).build();
	}

	@Bean
	public Job helloWorldJob(JobRepository jobRepository, Step helloWorldStep) {
		return new JobBuilder("HelloWorld", jobRepository).incrementer(new RunIdIncrementer())
			.start(helloWorldStep)
			.build();
	}

}
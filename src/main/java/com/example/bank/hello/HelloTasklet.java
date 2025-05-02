package com.example.bank.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class HelloTasklet implements Tasklet {

	private final Logger logger = LoggerFactory.getLogger(HelloTasklet.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("Hello World");
		return RepeatStatus.FINISHED;
	}

}
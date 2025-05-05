package com.example.bank;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = { "spring.batch.job.enabled=false", "spring.docker.compose.enabled=false" })
@SpringBatchTest
class BankAccountStageBatchApplicationTests {

	@Autowired
	JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	@Qualifier("bankAccountStageJob")
	Job job;

	@Autowired
	JdbcClient jdbcClient;

	@Test
	void allUseCases() throws Exception {
		this.jobLauncherTestUtils.setJob(this.job);
		JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParameters(Map.of("inputFile",
				new JobParameter<>("classpath:bank_account_stage_usecase_data.csv", String.class))));
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

		List<Map<String, Object>> customerStageCalculations = this.jdbcClient.sql("""
				SELECT
				    id,
				    customer_id,
				    calculation_date,
				    valid_from,
				    valid_to,
				    current_stage_code,
				    final_stage_code,
				    total_balance,
				    foreign_currency_balance,
				    investment_trust_balance,
				    monthly_foreign_currency_purchase,
				    monthly_investment_trust_purchase,
				    housing_loan_balance,
				    monthly_fx_trading_volume
				FROM
				    customer_stage_calculations
				ORDER BY
				    id
				""").query().listOfRows();

		assertThat(customerStageCalculations).hasSize(11)
		// @formatter:off
				.containsExactly(
						Map.ofEntries(
								Map.entry("id", 1),
								Map.entry("customer_id", "CUS001"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "NONE"),
								Map.entry("final_stage_code", "NONE"),
								Map.entry("total_balance", new BigDecimal("2500000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("0.00")),
								Map.entry("investment_trust_balance", new BigDecimal("0.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 2),
								Map.entry("customer_id", "CUS002"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "NONE"),
								Map.entry("final_stage_code", "SILVER"),
								Map.entry("total_balance", new BigDecimal("3500000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("0.00")),
								Map.entry("investment_trust_balance", new BigDecimal("0.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 3),
								Map.entry("customer_id", "CUS003"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "SILVER"),
								Map.entry("final_stage_code", "GOLD"),
								Map.entry("total_balance", new BigDecimal("8800000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("3000000.00")),
								Map.entry("investment_trust_balance", new BigDecimal("3000000.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 4),
								Map.entry("customer_id", "CUS004"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "GOLD"),
								Map.entry("final_stage_code", "SILVER"),
								Map.entry("total_balance", new BigDecimal("9000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("1000000.00")),
								Map.entry("investment_trust_balance", new BigDecimal("1000000.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 5),
								Map.entry("customer_id", "CUS005"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "PLATINUM"),
								Map.entry("final_stage_code", "GOLD"),
								Map.entry("total_balance", new BigDecimal("13000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("4000000.00")),
								Map.entry("investment_trust_balance", new BigDecimal("4000000.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 6),
								Map.entry("customer_id", "CUS006"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "NONE"),
								Map.entry("final_stage_code", "SILVER"),
								Map.entry("total_balance", new BigDecimal("1000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("0.00")),
								Map.entry("investment_trust_balance", new BigDecimal("0.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("40000.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 7),
								Map.entry("customer_id", "CUS007"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "NONE"),
								Map.entry("final_stage_code", "SILVER"),
								Map.entry("total_balance", new BigDecimal("1000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("0.00")),
								Map.entry("investment_trust_balance", new BigDecimal("0.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("40000.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 8),
								Map.entry("customer_id", "CUS008"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "SILVER"),
								Map.entry("final_stage_code", "PLATINUM"),
								Map.entry("total_balance", new BigDecimal("13000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("6000000.00")),
								Map.entry("investment_trust_balance", new BigDecimal("6000000.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 9),
								Map.entry("customer_id", "CUS009"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "NONE"),
								Map.entry("final_stage_code", "SILVER"),
								Map.entry("total_balance", new BigDecimal("1000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("0.00")),
								Map.entry("investment_trust_balance", new BigDecimal("0.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("10000000.00")),
								Map.entry("monthly_fx_trading_volume", 0)
						),
						Map.ofEntries(
								Map.entry("id", 10),
								Map.entry("customer_id", "CUS010"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "NONE"),
								Map.entry("final_stage_code", "SILVER"),
								Map.entry("total_balance", new BigDecimal("1000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("0.00")),
								Map.entry("investment_trust_balance", new BigDecimal("0.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("0.00")),
								Map.entry("monthly_fx_trading_volume", 1200)
						),
						Map.ofEntries(
								Map.entry("id", 11),
								Map.entry("customer_id", "CUS011"),
								Map.entry("calculation_date", Date.valueOf("2025-04-30")),
								Map.entry("valid_from", Date.valueOf("2025-05-01")),
								Map.entry("valid_to", Date.valueOf("2025-05-31")),
								Map.entry("current_stage_code", "NONE"),
								Map.entry("final_stage_code", "PLATINUM"),
								Map.entry("total_balance", new BigDecimal("13000000.00")),
								Map.entry("foreign_currency_balance", new BigDecimal("6000000.00")),
								Map.entry("investment_trust_balance", new BigDecimal("6000000.00")),
								Map.entry("monthly_foreign_currency_purchase", new BigDecimal("0.00")),
								Map.entry("monthly_investment_trust_purchase", new BigDecimal("0.00")),
								Map.entry("housing_loan_balance", new BigDecimal("10000000.00")),
								Map.entry("monthly_fx_trading_volume", 1200)
						)
				)
		// @formatter:on
		;

		List<Map<String, Object>> conditionEvaluationResults = this.jdbcClient.sql("""
				SELECT
				    calculation_id,
				    condition_id,
				    is_met,
				    evaluated_value
				FROM
				    condition_evaluation_results
				ORDER BY
				    id
				""").query().listOfRows();

		assertThat(conditionEvaluationResults).hasSize(77)
		// @formatter:off
				.containsExactly(
						Map.of("calculation_id", 1L, "condition_id", 1L, "is_met", false, "evaluated_value", new BigDecimal("2500000.00")),
						Map.of("calculation_id", 1L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 1L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 1L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 1L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 1L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 1L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 2L, "condition_id", 1L, "is_met", true, "evaluated_value", new BigDecimal("3500000.00")),
						Map.of("calculation_id", 2L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 2L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 2L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 2L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 2L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 2L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 3L, "condition_id", 1L, "is_met", true, "evaluated_value", new BigDecimal("8800000.00")),
						Map.of("calculation_id", 3L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 3L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 3L, "condition_id", 4L, "is_met", true, "evaluated_value", new BigDecimal("6000000.00")),
						Map.of("calculation_id", 3L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("6000000.00")),
						Map.of("calculation_id", 3L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 3L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 4L, "condition_id", 1L, "is_met", true, "evaluated_value", new BigDecimal("9000000.00")),
						Map.of("calculation_id", 4L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 4L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 4L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("2000000.00")),
						Map.of("calculation_id", 4L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("2000000.00")),
						Map.of("calculation_id", 4L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 4L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 5L, "condition_id", 1L, "is_met", true, "evaluated_value", new BigDecimal("13000000.00")),
						Map.of("calculation_id", 5L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 5L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 5L, "condition_id", 4L, "is_met", true, "evaluated_value", new BigDecimal("8000000.00")),
						Map.of("calculation_id", 5L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("8000000.00")),
						Map.of("calculation_id", 5L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 5L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 6L, "condition_id", 1L, "is_met", false, "evaluated_value", new BigDecimal("1000000.00")),
						Map.of("calculation_id", 6L, "condition_id", 2L, "is_met", true, "evaluated_value", new BigDecimal("40000.00")),
						Map.of("calculation_id", 6L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 6L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 6L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 6L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 6L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 7L, "condition_id", 1L, "is_met", false, "evaluated_value", new BigDecimal("1000000.00")),
						Map.of("calculation_id", 7L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 7L, "condition_id", 3L, "is_met", true, "evaluated_value", new BigDecimal("40000.00")),
						Map.of("calculation_id", 7L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 7L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 7L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 7L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 8L, "condition_id", 1L, "is_met", true, "evaluated_value", new BigDecimal("13000000.00")),
						Map.of("calculation_id", 8L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 8L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 8L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("12000000.00")),
						Map.of("calculation_id", 8L, "condition_id", 5L, "is_met", true, "evaluated_value", new BigDecimal("12000000.00")),
						Map.of("calculation_id", 8L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 8L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 9L, "condition_id", 1L, "is_met", false, "evaluated_value", new BigDecimal("1000000.00")),
						Map.of("calculation_id", 9L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 9L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 9L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 9L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 9L, "condition_id", 6L, "is_met", true, "evaluated_value", new BigDecimal("10000000.00")),
						Map.of("calculation_id", 9L, "condition_id", 7L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 10L, "condition_id", 1L, "is_met", false, "evaluated_value", new BigDecimal("1000000.00")),
						Map.of("calculation_id", 10L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 10L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 10L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 10L, "condition_id", 5L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 10L, "condition_id", 6L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 10L, "condition_id", 7L, "is_met", true, "evaluated_value", new BigDecimal("1200.00")),
						Map.of("calculation_id", 11L, "condition_id", 1L, "is_met", true, "evaluated_value", new BigDecimal("13000000.00")),
						Map.of("calculation_id", 11L, "condition_id", 2L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 11L, "condition_id", 3L, "is_met", false, "evaluated_value", new BigDecimal("0.00")),
						Map.of("calculation_id", 11L, "condition_id", 4L, "is_met", false, "evaluated_value", new BigDecimal("12000000.00")),
						Map.of("calculation_id", 11L, "condition_id", 5L, "is_met", true, "evaluated_value", new BigDecimal("12000000.00")),
						Map.of("calculation_id", 11L, "condition_id", 6L, "is_met", true, "evaluated_value", new BigDecimal("10000000.00")),
						Map.of("calculation_id", 11L, "condition_id", 7L, "is_met", true, "evaluated_value", new BigDecimal("1200.00"))
				)
		// @formatter:on
		;

		List<Map<String, Object>> stageTransitions = this.jdbcClient.sql("""
				SELECT
				    customer_id,
				    calculation_id,
				    previous_stage_code,
				    current_stage_code,
				    transition_date
				FROM
				    stage_transitions
				ORDER BY id
				""").query().listOfRows();
		assertThat(stageTransitions).hasSize(10)
		// @formatter:off
				.containsExactly(
						Map.of("customer_id", "CUS002", "calculation_id", 2L, "previous_stage_code", "NONE", "current_stage_code", "SILVER", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS003", "calculation_id", 3L, "previous_stage_code", "SILVER", "current_stage_code", "GOLD", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS004", "calculation_id", 4L, "previous_stage_code", "GOLD", "current_stage_code", "SILVER", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS005", "calculation_id", 5L, "previous_stage_code", "PLATINUM", "current_stage_code", "GOLD", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS006", "calculation_id", 6L, "previous_stage_code", "NONE", "current_stage_code", "SILVER", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS007", "calculation_id", 7L, "previous_stage_code", "NONE", "current_stage_code", "SILVER", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS008", "calculation_id", 8L, "previous_stage_code", "SILVER", "current_stage_code", "PLATINUM", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS009", "calculation_id", 9L, "previous_stage_code", "NONE", "current_stage_code", "SILVER", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS010", "calculation_id", 10L, "previous_stage_code", "NONE", "current_stage_code", "SILVER", "transition_date", Date.valueOf("2025-05-01")),
						Map.of("customer_id", "CUS011", "calculation_id", 11L, "previous_stage_code", "NONE", "current_stage_code", "PLATINUM", "transition_date", Date.valueOf("2025-05-01"))
				)
		// @formatter:on
		;
	}

}

package com.example.bank.stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionTest {

	@ParameterizedTest
	@CsvSource({ "0,false", "100,true", "150,true", "200,false" })
	void stageConditionMet(BigDecimal evaluatedValue, boolean expected) {
		Condition.StageCondition condition = new Condition.StageCondition(1, ConditionType.TOTAL_BALANCE,
				"Test Condition", StageCode.SILVER, BigDecimal.valueOf(100), BigDecimal.valueOf(200), LocalDate.now(),
				LocalDate.now());
		boolean isMet = condition.isMet(evaluatedValue);
		assertThat(isMet).isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource({ "99,false", "100,true", "101,true" })
	void rangeConditionMet(BigDecimal evaluatedValue, boolean expected) {
		Condition.RankChangeCondition condition = new Condition.RankChangeCondition(1, ConditionType.HOUSING_LOAN,
				"Test Condition", BigDecimal.valueOf(100), 1, LocalDate.now(), LocalDate.now());
		boolean isMet = condition.isMet(evaluatedValue);
		assertThat(isMet).isEqualTo(expected);
	}

	@Test
	void isEffective() {
		Condition.StageCondition condition = new Condition.StageCondition(1, ConditionType.TOTAL_BALANCE,
				"Test Condition", StageCode.SILVER, BigDecimal.valueOf(100), BigDecimal.valueOf(200),
				LocalDate.parse("2025-05-01"), LocalDate.parse("2025-05-31"));
		assertThat(condition.isEffective(LocalDate.parse("2025-04-30"))).isFalse();
		assertThat(condition.isEffective(LocalDate.parse("2025-05-01"))).isTrue();
		assertThat(condition.isEffective(LocalDate.parse("2025-05-02"))).isTrue();
		assertThat(condition.isEffective(LocalDate.parse("2025-05-30"))).isTrue();
		assertThat(condition.isEffective(LocalDate.parse("2025-05-31"))).isTrue();
		assertThat(condition.isEffective(LocalDate.parse("2025-06-01"))).isFalse();
	}

}

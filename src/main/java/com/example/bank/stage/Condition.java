package com.example.bank.stage;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.bank.stage.Condition.RankChangeCondition;
import static com.example.bank.stage.Condition.StageCondition;

public sealed interface Condition permits StageCondition, RankChangeCondition {

	int id();

	ConditionType conditionType();

	String conditionName();

	boolean isMet(BigDecimal evaluatedValue);

	ConditionCategory conditionCategory();

	LocalDate validFrom();

	LocalDate validTo();

	default boolean isEffective(LocalDate date) {
		return (validFrom().isBefore(date) && validTo().isAfter(date)) || validFrom().isEqual(date)
				|| validTo().isEqual(date);
	}

	default BigDecimal evaluate(StageCalculationSource input) {
		return conditionType().evaluate(input);
	}

	record StageCondition(int id, ConditionType conditionType, String conditionName, StageCode stageCode,
			BigDecimal minValue, BigDecimal maxValue, LocalDate validFrom, LocalDate validTo) implements Condition {

		@Override
		public boolean isMet(BigDecimal evaluatedValue) {
			return evaluatedValue.compareTo(minValue) >= 0 && evaluatedValue.compareTo(maxValue) < 0;
		}

		@Override
		public ConditionCategory conditionCategory() {
			return ConditionCategory.STAGE;
		}
	}

	record RankChangeCondition(int id, ConditionType conditionType, String conditionName, BigDecimal thresholdValue,
			int rankChangeLevels, LocalDate validFrom, LocalDate validTo) implements Condition {

		@Override
		public boolean isMet(BigDecimal evaluatedValue) {
			return evaluatedValue.compareTo(thresholdValue) >= 0;
		}

		@Override
		public ConditionCategory conditionCategory() {
			return ConditionCategory.RANK_CHANGE;
		}
	}

}

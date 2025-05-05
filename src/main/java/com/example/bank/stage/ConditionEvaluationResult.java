package com.example.bank.stage;

import java.math.BigDecimal;

import static com.example.bank.stage.ConditionEvaluationResult.CalculationIdAssigned;
import static com.example.bank.stage.ConditionEvaluationResult.CalculationIdUnassigned;

public sealed interface ConditionEvaluationResult permits CalculationIdAssigned, CalculationIdUnassigned {

	int conditionId();

	boolean isMet();

	BigDecimal evaluatedValue();

	record CalculationIdAssigned(long calculationId, int conditionId, boolean isMet,
			BigDecimal evaluatedValue) implements ConditionEvaluationResult {
	}

	record CalculationIdUnassigned(int conditionId, boolean isMet,
			BigDecimal evaluatedValue) implements ConditionEvaluationResult {

		public CalculationIdAssigned assignCalculationId(long calculationId) {
			return new CalculationIdAssigned(calculationId, this.conditionId(), this.isMet(), this.evaluatedValue());
		}
	}

}

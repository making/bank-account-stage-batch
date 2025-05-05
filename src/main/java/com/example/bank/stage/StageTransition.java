package com.example.bank.stage;

import java.time.LocalDate;

import static com.example.bank.stage.StageTransition.CalculationIdAssigned;
import static com.example.bank.stage.StageTransition.CalculationIdUnassigned;

public sealed interface StageTransition permits CalculationIdAssigned, CalculationIdUnassigned {

	String customerId();

	StageCode previousStageCode();

	StageCode currentStageCode();

	LocalDate transitionDate();

	record CalculationIdAssigned(String customerId, long calculationId, StageCode previousStageCode,
			StageCode currentStageCode, LocalDate transitionDate) implements StageTransition {
	}

	record CalculationIdUnassigned(String customerId, StageCode previousStageCode, StageCode currentStageCode,
			LocalDate transitionDate) implements StageTransition {

		public CalculationIdAssigned assignCalculationId(long calculationId) {
			return new CalculationIdAssigned(this.customerId(), calculationId, this.previousStageCode(),
					this.currentStageCode(), this.transitionDate());
		}
	}

}
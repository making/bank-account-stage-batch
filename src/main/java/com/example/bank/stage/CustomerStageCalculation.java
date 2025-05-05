package com.example.bank.stage;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.bank.stage.CustomerStageCalculation.CalculationIdAssigned;
import static com.example.bank.stage.CustomerStageCalculation.CalculationIdUnassigned;

public sealed interface CustomerStageCalculation permits CalculationIdUnassigned, CalculationIdAssigned {

	String customerId();

	LocalDate calculationDate();

	LocalDate validFrom();

	LocalDate validTo();

	StageCode currentStageCode();

	StageCode finalStageCode();

	BigDecimal totalBalance();

	BigDecimal foreignCurrencyBalance();

	BigDecimal investmentTrustBalance();

	BigDecimal monthlyForeignCurrencyPurchase();

	BigDecimal monthlyInvestmentTrustPurchase();

	BigDecimal housingLoanBalance();

	int monthlyFxTradingVolume();

	record CalculationIdUnassigned(String customerId, LocalDate calculationDate, LocalDate validFrom, LocalDate validTo,
			StageCode currentStageCode, StageCode finalStageCode, BigDecimal totalBalance,
			BigDecimal foreignCurrencyBalance, BigDecimal investmentTrustBalance,
			BigDecimal monthlyForeignCurrencyPurchase, BigDecimal monthlyInvestmentTrustPurchase,
			BigDecimal housingLoanBalance, int monthlyFxTradingVolume) implements CustomerStageCalculation {
		public CalculationIdAssigned assignCalculationId(long calculationId) {
			return new CalculationIdAssigned(calculationId, this.customerId(), this.calculationDate(), this.validFrom(),
					this.validTo(), this.currentStageCode(), this.finalStageCode(), this.totalBalance(),
					this.foreignCurrencyBalance(), this.investmentTrustBalance(), this.monthlyForeignCurrencyPurchase(),
					this.monthlyInvestmentTrustPurchase(), this.housingLoanBalance(), this.monthlyFxTradingVolume());
		}
	}

	record CalculationIdAssigned(long id, String customerId, LocalDate calculationDate, LocalDate validFrom,
			LocalDate validTo, StageCode currentStageCode, StageCode finalStageCode, BigDecimal totalBalance,
			BigDecimal foreignCurrencyBalance, BigDecimal investmentTrustBalance,
			BigDecimal monthlyForeignCurrencyPurchase, BigDecimal monthlyInvestmentTrustPurchase,
			BigDecimal housingLoanBalance, int monthlyFxTradingVolume) implements CustomerStageCalculation {
	}

}

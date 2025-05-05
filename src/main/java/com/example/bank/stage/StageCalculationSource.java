package com.example.bank.stage;

import java.math.BigDecimal;

/**
 * Record representing the input data for the Bank Account Stage monthly batch process.
 */
public record StageCalculationSource(
		/*
		 * Total balance across all accounts at month-end (in JPY).
		 */
		BigDecimal totalBalance,

		/*
		 * Foreign currency deposit balance at month-end (converted to JPY).
		 */
		BigDecimal foreignCurrencyBalance,

		/*
		 * Investment trust balance at month-end (based on trade date).
		 */
		BigDecimal investmentTrustBalance,

		/*
		 * Monthly accumulated foreign currency purchase amount (in JPY).
		 */
		BigDecimal monthlyForeignCurrencyPurchase,

		/*
		 * Monthly accumulated investment trust purchase amount (in JPY).
		 */
		BigDecimal monthlyInvestmentTrustPurchase,

		/*
		 * Current housing loan balance (0 if none).
		 */
		BigDecimal housingLoanBalance,

		/*
		 * Monthly FX trading volume (in number of contracts).
		 */
		int monthlyFxTradingVolume) {
}
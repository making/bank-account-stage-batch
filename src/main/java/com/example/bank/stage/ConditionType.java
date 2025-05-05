package com.example.bank.stage;

import java.math.BigDecimal;

public enum ConditionType {

	TOTAL_BALANCE {
		@Override
		public BigDecimal evaluate(StageCalculationSource input) {
			return input.totalBalance();
		}
	},
	MONTHLY_FOREIGN_CURRENCY_PURCHASE {
		@Override
		public BigDecimal evaluate(StageCalculationSource input) {
			return input.monthlyForeignCurrencyPurchase();
		}
	},
	MONTHLY_INVESTMENT_TRUST_PURCHASE {
		@Override
		public BigDecimal evaluate(StageCalculationSource input) {
			return input.monthlyInvestmentTrustPurchase();
		}
	},
	COMBINED_BALANCE_GOLD {
		@Override
		public BigDecimal evaluate(StageCalculationSource input) {
			return input.foreignCurrencyBalance().add(input.investmentTrustBalance());
		}
	},
	COMBINED_BALANCE_PLATINUM {
		@Override
		public BigDecimal evaluate(StageCalculationSource input) {
			return input.foreignCurrencyBalance().add(input.investmentTrustBalance());

		}
	},
	HOUSING_LOAN {
		@Override
		public BigDecimal evaluate(StageCalculationSource input) {
			return input.housingLoanBalance();
		}
	},
	FX_TRADING {
		@Override
		public BigDecimal evaluate(StageCalculationSource input) {
			return BigDecimal.valueOf(input.monthlyFxTradingVolume());
		}
	};

	abstract public BigDecimal evaluate(StageCalculationSource input);

}

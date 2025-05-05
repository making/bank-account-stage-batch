package com.example.bank.stage;

public enum StageCode {

	NONE {
		@Override
		public StageCode rankUp(int rankChangeLevels) {
			if (rankChangeLevels <= 0) {
				return NONE;
			}
			else if (rankChangeLevels == 1) {
				return SILVER;
			}
			else if (rankChangeLevels == 2) {
				return GOLD;
			}
			else {
				return PLATINUM;
			}
		}
	},
	SILVER {
		@Override
		public StageCode rankUp(int rankChangeLevels) {
			if (rankChangeLevels <= 0) {
				return SILVER;
			}
			else if (rankChangeLevels == 1) {
				return GOLD;
			}
			else {
				return PLATINUM;
			}
		}
	},
	GOLD {
		@Override
		public StageCode rankUp(int rankChangeLevels) {
			if (rankChangeLevels <= 0) {
				return GOLD;
			}
			else {
				return PLATINUM;
			}
		}
	},
	PLATINUM {
		@Override
		public StageCode rankUp(int rankChangeLevels) {
			return PLATINUM;
		}
	};

	abstract public StageCode rankUp(int rankChangeLevels);

	public boolean isBetterThan(StageCode other) {
		return this.compareTo(other) > 0;
	}

}

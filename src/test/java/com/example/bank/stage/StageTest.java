package com.example.bank.stage;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StageTest {

	@Test
	void compareTo() {
		var s1 = new Stage(StageCode.NONE, "none", 0);
		var s2 = new Stage(StageCode.SILVER, "silver", 1);
		var s3 = new Stage(StageCode.GOLD, "gold", 2);
		var s4 = new Stage(StageCode.PLATINUM, "platinum", 3);
		var list = new ArrayList<>(List.of(s4, s2, s1, s3));
		list.sort(Stage::compareTo);
		assertThat(list).containsExactly(s1, s2, s3, s4);
	}

	@Test
	void isBetterThan() {
		assertThat(StageCode.SILVER.isBetterThan(StageCode.NONE)).isTrue();
		assertThat(StageCode.SILVER.isBetterThan(StageCode.GOLD)).isFalse();
	}

	@Test
	void noneRankUp() {
		StageCode base = StageCode.NONE;
		assertThat(base.rankUp(0)).isEqualTo(base);
		assertThat(base.rankUp(1)).isEqualTo(StageCode.SILVER);
		assertThat(base.rankUp(2)).isEqualTo(StageCode.GOLD);
	}

	@Test
	void silverRankUp() {
		StageCode base = StageCode.SILVER;
		assertThat(base.rankUp(0)).isEqualTo(base);
		assertThat(base.rankUp(1)).isEqualTo(StageCode.GOLD);
		assertThat(base.rankUp(2)).isEqualTo(StageCode.PLATINUM);
	}

	@Test
	void goldRankUp() {
		StageCode base = StageCode.GOLD;
		assertThat(base.rankUp(0)).isEqualTo(base);
		assertThat(base.rankUp(1)).isEqualTo(StageCode.PLATINUM);
		assertThat(base.rankUp(2)).isEqualTo(StageCode.PLATINUM);
	}

	@Test
	void platinumRankUp() {
		StageCode base = StageCode.PLATINUM;
		assertThat(base.rankUp(0)).isEqualTo(base);
		assertThat(base.rankUp(1)).isEqualTo(StageCode.PLATINUM);
		assertThat(base.rankUp(2)).isEqualTo(StageCode.PLATINUM);
	}

}
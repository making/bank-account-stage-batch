package com.example.bank.stage;

import java.util.Comparator;
import java.util.Objects;

public record Stage(StageCode stageCode, String stageName, int stageOrder) implements Comparable<Stage> {
	@Override
	public int compareTo(Stage o) {
		return Objects.compare(stageOrder, o.stageOrder, Comparator.naturalOrder());
	}

}

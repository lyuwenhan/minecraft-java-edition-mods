package com.example.seedgen.core;

/** A square generation target measured in 16x16-chunk generation units. */
public record GenerationTarget(int unitX, int unitZ, int radius) {
	public static GenerationTarget inactive() {
		return new GenerationTarget(0, 0, -1);
	}

	public boolean isActive() {
		return radius >= 0;
	}
}

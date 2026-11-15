package com.example.seedgen.core;

public record GenerationTarget(int unitX, int unitZ, int radius) {
	public static GenerationTarget inactive() {
		return new GenerationTarget(0, 0, -1);
	}

	public boolean isActive() {
		return radius >= 0;
	}
}

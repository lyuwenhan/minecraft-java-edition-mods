package com.example.wenhanclient.coyote;

public final class CoyoteConfig {
	public static final int DEFAULT_TIME_TICKS = 0;
	public static final int MIN_TIME_TICKS = 0;
	public static final int MAX_TIME_TICKS = Integer.MAX_VALUE;

	public int timeTicks = DEFAULT_TIME_TICKS;

	public void sanitize() {
		if (timeTicks < MIN_TIME_TICKS) {
			timeTicks = MIN_TIME_TICKS;
		}
	}

	public CoyoteConfig copy() {
		CoyoteConfig copy = new CoyoteConfig();
		copy.timeTicks = timeTicks;
		return copy;
	}
}

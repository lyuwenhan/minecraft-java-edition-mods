package com.example.wenhanclient.delayedfalling;

public final class DelayedFallingConfig {
	public static final int DEFAULT_DELAY_TICKS = 0;
	public static final int MIN_DELAY_TICKS = 0;
	public static final int MAX_DELAY_TICKS = Integer.MAX_VALUE;

	public int delayTicks = DEFAULT_DELAY_TICKS;

	public void sanitize() {
		delayTicks = Math.max(MIN_DELAY_TICKS, delayTicks);
	}

	public DelayedFallingConfig copy() {
		DelayedFallingConfig copy = new DelayedFallingConfig();
		copy.delayTicks = this.delayTicks;
		copy.sanitize();
		return copy;
	}
}

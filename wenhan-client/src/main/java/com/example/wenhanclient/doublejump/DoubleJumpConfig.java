package com.example.wenhanclient.doublejump;

public final class DoubleJumpConfig {
	public static final int DEFAULT_JUMP_COUNT = 2;
	public static final boolean DEFAULT_INFINITE_JUMPS = false;
	public static final boolean DEFAULT_COOLDOWN_ENABLED = false;
	public static final int DEFAULT_COOLDOWN_TICKS = 5;
	public static final int MIN_JUMP_COUNT = 1;
	public static final int SLIDER_MAX_JUMP_COUNT = 10;
	public static final int MAX_JUMP_COUNT = Integer.MAX_VALUE;
	public static final int MIN_COOLDOWN_TICKS = 0;
	public static final int MAX_COOLDOWN_TICKS = Integer.MAX_VALUE;

	public int jumpCount = DEFAULT_JUMP_COUNT;
	public boolean infiniteJumps = DEFAULT_INFINITE_JUMPS;
	public boolean cooldownEnabled = DEFAULT_COOLDOWN_ENABLED;
	public int cooldownTicks = DEFAULT_COOLDOWN_TICKS;

	public void sanitize() {
		if (this.jumpCount < MIN_JUMP_COUNT) {
			this.jumpCount = MIN_JUMP_COUNT;
		}

		if (this.cooldownTicks < MIN_COOLDOWN_TICKS) {
			this.cooldownTicks = MIN_COOLDOWN_TICKS;
		}
	}

	public DoubleJumpConfig copy() {
		DoubleJumpConfig copy = new DoubleJumpConfig();
		copy.jumpCount = this.jumpCount;
		copy.infiniteJumps = this.infiniteJumps;
		copy.cooldownEnabled = this.cooldownEnabled;
		copy.cooldownTicks = this.cooldownTicks;
		return copy;
	}
}

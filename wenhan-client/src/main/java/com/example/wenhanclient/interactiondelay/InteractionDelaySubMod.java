package com.example.wenhanclient.interactiondelay;

import com.example.wenhanclient.WenhanClientMod;

public final class InteractionDelaySubMod {
	private static InteractionDelayConfig config = new InteractionDelayConfig();
	private static int miningDelayStep;
	private static int placingDelayStep;

	private InteractionDelaySubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.interactionDelay.copy();
	}

	public static InteractionDelayConfig config() {
		return config.copy();
	}

	public static void setConfig(InteractionDelayConfig newConfig) {
		config = newConfig == null ? new InteractionDelayConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.interactionDelay = config.copy();
		WenhanClientMod.CONFIG.save();
		if (!config.miningDelay) {
			resetMiningDelay();
		}
		if (!config.placingDelay) {
			resetPlacingDelay();
		}
	}

	public static boolean isMiningDelayEnabled() {
		return config.miningDelay;
	}

	public static boolean isPlacingDelayEnabled() {
		return config.placingDelay;
	}

	public static int nextMiningDelay() {
		return nextSteppedDelay(5, miningDelayStep++);
	}

	public static int nextPlacingDelay() {
		return nextSteppedDelay(4, placingDelayStep++);
	}

	private static int nextSteppedDelay(int startDelay, int step) {
		return Math.max(1, startDelay - step / 2);
	}

	public static void resetMiningDelay() {
		miningDelayStep = 0;
	}

	public static void resetPlacingDelay() {
		placingDelayStep = 0;
	}
}

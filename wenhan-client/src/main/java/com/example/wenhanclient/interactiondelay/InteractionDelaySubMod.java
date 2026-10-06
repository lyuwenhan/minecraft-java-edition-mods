package com.example.wenhanclient.interactiondelay;

import com.example.wenhanclient.WenhanClientMod;

public final class InteractionDelaySubMod {
	private static final int[] MINING_DELAYS = {5, 5, 4, 4, 3, 3, 2, 2};
	private static final int[] PLACING_DELAYS = {4, 4, 3, 3, 2, 2};
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
		return nextDelay(MINING_DELAYS, miningDelayStep++);
	}

	public static int nextPlacingDelay() {
		return nextDelay(PLACING_DELAYS, placingDelayStep++);
	}

	public static void resetMiningDelay() {
		miningDelayStep = 0;
	}

	public static void resetPlacingDelay() {
		placingDelayStep = 0;
	}

	private static int nextDelay(int[] delays, int step) {
		return step < delays.length ? delays[step] : 1;
	}
}

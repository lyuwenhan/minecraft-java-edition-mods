package com.example.wenhanclient.betterstep;

import com.example.wenhanclient.WenhanClientMod;

public final class BetterStepSubMod {
	private static BetterStepConfig config = new BetterStepConfig();

	private BetterStepSubMod() {}

	public static void init() {
		config = BetterStepConfig.sanitize(WenhanClientMod.CONFIG.betterStep);
		WenhanClientMod.CONFIG.betterStep = config.copy();
	}

	public static BetterStepConfig config() {
		return config.copy();
	}

	public static void setConfig(BetterStepConfig newConfig) {
		config = BetterStepConfig.sanitize(newConfig);
		WenhanClientMod.CONFIG.betterStep = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean airStepUpEnabled() {
		return config.airStepUpEnabled;
	}

	public static boolean stepDownEnabled() {
		return config.stepDownEnabled;
	}

	public static double stepHeight() {
		return config.stepHeight;
	}
}

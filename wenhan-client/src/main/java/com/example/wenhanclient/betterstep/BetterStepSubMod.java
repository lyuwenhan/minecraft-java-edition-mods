package com.example.wenhanclient.betterstep;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.world.entity.player.Player;

public final class BetterStepSubMod {
	private static BetterStepConfig config = new BetterStepConfig();

	private BetterStepSubMod() {}

	public static void init() {
		config =
				WenhanClientMod.CONFIG.betterStep == null
						? new BetterStepConfig()
						: WenhanClientMod.CONFIG.betterStep;
		config.sanitize();
		WenhanClientMod.CONFIG.betterStep = config.copy();
	}

	public static BetterStepConfig config() {
		return config.copy();
	}

	public static void setConfig(BetterStepConfig newConfig) {
		config = newConfig == null ? new BetterStepConfig() : newConfig;
		config.sanitize();
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

	public static double stepHeight(Player player) {
		if (player != null && player.isShiftKeyDown()) {
			return config.sneakingStepHeight;
		}

		return config.stepHeight;
	}
}

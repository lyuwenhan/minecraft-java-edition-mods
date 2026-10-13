package com.example.wenhanclient.jumpheight;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.player.LocalPlayer;

public final class JumpHeightSubMod {
	private static JumpHeightConfig config = new JumpHeightConfig();

	private JumpHeightSubMod() {}

	public static void init() {
		config = JumpHeightConfig.sanitize(WenhanClientMod.CONFIG.jumpHeight);
		WenhanClientMod.CONFIG.jumpHeight = config.copy();
	}

	public static JumpHeightConfig config() {
		return config.copy();
	}

	public static void setConfig(JumpHeightConfig newConfig) {
		config = JumpHeightConfig.sanitize(newConfig);
		WenhanClientMod.CONFIG.jumpHeight = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static float getJumpStrength(LocalPlayer player) {
		if (!config.enabled) {
			return Float.NaN;
		}

		double gravity = player.getGravity();
		return (float) JumpHeightCalculator.inverseHeight(config.targetHeight, gravity);
	}
}

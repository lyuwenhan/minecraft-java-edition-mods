package com.example.wenhanclient.doublejump;

import com.example.wenhanclient.WenhanClientMod;

public final class DoubleJumpSubMod {
	public static final String MOD_ID = "double-jump";
	private static DoubleJumpConfig config = new DoubleJumpConfig();

	private DoubleJumpSubMod() {}

	public static void init() {
		config =
				WenhanClientMod.CONFIG.doubleJump == null
						? new DoubleJumpConfig()
						: WenhanClientMod.CONFIG.doubleJump;
		config.sanitize();
		WenhanClientMod.CONFIG.doubleJump = config.copy();
	}

	public static DoubleJumpConfig config() {
		return config;
	}

	public static void setConfig(DoubleJumpConfig newConfig) {
		config = newConfig == null ? new DoubleJumpConfig() : newConfig;
		config.sanitize();
		WenhanClientMod.CONFIG.doubleJump = config.copy();
		WenhanClientMod.CONFIG.save();
	}
}

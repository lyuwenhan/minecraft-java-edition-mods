package com.example.wenhanclient.hiderealms;

import com.example.wenhanclient.WenhanClientMod;

public final class HideRealmsSubMod {
	private static HideRealmsConfig config = new HideRealmsConfig();

	private HideRealmsSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.hideRealms.copy();
	}

	public static HideRealmsConfig config() {
		return config.copy();
	}

	public static void setConfig(HideRealmsConfig newConfig) {
		config = newConfig == null ? new HideRealmsConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.hideRealms = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

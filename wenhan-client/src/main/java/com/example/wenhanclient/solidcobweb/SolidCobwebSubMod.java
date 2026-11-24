package com.example.wenhanclient.solidcobweb;

import com.example.wenhanclient.WenhanClientMod;

public final class SolidCobwebSubMod {
	private static SolidCobwebConfig config = new SolidCobwebConfig();

	private SolidCobwebSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.solidCobweb.copy();
	}

	public static SolidCobwebConfig config() {
		return config.copy();
	}

	public static void setConfig(SolidCobwebConfig newConfig) {
		config = newConfig == null ? new SolidCobwebConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.solidCobweb = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

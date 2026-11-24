package com.example.wenhanclient.noflightreset;

import com.example.wenhanclient.WenhanClientMod;

public final class NoFlightResetSubMod {
	private static NoFlightResetConfig config = new NoFlightResetConfig();

	private NoFlightResetSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noFlightReset.copy();
	}

	public static NoFlightResetConfig config() {
		return config.copy();
	}

	public static void setConfig(NoFlightResetConfig newConfig) {
		config = newConfig == null ? new NoFlightResetConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noFlightReset = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

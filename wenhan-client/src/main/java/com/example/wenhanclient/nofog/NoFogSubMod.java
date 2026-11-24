package com.example.wenhanclient.nofog;

import com.example.wenhanclient.WenhanClientMod;

public final class NoFogSubMod {
	private static NoFogConfig config = new NoFogConfig();

	private NoFogSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noFog.copy();
	}

	public static NoFogConfig config() {
		return config.copy();
	}

	public static void setConfig(NoFogConfig newConfig) {
		config = newConfig == null ? new NoFogConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noFog = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

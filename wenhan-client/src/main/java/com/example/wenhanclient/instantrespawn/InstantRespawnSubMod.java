package com.example.wenhanclient.instantrespawn;

import com.example.wenhanclient.WenhanClientMod;

public final class InstantRespawnSubMod {
	private static InstantRespawnConfig config = new InstantRespawnConfig();

	private InstantRespawnSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.instantRespawn.copy();
	}

	public static InstantRespawnConfig config() {
		return config.copy();
	}

	public static void setConfig(InstantRespawnConfig newConfig) {
		config = newConfig == null ? new InstantRespawnConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.instantRespawn = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

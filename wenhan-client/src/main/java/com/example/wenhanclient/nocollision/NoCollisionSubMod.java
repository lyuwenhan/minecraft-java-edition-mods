package com.example.wenhanclient.nocollision;

import com.example.wenhanclient.WenhanClientMod;

public final class NoCollisionSubMod {
	private static NoCollisionConfig config = new NoCollisionConfig();

	private NoCollisionSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noCollision.copy();
	}

	public static NoCollisionConfig config() {
		return config.copy();
	}

	public static void setConfig(NoCollisionConfig newConfig) {
		config = newConfig == null ? new NoCollisionConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noCollision = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

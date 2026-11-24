package com.example.wenhanclient.nocameracollision;

import com.example.wenhanclient.WenhanClientMod;

public final class NoCameraCollisionSubMod {
	private static NoCameraCollisionConfig config = new NoCameraCollisionConfig();

	private NoCameraCollisionSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noCameraCollision.copy();
	}

	public static NoCameraCollisionConfig config() {
		return config.copy();
	}

	public static void setConfig(NoCameraCollisionConfig newConfig) {
		config = newConfig == null ? new NoCameraCollisionConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noCameraCollision = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

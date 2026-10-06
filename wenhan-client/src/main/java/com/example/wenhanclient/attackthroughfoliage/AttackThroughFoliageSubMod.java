package com.example.wenhanclient.attackthroughfoliage;

import com.example.wenhanclient.WenhanClientMod;

public final class AttackThroughFoliageSubMod {
	private static AttackThroughFoliageConfig config = new AttackThroughFoliageConfig();

	private AttackThroughFoliageSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.attackThroughFoliage.copy();
	}

	public static AttackThroughFoliageConfig config() {
		return config.copy();
	}

	public static void setConfig(AttackThroughFoliageConfig newConfig) {
		config = newConfig == null ? new AttackThroughFoliageConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.attackThroughFoliage = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

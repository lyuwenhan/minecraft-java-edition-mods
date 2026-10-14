package com.example.wenhanclient.attackthroughfoliage;

import com.example.wenhanclient.WenhanClientMod;

public final class AttackThroughFoliageConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public static AttackThroughFoliageConfig load() {
		return WenhanClientMod.CONFIG.attackThroughFoliage.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.attackThroughFoliage = copy();
		WenhanClientMod.CONFIG.save();
	}

	public AttackThroughFoliageConfig copy() {
		AttackThroughFoliageConfig copy = new AttackThroughFoliageConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

package com.example.wenhanclient.attackthroughfoliage;

import com.example.wenhanclient.WenhanClientMod;

public final class AttackThroughFoliageConfig {
	public boolean enabled = true;

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

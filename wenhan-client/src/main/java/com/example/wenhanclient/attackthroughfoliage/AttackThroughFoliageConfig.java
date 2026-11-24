package com.example.wenhanclient.attackthroughfoliage;

import com.example.wenhanclient.WenhanClientMod;

public final class AttackThroughFoliageConfig {
	public enum Mode {
		NONE,
		FOLIAGE,
		ALL
	}

	public static final Mode DEFAULT_MODE = Mode.NONE;

	public Mode mode = DEFAULT_MODE;

	public static AttackThroughFoliageConfig load() {
		return WenhanClientMod.CONFIG.attackThroughFoliage.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.attackThroughFoliage = copy();
		WenhanClientMod.CONFIG.save();
	}

	public AttackThroughFoliageConfig copy() {
		AttackThroughFoliageConfig copy = new AttackThroughFoliageConfig();
		copy.mode = this.mode == null ? DEFAULT_MODE : this.mode;
		return copy;
	}
}

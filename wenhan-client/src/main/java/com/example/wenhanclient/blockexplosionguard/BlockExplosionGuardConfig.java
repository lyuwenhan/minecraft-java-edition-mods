package com.example.wenhanclient.blockexplosionguard;

import com.example.wenhanclient.WenhanClientMod;

public final class BlockExplosionGuardConfig {
	public boolean enabled = false;

	public static BlockExplosionGuardConfig load() {
		return WenhanClientMod.CONFIG.blockExplosionGuard.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.blockExplosionGuard = copy();
		WenhanClientMod.CONFIG.save();
	}

	public BlockExplosionGuardConfig copy() {
		BlockExplosionGuardConfig copy = new BlockExplosionGuardConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

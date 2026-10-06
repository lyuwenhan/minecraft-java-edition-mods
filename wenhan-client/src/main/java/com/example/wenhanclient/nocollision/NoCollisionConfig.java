package com.example.wenhanclient.nocollision;

import com.example.wenhanclient.WenhanClientMod;

public final class NoCollisionConfig {
	public boolean enabled = false;

	public static NoCollisionConfig load() {
		return WenhanClientMod.CONFIG.noCollision.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noCollision = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoCollisionConfig copy() {
		NoCollisionConfig copy = new NoCollisionConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

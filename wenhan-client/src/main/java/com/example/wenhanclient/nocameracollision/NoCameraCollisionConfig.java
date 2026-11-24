package com.example.wenhanclient.nocameracollision;

import com.example.wenhanclient.WenhanClientMod;

public final class NoCameraCollisionConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public static NoCameraCollisionConfig load() {
		return WenhanClientMod.CONFIG.noCameraCollision.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noCameraCollision = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoCameraCollisionConfig copy() {
		NoCameraCollisionConfig copy = new NoCameraCollisionConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

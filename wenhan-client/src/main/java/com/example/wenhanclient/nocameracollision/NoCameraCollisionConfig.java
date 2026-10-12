package com.example.wenhanclient.nocameracollision;

import com.example.wenhanclient.WenhanClientMod;

public final class NoCameraCollisionConfig {
	public boolean enabled = false;

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

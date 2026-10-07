package com.example.wenhanclient.noslowdown;

import com.example.wenhanclient.WenhanClientMod;

public final class NoSlowdownConfig {
	public boolean enabled = false;

	public static NoSlowdownConfig load() {
		return WenhanClientMod.CONFIG.noSlowdown.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noSlowdown = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoSlowdownConfig copy() {
		NoSlowdownConfig copy = new NoSlowdownConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

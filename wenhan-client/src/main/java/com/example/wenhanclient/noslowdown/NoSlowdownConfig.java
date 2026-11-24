package com.example.wenhanclient.noslowdown;

import com.example.wenhanclient.WenhanClientMod;

public final class NoSlowdownConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

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

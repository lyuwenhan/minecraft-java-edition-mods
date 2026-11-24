package com.example.wenhanclient.noslowfalling;

import com.example.wenhanclient.WenhanClientMod;

public final class NoSlowFallingConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public static NoSlowFallingConfig load() {
		return WenhanClientMod.CONFIG.noSlowFalling.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noSlowFalling = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoSlowFallingConfig copy() {
		NoSlowFallingConfig copy = new NoSlowFallingConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

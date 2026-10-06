package com.example.wenhanclient.cancelslowdown;

import com.example.wenhanclient.WenhanClientMod;

public final class CancelSlowdownConfig {
	public boolean enabled = false;

	public static CancelSlowdownConfig load() {
		return WenhanClientMod.CONFIG.cancelSlowdown.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.cancelSlowdown = copy();
		WenhanClientMod.CONFIG.save();
	}

	public CancelSlowdownConfig copy() {
		CancelSlowdownConfig copy = new CancelSlowdownConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

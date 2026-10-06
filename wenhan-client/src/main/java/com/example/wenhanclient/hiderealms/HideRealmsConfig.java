package com.example.wenhanclient.hiderealms;

import com.example.wenhanclient.WenhanClientMod;

public final class HideRealmsConfig {
	public boolean enabled = false;

	public static HideRealmsConfig load() {
		return WenhanClientMod.CONFIG.hideRealms.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.hideRealms = copy();
		WenhanClientMod.CONFIG.save();
	}

	public HideRealmsConfig copy() {
		HideRealmsConfig copy = new HideRealmsConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

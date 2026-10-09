package com.example.wenhanclient.noslimebounce;

import com.example.wenhanclient.WenhanClientMod;

public final class NoSlimeBounceConfig {
	public boolean enabled = false;

	public static NoSlimeBounceConfig load() {
		return WenhanClientMod.CONFIG.noSlimeBounce.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noSlimeBounce = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoSlimeBounceConfig copy() {
		NoSlimeBounceConfig copy = new NoSlimeBounceConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

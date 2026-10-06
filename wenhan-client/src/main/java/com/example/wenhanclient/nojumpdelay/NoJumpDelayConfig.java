package com.example.wenhanclient.nojumpdelay;

import com.example.wenhanclient.WenhanClientMod;

public final class NoJumpDelayConfig {
	public boolean enabled = false;

	public static NoJumpDelayConfig load() {
		return WenhanClientMod.CONFIG.noJumpDelay.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noJumpDelay = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoJumpDelayConfig copy() {
		NoJumpDelayConfig copy = new NoJumpDelayConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

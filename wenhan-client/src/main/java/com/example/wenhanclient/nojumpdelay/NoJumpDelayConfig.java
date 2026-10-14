package com.example.wenhanclient.nojumpdelay;

import com.example.wenhanclient.WenhanClientMod;

public final class NoJumpDelayConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

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

package com.example.wenhanclient.nojumpdelay;

import com.example.wenhanclient.WenhanClientMod;

public final class NoJumpDelaySubMod {
	private static NoJumpDelayConfig config = new NoJumpDelayConfig();

	private NoJumpDelaySubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noJumpDelay.copy();
	}

	public static NoJumpDelayConfig config() {
		return config.copy();
	}

	public static void setConfig(NoJumpDelayConfig newConfig) {
		config = newConfig == null ? new NoJumpDelayConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noJumpDelay = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

package com.example.wenhanclient.interactiondelay;

import com.example.wenhanclient.WenhanClientMod;

public final class InteractionDelayConfig {
	public static final boolean DEFAULT_MINING_DELAY = false;
	public static final boolean DEFAULT_PLACING_DELAY = false;

	public boolean miningDelay = DEFAULT_MINING_DELAY;
	public boolean placingDelay = DEFAULT_PLACING_DELAY;

	public static InteractionDelayConfig load() {
		return WenhanClientMod.CONFIG.interactionDelay.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.interactionDelay = copy();
		WenhanClientMod.CONFIG.save();
	}

	public InteractionDelayConfig copy() {
		InteractionDelayConfig copy = new InteractionDelayConfig();
		copy.miningDelay = this.miningDelay;
		copy.placingDelay = this.placingDelay;
		return copy;
	}
}

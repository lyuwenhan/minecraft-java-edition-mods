package com.example.wenhanclient.interactiondelay;

import com.example.wenhanclient.WenhanClientMod;

public final class InteractionDelayConfig {
	public boolean miningDelay = false;
	public boolean placingDelay = false;

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

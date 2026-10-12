package com.example.wenhanclient.stablemining;

import com.example.wenhanclient.WenhanClientMod;

public final class StableMiningConfig {
	public boolean enabled = false;

	public static StableMiningConfig load() {
		return WenhanClientMod.CONFIG.stableMining.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.stableMining = copy();
		WenhanClientMod.CONFIG.save();
	}

	public StableMiningConfig copy() {
		StableMiningConfig copy = new StableMiningConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

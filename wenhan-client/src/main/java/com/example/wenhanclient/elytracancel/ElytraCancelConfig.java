package com.example.wenhanclient.elytracancel;

import com.example.wenhanclient.WenhanClientMod;

public final class ElytraCancelConfig {
	public boolean enabled = false;

	public static ElytraCancelConfig load() {
		return WenhanClientMod.CONFIG.elytraCancel.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.elytraCancel = copy();
		WenhanClientMod.CONFIG.save();
	}

	public ElytraCancelConfig copy() {
		ElytraCancelConfig copy = new ElytraCancelConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

package com.example.wenhanclient.nolevitation;

import com.example.wenhanclient.WenhanClientMod;

public final class NoLevitationConfig {
	public boolean enabled = false;

	public static NoLevitationConfig load() {
		return WenhanClientMod.CONFIG.noLevitation.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noLevitation = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoLevitationConfig copy() {
		NoLevitationConfig copy = new NoLevitationConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

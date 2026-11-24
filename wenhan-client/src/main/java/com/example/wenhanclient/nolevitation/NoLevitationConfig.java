package com.example.wenhanclient.nolevitation;

import com.example.wenhanclient.WenhanClientMod;

public final class NoLevitationConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

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

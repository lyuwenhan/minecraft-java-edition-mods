package com.example.wenhanclient.nofog;

import com.example.wenhanclient.WenhanClientMod;

public final class NoFogConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public static NoFogConfig load() {
		return WenhanClientMod.CONFIG.noFog.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noFog = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoFogConfig copy() {
		NoFogConfig copy = new NoFogConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

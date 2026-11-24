package com.example.wenhanclient.instantrespawn;

import com.example.wenhanclient.WenhanClientMod;

public final class InstantRespawnConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public static InstantRespawnConfig load() {
		return WenhanClientMod.CONFIG.instantRespawn.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.instantRespawn = copy();
		WenhanClientMod.CONFIG.save();
	}

	public InstantRespawnConfig copy() {
		InstantRespawnConfig copy = new InstantRespawnConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

package com.example.wenhanclient.instantrespawn;

import com.example.wenhanclient.WenhanClientMod;

public final class InstantRespawnConfig {
	public boolean enabled = false;

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

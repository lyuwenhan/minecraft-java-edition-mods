package com.example.wenhanclient.noflightreset;

import com.example.wenhanclient.WenhanClientMod;

public final class NoFlightResetConfig {
	public boolean enabled = false;

	public static NoFlightResetConfig load() {
		return WenhanClientMod.CONFIG.noFlightReset.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noFlightReset = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoFlightResetConfig copy() {
		NoFlightResetConfig copy = new NoFlightResetConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

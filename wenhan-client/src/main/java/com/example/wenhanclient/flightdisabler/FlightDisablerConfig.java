package com.example.wenhanclient.flightdisabler;

import com.example.wenhanclient.WenhanClientMod;

public final class FlightDisablerConfig {
	public boolean enabled = false;

	public static FlightDisablerConfig load() {
		return WenhanClientMod.CONFIG.flightDisabler.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.flightDisabler = copy();
		WenhanClientMod.CONFIG.save();
	}

	public FlightDisablerConfig copy() {
		FlightDisablerConfig copy = new FlightDisablerConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

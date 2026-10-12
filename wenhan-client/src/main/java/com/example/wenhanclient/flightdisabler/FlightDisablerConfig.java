package com.example.wenhanclient.flightdisabler;

import com.example.wenhanclient.WenhanClientMod;

public final class FlightDisablerConfig {
	public Boolean enabled = null;
	public boolean creativeFlightEnabled = false;
	public boolean elytraFlightEnabled = false;

	public static FlightDisablerConfig load() {
		return WenhanClientMod.CONFIG.flightDisabler.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.flightDisabler = copy();
		WenhanClientMod.CONFIG.save();
	}

	public FlightDisablerConfig copy() {
		FlightDisablerConfig copy = new FlightDisablerConfig();
		copy.creativeFlightEnabled = this.creativeFlightEnabled();
		copy.elytraFlightEnabled = this.elytraFlightEnabled();
		return copy;
	}

	public boolean creativeFlightEnabled() {
		return this.enabled != null ? this.enabled : this.creativeFlightEnabled;
	}

	public boolean elytraFlightEnabled() {
		return this.enabled != null ? this.enabled : this.elytraFlightEnabled;
	}

	public boolean anyEnabled() {
		return this.creativeFlightEnabled() || this.elytraFlightEnabled();
	}
}

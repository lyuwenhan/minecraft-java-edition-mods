package com.example.wenhanclient.flightdisabler;

import com.example.wenhanclient.WenhanClientMod;

public final class FlightDisablerConfig {
	public static final Boolean DEFAULT_ENABLED = null;
	public static final boolean DEFAULT_CREATIVE_FLIGHT_ENABLED = false;
	public static final boolean DEFAULT_ELYTRA_FLIGHT_ENABLED = false;

	public Boolean enabled = DEFAULT_ENABLED;
	public boolean creativeFlightEnabled = DEFAULT_CREATIVE_FLIGHT_ENABLED;
	public boolean elytraFlightEnabled = DEFAULT_ELYTRA_FLIGHT_ENABLED;

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

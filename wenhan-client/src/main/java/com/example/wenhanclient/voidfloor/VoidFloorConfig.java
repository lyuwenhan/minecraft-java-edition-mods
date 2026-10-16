package com.example.wenhanclient.voidfloor;

public final class VoidFloorConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public VoidFloorConfig copy() {
		VoidFloorConfig copy = new VoidFloorConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

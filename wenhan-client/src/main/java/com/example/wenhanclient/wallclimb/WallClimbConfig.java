package com.example.wenhanclient.wallclimb;

public final class WallClimbConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public WallClimbConfig copy() {
		WallClimbConfig copy = new WallClimbConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

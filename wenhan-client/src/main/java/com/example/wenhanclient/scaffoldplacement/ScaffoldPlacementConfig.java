package com.example.wenhanclient.scaffoldplacement;

public final class ScaffoldPlacementConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public ScaffoldPlacementConfig copy() {
		ScaffoldPlacementConfig copy = new ScaffoldPlacementConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

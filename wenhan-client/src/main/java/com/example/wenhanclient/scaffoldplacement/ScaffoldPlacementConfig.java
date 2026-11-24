package com.example.wenhanclient.scaffoldplacement;

public final class ScaffoldPlacementConfig {
	public static final boolean DEFAULT_ENABLED = false;
	public static final boolean DEFAULT_LIQUID_SURFACE_PLACEMENT = false;

	public boolean enabled = DEFAULT_ENABLED;
	public boolean liquidSurfacePlacement = DEFAULT_LIQUID_SURFACE_PLACEMENT;

	public ScaffoldPlacementConfig copy() {
		ScaffoldPlacementConfig copy = new ScaffoldPlacementConfig();
		copy.enabled = this.enabled;
		copy.liquidSurfacePlacement = this.liquidSurfacePlacement;
		return copy;
	}
}

package com.example.wenhanclient.placementrestriction;

public final class PlacementRestrictionConfig {
	public static final PlacementRestrictionMode DEFAULT_MODE = PlacementRestrictionMode.DISABLED;

	public PlacementRestrictionMode mode = DEFAULT_MODE;

	public void sanitize() {
		if (mode == null) {
			mode = DEFAULT_MODE;
		}
	}

	public PlacementRestrictionConfig copy() {
		PlacementRestrictionConfig copy = new PlacementRestrictionConfig();
		copy.mode = this.mode;
		copy.sanitize();
		return copy;
	}
}

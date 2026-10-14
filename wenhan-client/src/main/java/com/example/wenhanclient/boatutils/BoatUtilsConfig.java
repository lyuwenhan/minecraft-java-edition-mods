package com.example.wenhanclient.boatutils;


public final class BoatUtilsConfig {
	public static final boolean DEFAULT_UNRESTRICTED_VIEW_ROTATION = false;
	public static final boolean DEFAULT_VIEW_DIRECTION_LOCK_ENABLED = false;
	public static final boolean DEFAULT_CAMERA_AUTO_RECENTER_ENABLED = false;
	public static final boolean DEFAULT_DIRECTION_HOTKEYS_ENABLED = false;
	public static final boolean DEFAULT_DIRECTION_HOTKEYS_USE_45_DEGREE_ANGLES = false;
	public static final boolean DEFAULT_BLUE_ICE_SPEED_EVERYWHERE = false;
	public static final boolean DEFAULT_PREVENT_SINKING = false;
	public static final float DEFAULT_BOAT_STEP_HEIGHT = 0.0F;
	public static final boolean DEFAULT_HANDBRAKE_ENABLED = false;
	public static final boolean DEFAULT_HANDBRAKE_BOOST_ENABLED = false;
	public static final boolean DEFAULT_LATERAL_FRICTION_ENABLED = false;

	public boolean unrestrictedViewRotation = DEFAULT_UNRESTRICTED_VIEW_ROTATION;
	public boolean viewDirectionLockEnabled = DEFAULT_VIEW_DIRECTION_LOCK_ENABLED;
	public boolean cameraAutoRecenterEnabled = DEFAULT_CAMERA_AUTO_RECENTER_ENABLED;
	public boolean directionHotkeysEnabled = DEFAULT_DIRECTION_HOTKEYS_ENABLED;
	public boolean directionHotkeysUse45DegreeAngles =
			DEFAULT_DIRECTION_HOTKEYS_USE_45_DEGREE_ANGLES;
	public boolean blueIceSpeedEverywhere = DEFAULT_BLUE_ICE_SPEED_EVERYWHERE;
	public boolean preventSinking = DEFAULT_PREVENT_SINKING;
	public float boatStepHeight = DEFAULT_BOAT_STEP_HEIGHT;
	public boolean handbrakeEnabled = DEFAULT_HANDBRAKE_ENABLED;
	public boolean handbrakeBoostEnabled = DEFAULT_HANDBRAKE_BOOST_ENABLED;
	public boolean lateralFrictionEnabled = DEFAULT_LATERAL_FRICTION_ENABLED;

	public void sanitize() {
		this.boatStepHeight =
				Math.round(Math.max(0.0F, Math.min(10.0F, this.boatStepHeight)) * 10.0F) / 10.0F;
	}

	public BoatUtilsConfig copy() {
		BoatUtilsConfig copy = new BoatUtilsConfig();
		copy.unrestrictedViewRotation = this.unrestrictedViewRotation;
		copy.viewDirectionLockEnabled = this.viewDirectionLockEnabled;
		copy.cameraAutoRecenterEnabled = this.cameraAutoRecenterEnabled;
		copy.directionHotkeysEnabled = this.directionHotkeysEnabled;
		copy.directionHotkeysUse45DegreeAngles = this.directionHotkeysUse45DegreeAngles;
		copy.blueIceSpeedEverywhere = this.blueIceSpeedEverywhere;
		copy.preventSinking = this.preventSinking;
		copy.boatStepHeight = this.boatStepHeight;
		copy.handbrakeEnabled = this.handbrakeEnabled;
		copy.handbrakeBoostEnabled = this.handbrakeBoostEnabled;
		copy.lateralFrictionEnabled = this.lateralFrictionEnabled;
		return copy;
	}
}

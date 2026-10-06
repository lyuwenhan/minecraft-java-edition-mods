package com.example.wenhanclient.boatutils;

import com.example.wenhanclient.WenhanClientMod;

public final class BoatUtilsConfig {
	private static Values current = Values.defaults();

	private BoatUtilsConfig() {}

	public static synchronized void load() {
		current = sanitize(WenhanClientMod.CONFIG.boatUtils);
		WenhanClientMod.CONFIG.boatUtils = current.copy();
	}

	public static synchronized void save() {
		WenhanClientMod.CONFIG.boatUtils = current.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static synchronized Values get() {
		return current.copy();
	}

	public static synchronized void set(Values values) {
		current = sanitize(values);
		save();
	}

	public static synchronized boolean unrestrictedViewRotation() {
		return current.unrestrictedViewRotation;
	}

	public static synchronized boolean viewDirectionLockEnabled() {
		return current.viewDirectionLockEnabled;
	}

	public static synchronized boolean directionHotkeysEnabled() {
		return current.directionHotkeysEnabled;
	}

	public static synchronized boolean directionHotkeysUse45DegreeAngles() {
		return current.directionHotkeysUse45DegreeAngles;
	}

	public static synchronized boolean blueIceSpeedEverywhere() {
		return current.blueIceSpeedEverywhere;
	}

	public static synchronized boolean preventSinking() {
		return current.preventSinking;
	}

	public static synchronized float boatStepHeight() {
		return current.boatStepHeight;
	}

	public static synchronized boolean handbrakeEnabled() {
		return current.handbrakeEnabled;
	}

	public static synchronized boolean handbrakeBoostEnabled() {
		return current.handbrakeBoostEnabled;
	}

	public static synchronized boolean lateralFrictionEnabled() {
		return current.lateralFrictionEnabled;
	}

	public static Values sanitize(Values values) {
		if (values == null) {
			return Values.defaults();
		}

		Values sanitized = values.copy();
		sanitized.boatStepHeight =
				Math.round(Math.max(0.0F, Math.min(10.0F, sanitized.boatStepHeight)) * 10.0F)
						/ 10.0F;
		return sanitized;
	}

	public static final class Values {
		public boolean unrestrictedViewRotation;
		public boolean viewDirectionLockEnabled;
		public boolean directionHotkeysEnabled;
		public boolean directionHotkeysUse45DegreeAngles;
		public boolean blueIceSpeedEverywhere;
		public boolean preventSinking;
		public float boatStepHeight;
		public boolean handbrakeEnabled;
		public boolean handbrakeBoostEnabled;
		public boolean lateralFrictionEnabled;

		public static Values defaults() {
			Values values = new Values();
			values.unrestrictedViewRotation = false;
			values.viewDirectionLockEnabled = false;
			values.directionHotkeysEnabled = false;
			values.directionHotkeysUse45DegreeAngles = true;
			values.blueIceSpeedEverywhere = false;
			values.preventSinking = false;
			values.boatStepHeight = 0.0F;
			values.handbrakeEnabled = false;
			values.handbrakeBoostEnabled = false;
			values.lateralFrictionEnabled = false;
			return values;
		}

		public Values copy() {
			Values copy = new Values();
			copy.unrestrictedViewRotation = this.unrestrictedViewRotation;
			copy.viewDirectionLockEnabled = this.viewDirectionLockEnabled;
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
}

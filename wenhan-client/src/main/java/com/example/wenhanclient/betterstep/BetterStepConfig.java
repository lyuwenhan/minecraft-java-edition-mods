package com.example.wenhanclient.betterstep;

public final class BetterStepConfig {
	public static final boolean DEFAULT_AIR_STEP_UP_ENABLED = true;
	public static final boolean DEFAULT_STEP_DOWN_ENABLED = true;
	public static final double DEFAULT_STEP_HEIGHT = 0.6D;
	public static final double DEFAULT_SNEAKING_STEP_HEIGHT = DEFAULT_STEP_HEIGHT;
	public static final double MIN_STEP_HEIGHT = 0.0D;
	public static final double MAX_STEP_HEIGHT = 10.0D;
	public static final int STEP_HEIGHT_SLIDER_MIN = 0;
	public static final int STEP_HEIGHT_SLIDER_MAX = 100;

	public boolean airStepUpEnabled = DEFAULT_AIR_STEP_UP_ENABLED;
	public boolean stepDownEnabled = DEFAULT_STEP_DOWN_ENABLED;
	public double stepHeight = DEFAULT_STEP_HEIGHT;
	public double sneakingStepHeight = DEFAULT_SNEAKING_STEP_HEIGHT;

	public static BetterStepConfig sanitize(BetterStepConfig config) {
		BetterStepConfig sanitized = config == null ? new BetterStepConfig() : config.copy();
		sanitized.stepHeight =
				roundToOneDecimal(
						clamp(
								sanitized.stepHeight,
								DEFAULT_STEP_HEIGHT,
								MIN_STEP_HEIGHT,
								MAX_STEP_HEIGHT));
		sanitized.sneakingStepHeight =
				roundToOneDecimal(
						clamp(
								sanitized.sneakingStepHeight,
								DEFAULT_SNEAKING_STEP_HEIGHT,
								MIN_STEP_HEIGHT,
								MAX_STEP_HEIGHT));
		return sanitized;
	}

	public static int stepHeightToSlider(double value) {
		double sanitized = clamp(value, DEFAULT_STEP_HEIGHT, MIN_STEP_HEIGHT, MAX_STEP_HEIGHT);
		return (int) Math.round(sanitized * 10.0D);
	}

	public static double sliderToStepHeight(int value) {
		int sanitized = Math.max(STEP_HEIGHT_SLIDER_MIN, Math.min(STEP_HEIGHT_SLIDER_MAX, value));
		return sanitized / 10.0D;
	}

	public BetterStepConfig copy() {
		BetterStepConfig copy = new BetterStepConfig();
		copy.airStepUpEnabled = this.airStepUpEnabled;
		copy.stepDownEnabled = this.stepDownEnabled;
		copy.stepHeight = this.stepHeight;
		copy.sneakingStepHeight = this.sneakingStepHeight;
		return copy;
	}

	private static double clamp(double value, double fallback, double min, double max) {
		double result = Double.isFinite(value) ? value : fallback;
		if (result < min) {
			return min;
		}
		if (result > max) {
			return max;
		}
		return result;
	}

	public static double roundToOneDecimal(double value) {
		return Math.round(value * 10.0D) / 10.0D;
	}
}

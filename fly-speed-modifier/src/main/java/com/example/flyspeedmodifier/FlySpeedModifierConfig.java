package com.example.flyspeedmodifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class FlySpeedModifierConfig {
	public static final double DEFAULT_MIN_SPEED = 0.0D;
	public static final double DEFAULT_MAX_SPEED = 20.0D;
	public static final double DEFAULT_INITIAL_SPEED = 1.0D;
	public static final double DEFAULT_SCROLL_STEP = 0.1D;
	public static final boolean DEFAULT_FULL_RANGE = false;
	public static final boolean DEFAULT_RESET_ON_ADJUST = true;
	public static final double MIN_ALLOWED_MIN_SPEED = 0.0D;
	public static final double MAX_ALLOWED_MIN_SPEED = 1.0D;
	public static final double MIN_ALLOWED_MAX_SPEED = 1.0D;
	public static final double STANDARD_MAX_SPEED = 20.0D;
	public static final double FULL_RANGE_MAX_SPEED = 100.0D;
	public static final double MAX_ALLOWED_MAX_SPEED = FULL_RANGE_MAX_SPEED;
	public static final double MIN_ALLOWED_INITIAL_SPEED = 0.0D;
	public static final double MAX_ALLOWED_INITIAL_SPEED = 100.0D;
	public static final double MIN_ALLOWED_SCROLL_STEP = 0.1D;
	public static final double MAX_ALLOWED_SCROLL_STEP = 2.0D;
	public static final int MAX_SPEED_SLIDER_MIN = 100;
	public static final int STANDARD_MAX_SPEED_SLIDER_MAX = 2000;
	public static final int FULL_RANGE_MAX_SPEED_SLIDER_MAX = 10000;
	public static final int MIN_SPEED_SLIDER_MIN = 0;
	public static final int MIN_SPEED_SLIDER_MAX = 1000;
	public static final int INITIAL_SPEED_SLIDER_MIN = 0;
	public static final int INITIAL_SPEED_SLIDER_MAX = 10000;
	public static final int SCROLL_STEP_SLIDER_MIN = 1;
	public static final int SCROLL_STEP_SLIDER_MAX = 20;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve(FlySpeedModifierMod.MOD_ID + ".json");
	private static Values current = Values.defaults();

	private FlySpeedModifierConfig() {}

	public static synchronized void load() {
		if (!Files.exists(CONFIG_PATH)) {
			current = Values.defaults();
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			Values loaded = GSON.fromJson(reader, Values.class);
			if (loaded == null) {
				current = Values.defaults();
			} else {
				current = sanitize(loaded);
			}
		} catch (IOException | RuntimeException ignored) {
			current = Values.defaults();
		}
		save();
	}

	public static synchronized void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(current, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static synchronized Values get() {
		return current.copy();
	}

	public static synchronized void set(Values values) {
		current = sanitize(values);
		save();
		FreecamSpeedController.onConfigChanged();
	}

	public static synchronized void resetToDefaults() {
		current = Values.defaults();
		save();
		FreecamSpeedController.onConfigChanged();
	}

	public static synchronized double minSpeed() {
		return current.minSpeed;
	}

	public static synchronized boolean fullRange() {
		return current.fullRange;
	}

	public static synchronized double maxSpeed() {
		double upperBound = current.fullRange ? FULL_RANGE_MAX_SPEED : STANDARD_MAX_SPEED;
		return clampFinite(current.maxSpeed, DEFAULT_MAX_SPEED, MIN_ALLOWED_MAX_SPEED, upperBound);
	}

	public static synchronized double initialSpeed() {
		return clampFinite(current.initialSpeed, DEFAULT_INITIAL_SPEED, minSpeed(), maxSpeed());
	}

	public static synchronized double scrollStep() {
		return current.scrollStep;
	}

	public static synchronized boolean resetOnAdjust() {
		return current.resetOnAdjust;
	}

	public static Values sanitize(Values values) {
		Values sanitized = values == null ? Values.defaults() : values.copy();
		sanitized.minSpeed =
				clampFinite(
						sanitized.minSpeed, DEFAULT_MIN_SPEED, MIN_ALLOWED_MIN_SPEED, MAX_ALLOWED_MIN_SPEED);
		sanitized.maxSpeed =
				clampFinite(
						sanitized.maxSpeed, DEFAULT_MAX_SPEED, MIN_ALLOWED_MAX_SPEED, MAX_ALLOWED_MAX_SPEED);
		sanitized.initialSpeed =
				roundToTwoDecimals(
						clampFinite(
								sanitized.initialSpeed,
								DEFAULT_INITIAL_SPEED,
								MIN_ALLOWED_INITIAL_SPEED,
								MAX_ALLOWED_INITIAL_SPEED));
		if (!sanitized.fullRange) {
			if (sanitized.maxSpeed > STANDARD_MAX_SPEED) {
				sanitized.maxSpeed = DEFAULT_MAX_SPEED;
			}
			if (sanitized.initialSpeed > STANDARD_MAX_SPEED) {
				sanitized.initialSpeed = DEFAULT_INITIAL_SPEED;
			}
		}
		sanitized.scrollStep =
				roundToOneDecimal(
						clampFinite(
								sanitized.scrollStep,
								DEFAULT_SCROLL_STEP,
								MIN_ALLOWED_SCROLL_STEP,
								MAX_ALLOWED_SCROLL_STEP));
		return sanitized;
	}

	public static int minSpeedToSlider(double value) {
		double sanitized =
				clampFinite(value, DEFAULT_MIN_SPEED, MIN_ALLOWED_MIN_SPEED, MAX_ALLOWED_MIN_SPEED);
		return (int) Math.round(sanitized * MIN_SPEED_SLIDER_MAX);
	}

	public static double sliderToMinSpeed(int sliderValue) {
		int sanitized = clampInt(sliderValue, MIN_SPEED_SLIDER_MIN, MIN_SPEED_SLIDER_MAX);
		return sanitized / (double) MIN_SPEED_SLIDER_MAX;
	}

	public static int initialSpeedToSlider(double value) {
		double sanitized =
				roundToTwoDecimals(
						clampFinite(
								value,
								DEFAULT_INITIAL_SPEED,
								MIN_ALLOWED_INITIAL_SPEED,
								MAX_ALLOWED_INITIAL_SPEED));
		return (int) Math.round(sanitized * 100.0D);
	}

	public static double sliderToInitialSpeed(int sliderValue) {
		int sanitized = clampInt(sliderValue, INITIAL_SPEED_SLIDER_MIN, INITIAL_SPEED_SLIDER_MAX);
		return sanitized / 100.0D;
	}

	public static int scrollStepToSlider(double value) {
		double sanitized =
				roundToOneDecimal(
						clampFinite(
								value, DEFAULT_SCROLL_STEP, MIN_ALLOWED_SCROLL_STEP, MAX_ALLOWED_SCROLL_STEP));
		return (int) Math.round(sanitized * 10.0D);
	}

	public static double sliderToScrollStep(int sliderValue) {
		int sanitized = clampInt(sliderValue, SCROLL_STEP_SLIDER_MIN, SCROLL_STEP_SLIDER_MAX);
		return sanitized / 10.0D;
	}

	public static int maxSpeedToSlider(double value, boolean fullRange) {
		double upperBound = fullRange ? FULL_RANGE_MAX_SPEED : STANDARD_MAX_SPEED;
		double sanitized =
				roundToTwoDecimals(
						clampFinite(value, DEFAULT_MAX_SPEED, MIN_ALLOWED_MAX_SPEED, upperBound));
		return clampInt(
				(int) Math.round(sanitized * 100.0D), MAX_SPEED_SLIDER_MIN, maxSpeedSliderMax(fullRange));
	}

	public static double sliderToMaxSpeed(int sliderValue, boolean fullRange) {
		int sanitized = clampInt(sliderValue, MAX_SPEED_SLIDER_MIN, maxSpeedSliderMax(fullRange));
		return sanitized / 100.0D;
	}

	public static int maxSpeedSliderMax(boolean fullRange) {
		if (fullRange) {
			return FULL_RANGE_MAX_SPEED_SLIDER_MAX;
		}
		return STANDARD_MAX_SPEED_SLIDER_MAX;
	}

	public static String formatSpeed(double value) {
		return String.format(java.util.Locale.ROOT, "%.2fx", value);
	}

	public static String formatScrollStep(double value) {
		return String.format(java.util.Locale.ROOT, "%.1f", value);
	}

	private static double roundToOneDecimal(double value) {
		return Math.round(value * 10.0D) / 10.0D;
	}

	private static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0D) / 100.0D;
	}

	private static double clampFinite(double value, double fallback, double min, double max) {
		double result = Double.isFinite(value) ? value : fallback;
		if (result < min) {
			return min;
		}
		if (result > max) {
			return max;
		}
		return result;
	}

	private static int clampInt(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	public static final class Values {
		public boolean fullRange = DEFAULT_FULL_RANGE;
		public double minSpeed = DEFAULT_MIN_SPEED;
		public double maxSpeed = DEFAULT_MAX_SPEED;
		public double initialSpeed = DEFAULT_INITIAL_SPEED;
		public double scrollStep = DEFAULT_SCROLL_STEP;
		public boolean resetOnAdjust = DEFAULT_RESET_ON_ADJUST;

		public static Values defaults() {
			return new Values();
		}

		public Values copy() {
			Values copy = new Values();
			copy.fullRange = this.fullRange;
			copy.minSpeed = this.minSpeed;
			copy.maxSpeed = this.maxSpeed;
			copy.initialSpeed = this.initialSpeed;
			copy.scrollStep = this.scrollStep;
			copy.resetOnAdjust = this.resetOnAdjust;
			return copy;
		}
	}
}

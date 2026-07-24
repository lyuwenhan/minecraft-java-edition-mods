package com.example.flyspeedmodifier;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.OptionInstance.UnitDouble;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class FlySpeedModifierOptions {
	private static final OptionInstance<Boolean> FULL_RANGE =
			OptionInstance.createBoolean(
					"option.fly-speed-modifier.full_range",
					value ->
							Tooltip.create(
									Component.translatable("option.fly-speed-modifier.full_range.tooltip")),
					FlySpeedModifierConfig.DEFAULT_FULL_RANGE,
					FlySpeedModifierOptions::onFullRangeChanged);
	private static final OptionInstance<Double> MAX_SPEED =
			new OptionInstance<>(
					"option.fly-speed-modifier.max_speed",
					value ->
							Tooltip.create(Component.translatable("option.fly-speed-modifier.max_speed.tooltip")),
					FlySpeedModifierOptions::speedText,
					UnitDouble.INSTANCE.xmap(
							FlySpeedModifierOptions::toMaxSpeed, FlySpeedModifierOptions::fromMaxSpeed),
					Codec.doubleRange(
							FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED,
							FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED),
					FlySpeedModifierConfig.DEFAULT_MAX_SPEED,
					FlySpeedModifierOptions::onMaxSpeedChanged);
	private static final OptionInstance<Double> MIN_SPEED =
			new OptionInstance<>(
					"option.fly-speed-modifier.min_speed",
					value ->
							Tooltip.create(Component.translatable("option.fly-speed-modifier.min_speed.tooltip")),
					FlySpeedModifierOptions::speedText,
					UnitDouble.INSTANCE.xmap(
							FlySpeedModifierOptions::toMinSpeed, FlySpeedModifierOptions::fromMinSpeed),
					Codec.doubleRange(
							FlySpeedModifierConfig.MIN_ALLOWED_MIN_SPEED,
							FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED),
					FlySpeedModifierConfig.DEFAULT_MIN_SPEED,
					FlySpeedModifierOptions::onMinSpeedChanged);
	private static final OptionInstance<Double> INITIAL_SPEED =
			new OptionInstance<>(
					"option.fly-speed-modifier.initial_speed",
					value ->
							Tooltip.create(
									Component.translatable("option.fly-speed-modifier.initial_speed.tooltip")),
					FlySpeedModifierOptions::speedText,
					UnitDouble.INSTANCE.xmap(
							FlySpeedModifierOptions::toInitialSpeed, FlySpeedModifierOptions::fromInitialSpeed),
					Codec.doubleRange(
							FlySpeedModifierConfig.MIN_ALLOWED_INITIAL_SPEED,
							FlySpeedModifierConfig.MAX_ALLOWED_INITIAL_SPEED),
					FlySpeedModifierConfig.DEFAULT_INITIAL_SPEED,
					FlySpeedModifierOptions::onInitialSpeedChanged);
	private static final OptionInstance<Boolean> RESET_ON_ADJUST =
			OptionInstance.createBoolean(
					"option.fly-speed-modifier.reset_on_adjust",
					value ->
							Tooltip.create(
									Component.translatable("option.fly-speed-modifier.reset_on_adjust.tooltip")),
					FlySpeedModifierConfig.DEFAULT_RESET_ON_ADJUST,
					FlySpeedModifierOptions::onResetOnAdjustChanged);
	private static final OptionInstance<Double> SCROLL_STEP =
			new OptionInstance<>(
					"option.fly-speed-modifier.scroll_step",
					value ->
							Tooltip.create(
									Component.translatable("option.fly-speed-modifier.scroll_step.tooltip")),
					FlySpeedModifierOptions::scrollStepText,
					UnitDouble.INSTANCE.xmap(
							FlySpeedModifierOptions::toScrollStep, FlySpeedModifierOptions::fromScrollStep),
					Codec.doubleRange(
							FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP,
							FlySpeedModifierConfig.MAX_ALLOWED_SCROLL_STEP),
					FlySpeedModifierConfig.DEFAULT_SCROLL_STEP,
					FlySpeedModifierOptions::onScrollStepChanged);

	private static final OptionInstance<Boolean> APPLY_TO_OTHER_MOVEMENT =
			OptionInstance.createBoolean(
					"option.fly-speed-modifier.apply_to_other_movement",
					value ->
							Tooltip.create(
									Component.translatable(
											"option.fly-speed-modifier.apply_to_other_movement.tooltip")),
					FlySpeedModifierConfig.DEFAULT_APPLY_TO_OTHER_MOVEMENT,
					FlySpeedModifierOptions::onApplyToOtherMovementChanged);

	private FlySpeedModifierOptions() {}

	public static OptionInstance<?>[] all() {
		return new OptionInstance<?>[] {
			fullRange(),
			maxSpeed(),
			minSpeed(),
			initialSpeed(),
			resetOnAdjust(),
			scrollStep(),
			applyToOtherMovement()
		};
	}

	public static OptionInstance<Boolean> fullRange() {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			FULL_RANGE.set(screen.draft().fullRange);
		}
		return FULL_RANGE;
	}

	public static OptionInstance<Double> maxSpeed() {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			MAX_SPEED.set(screen.draft().maxSpeed);
		}
		return MAX_SPEED;
	}

	public static OptionInstance<Double> minSpeed() {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			MIN_SPEED.set(screen.draft().minSpeed);
		}
		return MIN_SPEED;
	}

	public static OptionInstance<Double> initialSpeed() {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			INITIAL_SPEED.set(screen.draft().initialSpeed);
		}
		return INITIAL_SPEED;
	}

	public static OptionInstance<Boolean> resetOnAdjust() {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			RESET_ON_ADJUST.set(screen.draft().resetOnAdjust);
		}
		return RESET_ON_ADJUST;
	}

	public static OptionInstance<Boolean> applyToOtherMovement() {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			APPLY_TO_OTHER_MOVEMENT.set(screen.draft().applyToOtherMovement);
		}
		return APPLY_TO_OTHER_MOVEMENT;
	}

	public static OptionInstance<Double> scrollStep() {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			SCROLL_STEP.set(screen.draft().scrollStep);
		}
		return SCROLL_STEP;
	}

	private static void onFullRangeChanged(Boolean value) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen == null) {
			return;
		}
		screen.draft().fullRange = value;
		if (!value) {
			if (screen.draft().maxSpeed > FlySpeedModifierConfig.STANDARD_MAX_SPEED) {
				screen.draft().maxSpeed = FlySpeedModifierConfig.DEFAULT_MAX_SPEED;
				MAX_SPEED.set(screen.draft().maxSpeed);
			}
			if (screen.draft().initialSpeed > FlySpeedModifierConfig.STANDARD_MAX_SPEED) {
				screen.draft().initialSpeed = FlySpeedModifierConfig.DEFAULT_INITIAL_SPEED;
				INITIAL_SPEED.set(screen.draft().initialSpeed);
			}
		}
		constrainInitialSpeed(screen);
		screen.rescaleDynamicSliders();
	}

	private static void onMaxSpeedChanged(Double value) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen == null) {
			return;
		}
		screen.draft().maxSpeed = roundToTwoDecimals(value);
		constrainInitialSpeed(screen);
		screen.rescaleDynamicSliders();
	}

	private static void onMinSpeedChanged(Double value) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen == null) {
			return;
		}
		screen.draft().minSpeed = roundToTwoDecimals(value);
		constrainInitialSpeed(screen);
		screen.rescaleDynamicSliders();
	}

	private static void onInitialSpeedChanged(Double value) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().initialSpeed = roundToTwoDecimals(value);
		}
	}

	private static void onResetOnAdjustChanged(Boolean value) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().resetOnAdjust = value;
		}
	}

	private static void onApplyToOtherMovementChanged(Boolean value) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().applyToOtherMovement = value;
		}
	}

	private static void onScrollStepChanged(Double value) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().scrollStep = roundToOneDecimal(value);
		}
	}

	private static double toMaxSpeed(double normalized) {
		double upperBound =
				FULL_RANGE.get()
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		double result =
				FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED
						+ normalized * (upperBound - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED);
		return roundToTwoDecimals(result);
	}

	private static double fromMaxSpeed(double speed) {
		double upperBound =
				FULL_RANGE.get()
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		double clamped =
				Math.max(FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED, Math.min(speed, upperBound));
		return (clamped - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED)
				/ (upperBound - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED);
	}

	private static double toMinSpeed(double normalized) {
		return roundToTwoDecimals(normalized * FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED);
	}

	private static double fromMinSpeed(double speed) {
		return speed / FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED;
	}

	private static double toInitialSpeed(double normalized) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen == null) {
			return FlySpeedModifierConfig.DEFAULT_INITIAL_SPEED;
		}
		double lowerBound = screen.draft().minSpeed;
		double upperBound = effectiveMaximumSpeed(screen);
		if (upperBound <= lowerBound) {
			return roundToTwoDecimals(lowerBound);
		}
		return roundToTwoDecimals(lowerBound + normalized * (upperBound - lowerBound));
	}

	private static double fromInitialSpeed(double speed) {
		FlySpeedModifierConfigScreen screen = activeScreen();
		if (screen == null) {
			return 0.0D;
		}
		double lowerBound = screen.draft().minSpeed;
		double upperBound = effectiveMaximumSpeed(screen);
		if (upperBound <= lowerBound) {
			return 0.0D;
		}
		double clamped = Math.max(lowerBound, Math.min(speed, upperBound));
		return (clamped - lowerBound) / (upperBound - lowerBound);
	}

	private static double toScrollStep(double normalized) {
		double range =
				FlySpeedModifierConfig.MAX_ALLOWED_SCROLL_STEP
						- FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP;
		return roundToOneDecimal(FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP + normalized * range);
	}

	private static double fromScrollStep(double step) {
		double range =
				FlySpeedModifierConfig.MAX_ALLOWED_SCROLL_STEP
						- FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP;
		return (step - FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP) / range;
	}

	private static void constrainInitialSpeed(FlySpeedModifierConfigScreen screen) {
		double lowerBound = screen.draft().minSpeed;
		double upperBound = effectiveMaximumSpeed(screen);
		double constrained = Math.max(lowerBound, Math.min(screen.draft().initialSpeed, upperBound));
		if (Double.compare(constrained, screen.draft().initialSpeed) != 0) {
			screen.draft().initialSpeed = roundToTwoDecimals(constrained);
			INITIAL_SPEED.set(screen.draft().initialSpeed);
		}
	}

	private static double effectiveMaximumSpeed(FlySpeedModifierConfigScreen screen) {
		double fullRangeUpperBound =
				screen.draft().fullRange
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		return Math.max(
				screen.draft().minSpeed, Math.min(screen.draft().maxSpeed, fullRangeUpperBound));
	}

	private static Component speedText(Component optionText, Double value) {
		return Component.literal(
				optionText.getString() + ": " + FlySpeedModifierConfig.formatSpeed(value));
	}

	private static Component scrollStepText(Component optionText, Double value) {
		return Component.literal(
				optionText.getString() + ": " + FlySpeedModifierConfig.formatScrollStep(value));
	}

	private static FlySpeedModifierConfigScreen activeScreen() {
		Minecraft client = Minecraft.getInstance();
		if (client.screen instanceof FlySpeedModifierConfigScreen screen) {
			return screen;
		}
		return null;
	}

	private static double roundToOneDecimal(double value) {
		return Math.round(value * 10.0D) / 10.0D;
	}

	private static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0D) / 100.0D;
	}
}

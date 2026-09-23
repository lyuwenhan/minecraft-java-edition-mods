package com.example.doublejump;

import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.OptionInstance.UnitDouble;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class DoubleJumpOptions {
	private static final OptionInstance<Boolean> ENABLED =
			OptionInstance.createBoolean(
					"option.double-jump.enabled",
					value ->
							Tooltip.create(
									Component.translatable("option.double-jump.enabled.tooltip")),
					DoubleJumpConfig.DEFAULT_ENABLED,
					DoubleJumpOptions::onEnabledChanged);

	private static final OptionInstance<Integer> JUMP_COUNT =
			new OptionInstance<>(
					"option.double-jump.jump_count",
					value ->
							Tooltip.create(
									Component.translatable(
											"option.double-jump.jump_count.tooltip")),
					DoubleJumpOptions::jumpCountText,
					UnitDouble.INSTANCE.xmap(
							DoubleJumpOptions::toJumpCount, DoubleJumpOptions::fromJumpCount),
					Codec.intRange(
							DoubleJumpConfig.MIN_JUMP_COUNT,
							DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT),
					DoubleJumpConfig.DEFAULT_JUMP_COUNT,
					DoubleJumpOptions::onJumpCountChanged);

	private static final OptionInstance<Integer> COYOTE_TIME =
			new OptionInstance<>(
					"option.double-jump.coyote_time",
					value ->
							Tooltip.create(
									Component.translatable(
											"option.double-jump.coyote_time.tooltip")),
					DoubleJumpOptions::coyoteTimeText,
					UnitDouble.INSTANCE.xmap(
							DoubleJumpOptions::toCoyoteTime, DoubleJumpOptions::fromCoyoteTime),
					Codec.intRange(
							DoubleJumpConfig.MIN_COYOTE_TIME_TICKS,
							DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS),
					DoubleJumpConfig.DEFAULT_COYOTE_TIME_TICKS,
					DoubleJumpOptions::onCoyoteTimeChanged);

	private static final OptionInstance<Boolean> COOLDOWN_ENABLED =
			OptionInstance.createBoolean(
					"option.double-jump.cooldown_enabled",
					value ->
							Tooltip.create(
									Component.translatable(
											"option.double-jump.cooldown_enabled.tooltip")),
					DoubleJumpConfig.DEFAULT_COOLDOWN_ENABLED,
					DoubleJumpOptions::onCooldownEnabledChanged);

	private static final OptionInstance<Boolean> INFINITE_JUMPS =
			OptionInstance.createBoolean(
					"option.double-jump.infinite_jumps",
					value ->
							Tooltip.create(
									Component.translatable(
											"option.double-jump.infinite_jumps.tooltip")),
					DoubleJumpConfig.DEFAULT_INFINITE_JUMPS,
					DoubleJumpOptions::onInfiniteJumpsChanged);

	private DoubleJumpOptions() {}

	public static OptionInstance<?>[] all() {
		return new OptionInstance<?>[] {
			enabled(), infiniteJumps(), jumpCount(), coyoteTime(), cooldownEnabled()
		};
	}

	public static OptionInstance<Boolean> enabled() {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			ENABLED.set(screen.draft().enabled);
		}
		return ENABLED;
	}

	public static OptionInstance<Integer> jumpCount() {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.setJumpCountSliderValueSilently(
					clampJumpCountToSlider(screen.draft().jumpCount));
		}
		return JUMP_COUNT;
	}

	public static OptionInstance<Integer> coyoteTime() {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.setCoyoteTimeSliderValueSilently(
					clampCoyoteTimeToSlider(screen.draft().coyoteTimeTicks));
		}
		return COYOTE_TIME;
	}

	public static OptionInstance<Boolean> cooldownEnabled() {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			COOLDOWN_ENABLED.set(screen.draft().cooldownEnabled);
		}
		return COOLDOWN_ENABLED;
	}

	public static OptionInstance<Boolean> infiniteJumps() {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			INFINITE_JUMPS.set(screen.draft().infiniteJumps);
		}
		return INFINITE_JUMPS;
	}

	static void setJumpCountOption(int value) {
		JUMP_COUNT.set(clampJumpCountToSlider(value));
	}

	static void setCoyoteTimeOption(int value) {
		COYOTE_TIME.set(clampCoyoteTimeToSlider(value));
	}

	private static void onEnabledChanged(Boolean value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().enabled = value;
		}
	}

	private static void onJumpCountChanged(Integer value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen == null || screen.isSynchronizingJumpCountSlider()) {
			return;
		}
		screen.draft().jumpCount = value;
		screen.syncJumpCountInputFromSlider(value);
	}

	private static void onCoyoteTimeChanged(Integer value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen == null || screen.isSynchronizingCoyoteTimeSlider()) {
			return;
		}
		screen.draft().coyoteTimeTicks = value;
		screen.syncCoyoteTimeInputFromSlider(value);
	}

	private static void onCooldownEnabledChanged(Boolean value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().cooldownEnabled = value;
		}
	}

	private static void onInfiniteJumpsChanged(Boolean value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().infiniteJumps = value;
		}
	}

	private static int toJumpCount(double normalized) {
		double range = DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT - DoubleJumpConfig.MIN_JUMP_COUNT;
		return clampJumpCountToSlider(
				(int) Math.round(DoubleJumpConfig.MIN_JUMP_COUNT + normalized * range));
	}

	private static double fromJumpCount(int count) {
		double range = DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT - DoubleJumpConfig.MIN_JUMP_COUNT;
		return (clampJumpCountToSlider(count) - DoubleJumpConfig.MIN_JUMP_COUNT) / range;
	}

	private static int toCoyoteTime(double normalized) {
		double range =
				DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS
						- DoubleJumpConfig.MIN_COYOTE_TIME_TICKS;
		return clampCoyoteTimeToSlider(
				(int) Math.round(DoubleJumpConfig.MIN_COYOTE_TIME_TICKS + normalized * range));
	}

	private static double fromCoyoteTime(int ticks) {
		double range =
				DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS
						- DoubleJumpConfig.MIN_COYOTE_TIME_TICKS;
		return (clampCoyoteTimeToSlider(ticks) - DoubleJumpConfig.MIN_COYOTE_TIME_TICKS) / range;
	}

	private static Component jumpCountText(Component optionText, Integer value) {
		return Component.literal(optionText.getString() + ": " + value);
	}

	private static Component coyoteTimeText(Component optionText, Integer value) {
		return Component.literal(optionText.getString() + ": " + value);
	}

	private static int clampJumpCountToSlider(int value) {
		if (value < DoubleJumpConfig.MIN_JUMP_COUNT) {
			return DoubleJumpConfig.MIN_JUMP_COUNT;
		}
		if (value > DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT) {
			return DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT;
		}
		return value;
	}

	private static int clampCoyoteTimeToSlider(int value) {
		if (value < DoubleJumpConfig.MIN_COYOTE_TIME_TICKS) {
			return DoubleJumpConfig.MIN_COYOTE_TIME_TICKS;
		}
		if (value > DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS) {
			return DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS;
		}
		return value;
	}

	private static DoubleJumpConfigScreen activeScreen() {
		Minecraft client = Minecraft.getInstance();
		if (client.gui.screen() instanceof DoubleJumpConfigScreen screen) {
			return screen;
		}
		return null;
	}
}

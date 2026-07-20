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
					value -> Tooltip.create(Component.translatable("option.double-jump.enabled.tooltip")),
					DoubleJumpConfig.DEFAULT_ENABLED,
					DoubleJumpOptions::onEnabledChanged);

	private static final OptionInstance<Integer> JUMP_COUNT =
			new OptionInstance<>(
					"option.double-jump.jump_count",
					value -> Tooltip.create(Component.translatable("option.double-jump.jump_count.tooltip")),
					DoubleJumpOptions::jumpCountText,
					UnitDouble.INSTANCE.xmap(
							DoubleJumpOptions::toJumpCount, DoubleJumpOptions::fromJumpCount),
					Codec.intRange(DoubleJumpConfig.MIN_JUMP_COUNT, DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT),
					DoubleJumpConfig.DEFAULT_JUMP_COUNT,
					DoubleJumpOptions::onJumpCountChanged);

	private static final OptionInstance<Boolean> INFINITE_JUMPS =
			OptionInstance.createBoolean(
					"option.double-jump.infinite_jumps",
					value ->
							Tooltip.create(Component.translatable("option.double-jump.infinite_jumps.tooltip")),
					DoubleJumpConfig.DEFAULT_INFINITE_JUMPS,
					DoubleJumpOptions::onInfiniteJumpsChanged);

	private DoubleJumpOptions() {}

	public static OptionInstance<?>[] all() {
		return new OptionInstance<?>[] {enabled(), infiniteJumps(), jumpCount()};
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
			screen.setSliderValueSilently(clampToSlider(screen.draft().jumpCount));
		}
		return JUMP_COUNT;
	}

	public static OptionInstance<Boolean> infiniteJumps() {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			INFINITE_JUMPS.set(screen.draft().infiniteJumps);
		}
		return INFINITE_JUMPS;
	}

	static void setJumpCountOption(int value) {
		JUMP_COUNT.set(clampToSlider(value));
	}

	private static void onEnabledChanged(Boolean value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().enabled = value;
		}
	}

	private static void onJumpCountChanged(Integer value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen == null || screen.isSynchronizingSlider()) {
			return;
		}
		screen.draft().jumpCount = value;
		screen.syncInputFromSlider(value);
	}

	private static void onInfiniteJumpsChanged(Boolean value) {
		DoubleJumpConfigScreen screen = activeScreen();
		if (screen != null) {
			screen.draft().infiniteJumps = value;
		}
	}

	private static int toJumpCount(double normalized) {
		double range = DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT - DoubleJumpConfig.MIN_JUMP_COUNT;
		return clampToSlider((int) Math.round(DoubleJumpConfig.MIN_JUMP_COUNT + normalized * range));
	}

	private static double fromJumpCount(int count) {
		double range = DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT - DoubleJumpConfig.MIN_JUMP_COUNT;
		return (clampToSlider(count) - DoubleJumpConfig.MIN_JUMP_COUNT) / range;
	}

	private static Component jumpCountText(Component optionText, Integer value) {
		return Component.literal(optionText.getString() + ": " + value);
	}

	private static int clampToSlider(int value) {
		if (value < DoubleJumpConfig.MIN_JUMP_COUNT) {
			return DoubleJumpConfig.MIN_JUMP_COUNT;
		}
		if (value > DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT) {
			return DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT;
		}
		return value;
	}

	private static DoubleJumpConfigScreen activeScreen() {
		Minecraft client = Minecraft.getInstance();
		if (client.screen instanceof DoubleJumpConfigScreen screen) {
			return screen;
		}
		return null;
	}
}

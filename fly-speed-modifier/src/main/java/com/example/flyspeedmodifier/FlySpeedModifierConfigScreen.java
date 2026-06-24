package com.example.flyspeedmodifier;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FlySpeedModifierConfigScreen {
	private FlySpeedModifierConfigScreen() {
	}

	public static Screen create(Screen parent) {
		FlySpeedModifierConfig.Values draft = FlySpeedModifierConfig.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("title.fly-speed-modifier.config"));

		builder.setSavingRunnable(() -> FlySpeedModifierConfig.set(draft));

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.fly-speed-modifier.general"));

		general.addEntry(entryBuilder.startIntSlider(
						Component.translatable("option.fly-speed-modifier.max_speed"),
						FlySpeedModifierConfig.maxSpeedToSlider(draft.maxSpeed),
						FlySpeedModifierConfig.MAX_SPEED_SLIDER_MIN,
						FlySpeedModifierConfig.MAX_SPEED_SLIDER_MAX
				)
				.setDefaultValue(FlySpeedModifierConfig.maxSpeedToSlider(FlySpeedModifierConfig.DEFAULT_MAX_SPEED))
				.setTextGetter(value -> Component.literal(FlySpeedModifierConfig.formatSpeed(FlySpeedModifierConfig.sliderToMaxSpeed(value))))
				.setTooltip(Component.translatable("option.fly-speed-modifier.max_speed.tooltip"))
				.setSaveConsumer(value -> draft.maxSpeed = FlySpeedModifierConfig.sliderToMaxSpeed(value))
				.build());

		general.addEntry(entryBuilder.startIntSlider(
						Component.translatable("option.fly-speed-modifier.min_speed"),
						FlySpeedModifierConfig.minSpeedToSlider(draft.minSpeed),
						FlySpeedModifierConfig.MIN_SPEED_SLIDER_MIN,
						FlySpeedModifierConfig.MIN_SPEED_SLIDER_MAX
				)
				.setDefaultValue(FlySpeedModifierConfig.minSpeedToSlider(FlySpeedModifierConfig.DEFAULT_MIN_SPEED))
				.setTextGetter(value -> Component.literal(FlySpeedModifierConfig.formatSpeed(FlySpeedModifierConfig.sliderToMinSpeed(value))))
				.setTooltip(Component.translatable("option.fly-speed-modifier.min_speed.tooltip"))
				.setSaveConsumer(value -> draft.minSpeed = FlySpeedModifierConfig.sliderToMinSpeed(value))
				.build());

		general.addEntry(entryBuilder.startIntSlider(
						Component.translatable("option.fly-speed-modifier.scroll_step"),
						FlySpeedModifierConfig.scrollStepToSlider(draft.scrollStep),
						FlySpeedModifierConfig.SCROLL_STEP_SLIDER_MIN,
						FlySpeedModifierConfig.SCROLL_STEP_SLIDER_MAX
				)
				.setDefaultValue(FlySpeedModifierConfig.scrollStepToSlider(FlySpeedModifierConfig.DEFAULT_SCROLL_STEP))
				.setTextGetter(value -> Component.literal(FlySpeedModifierConfig.formatScrollStep(FlySpeedModifierConfig.sliderToScrollStep(value))))
				.setTooltip(Component.translatable("option.fly-speed-modifier.scroll_step.tooltip"))
				.setSaveConsumer(value -> draft.scrollStep = FlySpeedModifierConfig.sliderToScrollStep(value))
				.build());

		return builder.build();
	}
}

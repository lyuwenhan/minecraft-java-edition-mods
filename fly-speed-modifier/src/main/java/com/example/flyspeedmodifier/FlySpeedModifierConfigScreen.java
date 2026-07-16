package com.example.flyspeedmodifier;

import com.example.flyspeedmodifier.mixin.AbstractSliderButtonAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class FlySpeedModifierConfigScreen extends OptionsSubScreen {
	private final FlySpeedModifierConfig.Values draft;
	private boolean saved;

	public FlySpeedModifierConfigScreen(Screen parent) {
		super(
				parent,
				Minecraft.getInstance().options,
				Component.translatable("title.fly-speed-modifier.config"));
		this.draft = FlySpeedModifierConfig.get();
	}

	public static Screen create(Screen parent) {
		return new FlySpeedModifierConfigScreen(parent);
	}

	FlySpeedModifierConfig.Values draft() {
		return this.draft;
	}

	@Override
	protected void addOptions() {
		this.list.addSmall(FlySpeedModifierOptions.all());
		this.resetWidgets();
	}

	public void rescaleDynamicSliders() {
		this.resetSliderWidget(FlySpeedModifierOptions.maxSpeed(), normalizedMaxSpeed());
		this.resetSliderWidget(FlySpeedModifierOptions.initialSpeed(), normalizedInitialSpeed());
	}

	private void resetWidgets() {
		this.rescaleDynamicSliders();
	}

	private void resetSliderWidget(OptionInstance<Double> option, double normalizedValue) {
		var widget = this.list.findOption(option);
		if (widget instanceof AbstractSliderButton slider) {
			((AbstractSliderButtonAccessor) slider).flySpeedModifier$invokeSetValue(normalizedValue);
		}
	}

	private double normalizedMaxSpeed() {
		double upperBound =
				this.draft.fullRange
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		double clamped =
				Math.max(
						FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED,
						Math.min(this.draft.maxSpeed, upperBound));
		return (clamped - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED)
				/ (upperBound - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED);
	}

	private double normalizedInitialSpeed() {
		double lowerBound = this.draft.minSpeed;
		double upperBound = effectiveMaximumSpeed();
		if (upperBound <= lowerBound) {
			return 0.0D;
		}

		double clamped = Math.max(lowerBound, Math.min(this.draft.initialSpeed, upperBound));
		return (clamped - lowerBound) / (upperBound - lowerBound);
	}

	private double effectiveMaximumSpeed() {
		double fullRangeUpperBound =
				this.draft.fullRange
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		return Math.max(this.draft.minSpeed, Math.min(this.draft.maxSpeed, fullRangeUpperBound));
	}

	@Override
	public void onClose() {
		this.saveOnce();
		super.onClose();
	}

	@Override
	public void removed() {
		this.saveOnce();
		super.removed();
	}

	private void saveOnce() {
		if (this.saved) {
			return;
		}
		this.saved = true;
		FlySpeedModifierConfig.set(this.draft);
	}
}

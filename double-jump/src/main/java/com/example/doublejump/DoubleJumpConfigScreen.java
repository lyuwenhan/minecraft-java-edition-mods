package com.example.doublejump;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class DoubleJumpConfigScreen extends OptionsSubScreen {
	private final DoubleJumpConfig.Values draft;

	private EditBox jumpCountInput;
	private EditBox coyoteTimeInput;
	private EditBox cooldownTicksInput;
	private boolean saved;
	private boolean synchronizingJumpCountInput;
	private boolean synchronizingJumpCountSlider;
	private boolean synchronizingCoyoteTimeInput;
	private boolean synchronizingCoyoteTimeSlider;
	private String lastValidJumpCountInput;
	private String lastValidCoyoteTimeInput;
	private String lastValidCooldownInput;

	public DoubleJumpConfigScreen(Screen parent) {
		super(
				parent,
				Minecraft.getInstance().options,
				Component.translatable("title.double-jump.config"));

		this.draft = DoubleJumpConfig.get();
	}

	public static Screen create(Screen parent) {
		return new DoubleJumpConfigScreen(parent);
	}

	DoubleJumpConfig.Values draft() {
		return this.draft;
	}

	boolean isSynchronizingJumpCountSlider() {
		return this.synchronizingJumpCountSlider;
	}

	boolean isSynchronizingCoyoteTimeSlider() {
		return this.synchronizingCoyoteTimeSlider;
	}

	void setJumpCountSliderValueSilently(int value) {
		this.synchronizingJumpCountSlider = true;
		DoubleJumpOptions.setJumpCountOption(value);
		this.synchronizingJumpCountSlider = false;
	}

	void setCoyoteTimeSliderValueSilently(int value) {
		this.synchronizingCoyoteTimeSlider = true;
		DoubleJumpOptions.setCoyoteTimeOption(value);
		this.synchronizingCoyoteTimeSlider = false;
	}

	void syncJumpCountInputFromSlider(int value) {
		if (this.jumpCountInput == null) {
			return;
		}

		this.synchronizingJumpCountInput = true;
		this.lastValidJumpCountInput = Integer.toString(value);
		this.jumpCountInput.setValue(this.lastValidJumpCountInput);
		this.synchronizingJumpCountInput = false;
	}

	void syncCoyoteTimeInputFromSlider(int value) {
		if (this.coyoteTimeInput == null) {
			return;
		}

		this.synchronizingCoyoteTimeInput = true;
		this.lastValidCoyoteTimeInput = Integer.toString(value);
		this.coyoteTimeInput.setValue(this.lastValidCoyoteTimeInput);
		this.synchronizingCoyoteTimeInput = false;
	}

	@Override
	protected void addOptions() {
		AbstractWidget enabledWidget = DoubleJumpOptions.enabled().createButton(this.options);

		AbstractWidget infiniteJumpsWidget =
				DoubleJumpOptions.infiniteJumps().createButton(this.options);

		this.list.addSmall(enabledWidget, infiniteJumpsWidget);

		this.jumpCountInput =
				new EditBox(
						this.font,
						0,
						0,
						150,
						20,
						Component.translatable("option.double-jump.jump_count_input"));

		this.jumpCountInput.setMaxLength(10);
		this.lastValidJumpCountInput = Integer.toString(this.draft.jumpCount);
		this.jumpCountInput.setValue(this.lastValidJumpCountInput);
		this.jumpCountInput.setResponder(this::onJumpCountInputChanged);

		AbstractWidget jumpCountWidget = DoubleJumpOptions.jumpCount().createButton(this.options);
		this.list.addSmall(jumpCountWidget, this.jumpCountInput);

		this.coyoteTimeInput =
				new EditBox(
						this.font,
						0,
						0,
						150,
						20,
						Component.translatable("option.double-jump.coyote_time_input"));

		this.coyoteTimeInput.setMaxLength(10);
		this.lastValidCoyoteTimeInput = Integer.toString(this.draft.coyoteTimeTicks);
		this.coyoteTimeInput.setValue(this.lastValidCoyoteTimeInput);
		this.coyoteTimeInput.setResponder(this::onCoyoteTimeInputChanged);

		AbstractWidget coyoteTimeWidget = DoubleJumpOptions.coyoteTime().createButton(this.options);
		this.list.addSmall(coyoteTimeWidget, this.coyoteTimeInput);

		AbstractWidget cooldownEnabledWidget =
				DoubleJumpOptions.cooldownEnabled().createButton(this.options);

		this.cooldownTicksInput =
				new EditBox(
						this.font,
						0,
						0,
						150,
						20,
						Component.translatable("option.double-jump.cooldown_ticks"));

		this.cooldownTicksInput.setMaxLength(10);
		this.lastValidCooldownInput = Integer.toString(this.draft.cooldownTicks);
		this.cooldownTicksInput.setValue(this.lastValidCooldownInput);
		this.cooldownTicksInput.setResponder(this::onCooldownInputChanged);

		this.list.addSmall(cooldownEnabledWidget, this.cooldownTicksInput);
	}

	private void onJumpCountInputChanged(String value) {
		if (this.synchronizingJumpCountInput) {
			return;
		}

		Integer parsedValue = parseNonNegativeInteger(value, this::restoreLastValidJumpCountInput);
		if (parsedValue == null) {
			return;
		}

		this.lastValidJumpCountInput = value;
		if (parsedValue < DoubleJumpConfig.MIN_JUMP_COUNT) {
			return;
		}

		this.draft.jumpCount = parsedValue;
		this.setJumpCountSliderValueSilently(parsedValue);
	}

	private void onCoyoteTimeInputChanged(String value) {
		if (this.synchronizingCoyoteTimeInput) {
			return;
		}

		Integer parsedValue = parseNonNegativeInteger(value, this::restoreLastValidCoyoteTimeInput);
		if (parsedValue == null) {
			return;
		}

		this.lastValidCoyoteTimeInput = value;
		this.draft.coyoteTimeTicks = parsedValue;
		this.setCoyoteTimeSliderValueSilently(parsedValue);
	}

	private void onCooldownInputChanged(String value) {
		Integer parsedValue = parseNonNegativeInteger(value, this::restoreLastValidCooldownInput);
		if (parsedValue == null) {
			return;
		}

		this.lastValidCooldownInput = value;
		this.draft.cooldownTicks = parsedValue;
	}

	private Integer parseNonNegativeInteger(String value, Runnable restoreAction) {
		if (value.isEmpty()) {
			return null;
		}

		for (int index = 0; index < value.length(); index++) {
			if (!Character.isDigit(value.charAt(index))) {
				restoreAction.run();
				return null;
			}
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ignored) {
			restoreAction.run();
			return null;
		}
	}

	private void restoreLastValidCooldownInput() {
		if (this.cooldownTicksInput != null) {
			this.cooldownTicksInput.setValue(this.lastValidCooldownInput);
		}
	}

	private void restoreLastValidJumpCountInput() {
		if (this.jumpCountInput == null) {
			return;
		}

		this.synchronizingJumpCountInput = true;
		this.jumpCountInput.setValue(this.lastValidJumpCountInput);
		this.synchronizingJumpCountInput = false;
	}

	private void restoreLastValidCoyoteTimeInput() {
		if (this.coyoteTimeInput == null) {
			return;
		}

		this.synchronizingCoyoteTimeInput = true;
		this.coyoteTimeInput.setValue(this.lastValidCoyoteTimeInput);
		this.synchronizingCoyoteTimeInput = false;
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
		this.applyInputValue(
				this.jumpCountInput,
				DoubleJumpConfig.MIN_JUMP_COUNT,
				value -> this.draft.jumpCount = value);
		this.applyInputValue(
				this.coyoteTimeInput,
				DoubleJumpConfig.MIN_COYOTE_TIME_TICKS,
				value -> this.draft.coyoteTimeTicks = value);
		this.applyInputValue(
				this.cooldownTicksInput,
				DoubleJumpConfig.MIN_COOLDOWN_TICKS,
				value -> this.draft.cooldownTicks = value);

		DoubleJumpConfig.set(this.draft);
	}

	private void applyInputValue(
			EditBox input, int minimum, java.util.function.IntConsumer setter) {
		if (input == null || input.getValue().isEmpty()) {
			return;
		}

		try {
			int parsedValue = Integer.parseInt(input.getValue());
			if (parsedValue >= minimum) {
				setter.accept(parsedValue);
			}
		} catch (NumberFormatException ignored) {
		}
	}
}

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
	private boolean saved;
	private boolean synchronizingInput;
	private boolean synchronizingSlider;
	private String lastValidInput;

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

	boolean isSynchronizingSlider() {
		return this.synchronizingSlider;
	}

	void setSliderValueSilently(int value) {
		this.synchronizingSlider = true;
		DoubleJumpOptions.setJumpCountOption(value);
		this.synchronizingSlider = false;
	}

	void syncInputFromSlider(int value) {
		if (this.jumpCountInput == null) {
			return;
		}

		this.synchronizingInput = true;
		this.lastValidInput = Integer.toString(value);
		this.jumpCountInput.setValue(this.lastValidInput);
		this.synchronizingInput = false;
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

		this.lastValidInput = Integer.toString(this.draft.jumpCount);

		this.jumpCountInput.setValue(this.lastValidInput);
		this.jumpCountInput.setResponder(this::onInputChanged);

		AbstractWidget jumpCountWidget = DoubleJumpOptions.jumpCount().createButton(this.options);

		this.list.addSmall(jumpCountWidget, this.jumpCountInput);
	}

	private void onInputChanged(String value) {
		if (this.synchronizingInput) {
			return;
		}

		if (value.isEmpty()) {
			return;
		}

		for (int index = 0; index < value.length(); index++) {
			char currentCharacter = value.charAt(index);

			if (!Character.isDigit(currentCharacter)) {
				this.restoreLastValidInput();
				return;
			}
		}

		try {
			int parsedValue = Integer.parseInt(value);

			this.lastValidInput = value;

			if (parsedValue < DoubleJumpConfig.MIN_JUMP_COUNT) {
				return;
			}

			this.draft.jumpCount = parsedValue;
			this.setSliderValueSilently(parsedValue);
		} catch (NumberFormatException ignored) {
			this.restoreLastValidInput();
		}
	}

	private void restoreLastValidInput() {
		if (this.jumpCountInput == null) {
			return;
		}

		this.synchronizingInput = true;
		this.jumpCountInput.setValue(this.lastValidInput);
		this.synchronizingInput = false;
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

		if (this.jumpCountInput != null) {
			String value = this.jumpCountInput.getValue();

			if (!value.isEmpty()) {
				try {
					int parsedValue = Integer.parseInt(value);

					if (parsedValue >= DoubleJumpConfig.MIN_JUMP_COUNT) {
						this.draft.jumpCount = parsedValue;
					}
				} catch (NumberFormatException ignored) {
				}
			}
		}

		DoubleJumpConfig.set(this.draft);
	}
}

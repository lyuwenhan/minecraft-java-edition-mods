package com.example.boatutils;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class BoatUtilsConfigScreen extends OptionsSubScreen {
	private final BoatUtilsConfig.Values draft;
	private boolean saved;

	private final OptionInstance<Boolean> unrestrictedViewRotation;
	private final OptionInstance<Boolean> viewDirectionLockEnabled;
	private final OptionInstance<Boolean> blueIceSpeedEverywhere;
	private final OptionInstance<Boolean> preventSinking;
	private final OptionInstance<Integer> boatStepHeight;

	public BoatUtilsConfigScreen(Screen parent) {
		super(
				parent, Minecraft.getInstance().options, Component.translatable("title.boat-utils.config"));
		this.draft = BoatUtilsConfig.get();

		this.unrestrictedViewRotation =
				OptionInstance.createBoolean(
						"option.boat-utils.unrestricted_view_rotation",
						value ->
								Tooltip.create(
										Component.translatable("option.boat-utils.unrestricted_view_rotation.tooltip")),
						this.draft.unrestrictedViewRotation,
						value -> this.draft.unrestrictedViewRotation = value);

		this.viewDirectionLockEnabled =
				OptionInstance.createBoolean(
						"option.boat-utils.view_direction_lock_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.view_direction_lock_enabled.tooltip")),
						this.draft.viewDirectionLockEnabled,
						value -> this.draft.viewDirectionLockEnabled = value);

		this.blueIceSpeedEverywhere =
				OptionInstance.createBoolean(
						"option.boat-utils.blue_ice_speed_everywhere",
						value ->
								Tooltip.create(
										Component.translatable("option.boat-utils.blue_ice_speed_everywhere.tooltip")),
						this.draft.blueIceSpeedEverywhere,
						value -> this.draft.blueIceSpeedEverywhere = value);

		this.preventSinking =
				OptionInstance.createBoolean(
						"option.boat-utils.prevent_sinking",
						value ->
								Tooltip.create(Component.translatable("option.boat-utils.prevent_sinking.tooltip")),
						this.draft.preventSinking,
						value -> this.draft.preventSinking = value);

		this.boatStepHeight =
				new OptionInstance<>(
						"option.boat-utils.boat_step_height",
						value ->
								Tooltip.create(
										Component.translatable("option.boat-utils.boat_step_height.tooltip")),
						(optionText, value) ->
								Component.translatable(
										"option.boat-utils.boat_step_height.value",
										String.format(Locale.ROOT, "%.1f", value / 10.0D)),
						new OptionInstance.IntRange(0, 100),
						Math.round(this.draft.boatStepHeight * 10.0F),
						value -> this.draft.boatStepHeight = value / 10.0F);
	}

	public static Screen create(Screen parent) {
		return new BoatUtilsConfigScreen(parent);
	}

	@Override
	protected void addOptions() {
		this.list.addSmall(
				this.unrestrictedViewRotation,
				this.viewDirectionLockEnabled,
				this.blueIceSpeedEverywhere,
				this.preventSinking,
				this.boatStepHeight);
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
		BoatUtilsConfig.set(this.draft);
	}
}

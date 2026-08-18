package com.example.boatutils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class BoatUtilsConfigScreen extends OptionsSubScreen {
	private final BoatUtilsConfig.Values draft;
	private boolean saved;

	private final OptionInstance<Boolean> unrestrictedViewRotation;
	private final OptionInstance<Boolean> viewDirectionLockEnabled;
	private final OptionInstance<Boolean> directionHotkeysEnabled;
	private final OptionInstance<Boolean> directionHotkeysUse45DegreeAngles;
	private final OptionInstance<Boolean> blueIceSpeedEverywhere;
	private final OptionInstance<Boolean> preventSinking;
	private final OptionInstance<Integer> boatStepHeight;
	private final OptionInstance<Boolean> handbrakeEnabled;
	private final OptionInstance<Boolean> handbrakeBoostEnabled;
	private final OptionInstance<Boolean> lateralFrictionEnabled;

	public BoatUtilsConfigScreen(Screen parent) {
		super(
				parent,
				Minecraft.getInstance().options,
				Component.translatable("title.boat-utils.config"));
		this.draft = BoatUtilsConfig.get();

		this.unrestrictedViewRotation =
				OptionInstance.createBoolean(
						"option.boat-utils.unrestricted_view_rotation",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.unrestricted_view_rotation.tooltip")),
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
		this.directionHotkeysEnabled =
				OptionInstance.createBoolean(
						"option.boat-utils.direction_hotkeys_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.direction_hotkeys_enabled.tooltip")),
						this.draft.directionHotkeysEnabled,
						value -> this.draft.directionHotkeysEnabled = value);
		this.directionHotkeysUse45DegreeAngles =
				OptionInstance.createBoolean(
						"option.boat-utils.direction_hotkeys_use_45_degree_angles",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.direction_hotkeys_use_45_degree_angles.tooltip")),
						this.draft.directionHotkeysUse45DegreeAngles,
						value -> this.draft.directionHotkeysUse45DegreeAngles = value);
		this.blueIceSpeedEverywhere =
				OptionInstance.createBoolean(
						"option.boat-utils.blue_ice_speed_everywhere",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.blue_ice_speed_everywhere.tooltip")),
						this.draft.blueIceSpeedEverywhere,
						value -> this.draft.blueIceSpeedEverywhere = value);
		this.preventSinking =
				OptionInstance.createBoolean(
						"option.boat-utils.prevent_sinking",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.prevent_sinking.tooltip")),
						this.draft.preventSinking,
						value -> this.draft.preventSinking = value);
		this.handbrakeEnabled =
				OptionInstance.createBoolean(
						"option.boat-utils.handbrake_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.handbrake_enabled.tooltip")),
						this.draft.handbrakeEnabled,
						value -> this.draft.handbrakeEnabled = value);
		this.handbrakeBoostEnabled =
				OptionInstance.createBoolean(
						"option.boat-utils.handbrake_boost_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.handbrake_boost_enabled.tooltip")),
						this.draft.handbrakeBoostEnabled,
						value -> this.draft.handbrakeBoostEnabled = value);
		this.lateralFrictionEnabled =
				OptionInstance.createBoolean(
						"option.boat-utils.lateral_friction_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.lateral_friction_enabled.tooltip")),
						this.draft.lateralFrictionEnabled,
						value -> this.draft.lateralFrictionEnabled = value);
		this.boatStepHeight =
				new OptionInstance<>(
						"option.boat-utils.boat_step_height",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.boat-utils.boat_step_height.tooltip")),
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
				this.directionHotkeysEnabled,
				this.directionHotkeysUse45DegreeAngles,
				this.blueIceSpeedEverywhere,
				this.preventSinking,
				this.handbrakeEnabled,
				this.handbrakeBoostEnabled,
				this.lateralFrictionEnabled);

		this.list.addSmall(this.boatStepHeight);
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

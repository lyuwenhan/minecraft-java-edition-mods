package com.example.wenhanclient.client.compat;

import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageConfig;
import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageSubMod;
import com.example.wenhanclient.betterstep.BetterStepConfig;
import com.example.wenhanclient.betterstep.BetterStepSubMod;
import com.example.wenhanclient.boatutils.BoatUtilsConfig;
import com.example.wenhanclient.creativeflying.CreativeFlyingConfig;
import com.example.wenhanclient.creativeflying.CreativeFlyingSubMod;
import com.example.wenhanclient.doublejump.DoubleJumpConfig;
import com.example.wenhanclient.elytracancel.ElytraCancelConfig;
import com.example.wenhanclient.elytracancel.ElytraCancelSubMod;
import com.example.wenhanclient.flightdisabler.FlightDisablerConfig;
import com.example.wenhanclient.flightdisabler.FlightDisablerSubMod;
import com.example.wenhanclient.flyspeedmodifier.FlySpeedModifierConfig;
import com.example.wenhanclient.flyspeedmodifier.mixin.AbstractSliderButtonAccessor;
import com.example.wenhanclient.hidepassword.HidePasswordSubMod;
import com.example.wenhanclient.hidepassword.config.HidePasswordConfig;
import com.example.wenhanclient.hiderealms.HideRealmsConfig;
import com.example.wenhanclient.hiderealms.HideRealmsSubMod;
import com.example.wenhanclient.interactiondelay.InteractionDelayConfig;
import com.example.wenhanclient.interactiondelay.InteractionDelaySubMod;
import com.example.wenhanclient.minecartdirection.MinecartDirectionConfig;
import com.example.wenhanclient.minecartdirection.MinecartDirectionSubMod;
import com.example.wenhanclient.nocollision.NoCollisionConfig;
import com.example.wenhanclient.nocollision.NoCollisionSubMod;
import com.example.wenhanclient.noflightreset.NoFlightResetConfig;
import com.example.wenhanclient.noflightreset.NoFlightResetSubMod;
import com.example.wenhanclient.nofog.NoFogConfig;
import com.example.wenhanclient.nofog.NoFogSubMod;
import com.example.wenhanclient.nojumpdelay.NoJumpDelayConfig;
import com.example.wenhanclient.nojumpdelay.NoJumpDelaySubMod;
import com.example.wenhanclient.noslimebounce.NoSlimeBounceConfig;
import com.example.wenhanclient.noslimebounce.NoSlimeBounceSubMod;
import com.example.wenhanclient.noslowdown.NoSlowdownConfig;
import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;
import com.example.wenhanclient.notexturerotation.NoTextureRotationConfig;
import com.example.wenhanclient.notexturerotation.NoTextureRotationSubMod;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterConfig;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterSubMod;
import com.example.wenhanclient.transparentblocks.TransparentBlocksConfig;
import com.example.wenhanclient.transparentblocks.TransparentBlocksSubMod;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.OptionInstance.UnitDouble;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.IntConsumer;

public final class WenhanClientConfigScreen extends OptionsSubScreen {
	private final BoatUtilsConfig.Values boatUtilsDraft;
	private final AttackThroughFoliageConfig attackThroughFoliageDraft;
	private final BetterStepConfig betterStepDraft;
	private final NoSlowdownConfig noSlowdownDraft;
	private final NoSlimeBounceConfig noSlimeBounceDraft;
	private final CreativeFlyingConfig creativeFlyingDraft;
	private final DoubleJumpConfig.Values doubleJumpDraft;
	private final ElytraCancelConfig elytraCancelDraft;
	private final FlightDisablerConfig flightDisablerDraft;
	private final FlySpeedModifierConfig.Values flySpeedModifierDraft;
	private final HideRealmsConfig hideRealmsDraft;
	private final HidePasswordConfig hidePasswordDraft;
	private final InteractionDelayConfig interactionDelayDraft;
	private final MinecartDirectionConfig minecartDirectionDraft;
	private final NoCollisionConfig noCollisionDraft;
	private final NoFlightResetConfig noFlightResetDraft;
	private final NoFogConfig noFogDraft;
	private final NoJumpDelayConfig noJumpDelayDraft;
	private final NoTextureRotationConfig noTextureRotationDraft;
	private final PlayerHighlighterConfig playerHighlighterDraft;
	private final TransparentBlocksConfig transparentBlocksDraft;
	private boolean saved;

	private final OptionInstance<Boolean> attackThroughFoliageEnabled;
	private final OptionInstance<Boolean> betterStepAirStepUpEnabled;
	private final OptionInstance<Boolean> betterStepStepDownEnabled;
	private final OptionInstance<Integer> betterStepStepHeight;
	private final OptionInstance<Boolean> noSlowdownEnabled;
	private final OptionInstance<Boolean> noSlimeBounceEnabled;
	private final OptionInstance<Boolean> creativeFlyingEnabled;
	private final OptionInstance<Boolean> doubleJumpEnabled;
	private final OptionInstance<Boolean> doubleJumpInfiniteJumps;
	private final OptionInstance<Integer> doubleJumpJumpCount;
	private final OptionInstance<Integer> doubleJumpCoyoteTime;
	private final OptionInstance<Boolean> doubleJumpCooldownEnabled;
	private final OptionInstance<Boolean> elytraCancelEnabled;
	private final OptionInstance<Boolean> flightDisablerEnabled;
	private final OptionInstance<Boolean> flySpeedModifierFullRange;
	private final OptionInstance<Double> flySpeedModifierMaxSpeed;
	private final OptionInstance<Double> flySpeedModifierMinSpeed;
	private final OptionInstance<Double> flySpeedModifierInitialSpeed;
	private final OptionInstance<Boolean> flySpeedModifierResetOnAdjust;
	private final OptionInstance<Double> flySpeedModifierScrollStep;
	private final OptionInstance<Boolean> flySpeedModifierApplyToOtherMovement;
	private final OptionInstance<Boolean> flySpeedModifierEnabled;
	private final OptionInstance<Boolean> hideRealmsEnabled;
	private final OptionInstance<Boolean> hidePasswordEnabled;
	private final OptionInstance<Boolean> hidePasswordHideLength;
	private final OptionInstance<Boolean> interactionDelayMiningDelay;
	private final OptionInstance<Boolean> interactionDelayPlacingDelay;
	private final OptionInstance<Boolean> keepDirectionInMinecart;
	private final OptionInstance<Boolean> noCollisionEnabled;
	private final OptionInstance<Boolean> noFlightResetEnabled;
	private final OptionInstance<Boolean> noFogEnabled;
	private final OptionInstance<Boolean> noJumpDelayEnabled;
	private final OptionInstance<Boolean> noTextureRotationEnabled;
	private final OptionInstance<Boolean> noTextureRotationDisableRandomOffset;
	private final OptionInstance<Boolean> noTextureRotationKeepCollisionShapeOffsets;
	private final OptionInstance<Boolean> playerHighlighterEnabled;
	private final OptionInstance<Boolean> playerHighlighterKeep;
	private final OptionInstance<Boolean> playerHighlighterInformationHud;
	private final OptionInstance<Boolean> transparentBlocksAlwaysRender;

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

	private EditBox betterStepStepHeightInput;
	private EditBox doubleJumpJumpCountInput;
	private EditBox doubleJumpCoyoteTimeInput;
	private EditBox doubleJumpCooldownTicksInput;
	private boolean synchronizingDoubleJumpCountInput;
	private boolean synchronizingDoubleJumpCountSlider;
	private boolean synchronizingDoubleJumpCoyoteTimeInput;
	private boolean synchronizingDoubleJumpCoyoteTimeSlider;
	private boolean synchronizingBetterStepStepHeightInput;
	private boolean synchronizingBetterStepStepHeightSlider;
	private String lastValidBetterStepStepHeightInput;
	private String lastValidDoubleJumpCountInput;
	private String lastValidDoubleJumpCoyoteTimeInput;
	private String lastValidDoubleJumpCooldownInput;

	private WenhanClientConfigScreen(Screen parent) {
		super(
				parent,
				Minecraft.getInstance().options,
				Component.translatable("title.wenhan-client.config"));
		this.boatUtilsDraft = BoatUtilsConfig.get();
		this.attackThroughFoliageDraft = AttackThroughFoliageSubMod.config();
		this.betterStepDraft = BetterStepSubMod.config();
		this.noSlowdownDraft = NoSlowdownSubMod.config();
		this.noSlimeBounceDraft = NoSlimeBounceSubMod.config();
		this.creativeFlyingDraft = CreativeFlyingSubMod.config();
		this.doubleJumpDraft = DoubleJumpConfig.get();
		this.elytraCancelDraft = ElytraCancelSubMod.config();
		this.flightDisablerDraft = FlightDisablerSubMod.config();
		this.flySpeedModifierDraft = FlySpeedModifierConfig.get();
		this.hideRealmsDraft = HideRealmsSubMod.config();
		this.hidePasswordDraft = HidePasswordSubMod.config();
		this.interactionDelayDraft = InteractionDelaySubMod.config();
		this.minecartDirectionDraft = MinecartDirectionSubMod.config();
		this.noCollisionDraft = NoCollisionSubMod.config();
		this.noFlightResetDraft = NoFlightResetSubMod.config();
		this.noFogDraft = NoFogSubMod.config();
		this.noJumpDelayDraft = NoJumpDelaySubMod.config();
		this.noTextureRotationDraft = NoTextureRotationSubMod.config();
		this.playerHighlighterDraft = PlayerHighlighterSubMod.config();
		this.transparentBlocksDraft = TransparentBlocksSubMod.config();

		this.attackThroughFoliageEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.attack-through-foliage.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.attack-through-foliage.enabled.tooltip")),
						this.attackThroughFoliageDraft.enabled,
						value -> this.attackThroughFoliageDraft.enabled = value);
		this.betterStepAirStepUpEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.better-step.air_step_up_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.better-step.air_step_up_enabled.tooltip")),
						this.betterStepDraft.airStepUpEnabled,
						value -> this.betterStepDraft.airStepUpEnabled = value);
		this.betterStepStepDownEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.better-step.step_down_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.better-step.step_down_enabled.tooltip")),
						this.betterStepDraft.stepDownEnabled,
						value -> this.betterStepDraft.stepDownEnabled = value);
		this.betterStepStepHeight =
				new OptionInstance<>(
						"option.wenhan-client.better-step.step_height",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.better-step.step_height.tooltip")),
						(optionText, value) ->
								Component.translatable(
										"option.wenhan-client.better-step.step_height.value",
										String.format(Locale.ROOT, "%.1f", value / 10.0D)),
						new OptionInstance.IntRange(
								BetterStepConfig.STEP_HEIGHT_SLIDER_MIN,
								BetterStepConfig.STEP_HEIGHT_SLIDER_MAX),
						BetterStepConfig.stepHeightToSlider(this.betterStepDraft.stepHeight),
						this::onBetterStepStepHeightSliderChanged);
		this.noSlowdownEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-slowdown.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-slowdown.enabled.tooltip")),
						this.noSlowdownDraft.enabled,
						value -> this.noSlowdownDraft.enabled = value);
		this.noSlimeBounceEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-slime-bounce.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-slime-bounce.enabled.tooltip")),
						this.noSlimeBounceDraft.enabled,
						value -> this.noSlimeBounceDraft.enabled = value);
		this.noFogEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-fog.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-fog.enabled.tooltip")),
						this.noFogDraft.enabled,
						value -> this.noFogDraft.enabled = value);
		this.creativeFlyingEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.creative-flying.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.creative-flying.enabled.tooltip")),
						this.creativeFlyingDraft.enabled,
						value -> this.creativeFlyingDraft.enabled = value);
		this.doubleJumpEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.double-jump.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.double-jump.enabled.tooltip")),
						this.doubleJumpDraft.enabled,
						value -> this.doubleJumpDraft.enabled = value);
		this.doubleJumpInfiniteJumps =
				OptionInstance.createBoolean(
						"option.wenhan-client.double-jump.infinite_jumps",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.double-jump.infinite_jumps.tooltip")),
						this.doubleJumpDraft.infiniteJumps,
						value -> this.doubleJumpDraft.infiniteJumps = value);
		this.doubleJumpJumpCount =
				new OptionInstance<>(
						"option.wenhan-client.double-jump.jump_count",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.double-jump.jump_count.tooltip")),
						(optionText, value) ->
								Component.literal(optionText.getString() + ": " + value),
						UnitDouble.INSTANCE.xmap(
								WenhanClientConfigScreen::toDoubleJumpCount,
								WenhanClientConfigScreen::fromDoubleJumpCount),
						Codec.intRange(
								DoubleJumpConfig.MIN_JUMP_COUNT,
								DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT),
						clampDoubleJumpCountToSlider(this.doubleJumpDraft.jumpCount),
						this::onDoubleJumpCountSliderChanged);
		this.doubleJumpCoyoteTime =
				new OptionInstance<>(
						"option.wenhan-client.double-jump.coyote_time",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.double-jump.coyote_time.tooltip")),
						(optionText, value) ->
								Component.literal(optionText.getString() + ": " + value),
						UnitDouble.INSTANCE.xmap(
								WenhanClientConfigScreen::toDoubleJumpCoyoteTime,
								WenhanClientConfigScreen::fromDoubleJumpCoyoteTime),
						Codec.intRange(
								DoubleJumpConfig.MIN_COYOTE_TIME_TICKS,
								DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS),
						clampDoubleJumpCoyoteTimeToSlider(this.doubleJumpDraft.coyoteTimeTicks),
						this::onDoubleJumpCoyoteTimeSliderChanged);
		this.doubleJumpCooldownEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.double-jump.cooldown_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.double-jump.cooldown_enabled.tooltip")),
						this.doubleJumpDraft.cooldownEnabled,
						value -> this.doubleJumpDraft.cooldownEnabled = value);
		this.elytraCancelEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.elytra-cancel.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.elytra-cancel.enabled.tooltip")),
						this.elytraCancelDraft.enabled,
						value -> this.elytraCancelDraft.enabled = value);
		this.flightDisablerEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.flight-disabler.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.flight-disabler.enabled.tooltip")),
						this.flightDisablerDraft.enabled,
						value -> this.flightDisablerDraft.enabled = value);
		this.flySpeedModifierEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.fly-speed-modifier.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.enabled.tooltip")),
						this.flySpeedModifierDraft.enabled,
						value -> this.flySpeedModifierDraft.enabled = value);
		this.flySpeedModifierFullRange =
				OptionInstance.createBoolean(
						"option.wenhan-client.fly-speed-modifier.full_range",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.full_range.tooltip")),
						this.flySpeedModifierDraft.fullRange,
						this::onFlySpeedModifierFullRangeChanged);
		this.flySpeedModifierMaxSpeed =
				new OptionInstance<>(
						"option.wenhan-client.fly-speed-modifier.max_speed",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.max_speed.tooltip")),
						this::flySpeedModifierSpeedText,
						UnitDouble.INSTANCE.xmap(
								this::toFlySpeedModifierMaxSpeed,
								this::fromFlySpeedModifierMaxSpeed),
						Codec.doubleRange(
								FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED,
								FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED),
						this.flySpeedModifierDraft.maxSpeed,
						this::onFlySpeedModifierMaxSpeedChanged);
		this.flySpeedModifierMinSpeed =
				new OptionInstance<>(
						"option.wenhan-client.fly-speed-modifier.min_speed",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.min_speed.tooltip")),
						this::flySpeedModifierSpeedText,
						UnitDouble.INSTANCE.xmap(
								this::toFlySpeedModifierMinSpeed,
								this::fromFlySpeedModifierMinSpeed),
						Codec.doubleRange(
								FlySpeedModifierConfig.MIN_ALLOWED_MIN_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED),
						this.flySpeedModifierDraft.minSpeed,
						this::onFlySpeedModifierMinSpeedChanged);
		this.flySpeedModifierInitialSpeed =
				new OptionInstance<>(
						"option.wenhan-client.fly-speed-modifier.initial_speed",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.initial_speed.tooltip")),
						this::flySpeedModifierSpeedText,
						UnitDouble.INSTANCE.xmap(
								this::toFlySpeedModifierInitialSpeed,
								this::fromFlySpeedModifierInitialSpeed),
						Codec.doubleRange(
								FlySpeedModifierConfig.MIN_ALLOWED_INITIAL_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_INITIAL_SPEED),
						this.flySpeedModifierDraft.initialSpeed,
						value ->
								this.flySpeedModifierDraft.initialSpeed =
										roundToTwoDecimals(value));
		this.flySpeedModifierResetOnAdjust =
				OptionInstance.createBoolean(
						"option.wenhan-client.fly-speed-modifier.reset_on_adjust",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.reset_on_adjust.tooltip")),
						this.flySpeedModifierDraft.resetOnAdjust,
						value -> this.flySpeedModifierDraft.resetOnAdjust = value);
		this.flySpeedModifierScrollStep =
				new OptionInstance<>(
						"option.wenhan-client.fly-speed-modifier.scroll_step",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.scroll_step.tooltip")),
						this::flySpeedModifierScrollStepText,
						UnitDouble.INSTANCE.xmap(
								this::toFlySpeedModifierScrollStep,
								this::fromFlySpeedModifierScrollStep),
						Codec.doubleRange(
								FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP,
								FlySpeedModifierConfig.MAX_ALLOWED_SCROLL_STEP),
						this.flySpeedModifierDraft.scrollStep,
						value -> this.flySpeedModifierDraft.scrollStep = roundToOneDecimal(value));
		this.flySpeedModifierApplyToOtherMovement =
				OptionInstance.createBoolean(
						"option.wenhan-client.fly-speed-modifier.apply_to_other_movement",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.fly-speed-modifier.apply_to_other_movement.tooltip")),
						this.flySpeedModifierDraft.applyToOtherMovement,
						value -> this.flySpeedModifierDraft.applyToOtherMovement = value);
		this.hideRealmsEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.hide-realms.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.hide-realms.enabled.tooltip")),
						this.hideRealmsDraft.enabled,
						value -> this.hideRealmsDraft.enabled = value);
		this.hidePasswordEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.hide-password.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.hide-password.enabled.tooltip")),
						this.hidePasswordDraft.enabled,
						value -> this.hidePasswordDraft.enabled = value);
		this.hidePasswordHideLength =
				OptionInstance.createBoolean(
						"option.wenhan-client.hide-password.hide_length",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.hide-password.hide_length.tooltip")),
						this.hidePasswordDraft.hideLength,
						value -> this.hidePasswordDraft.hideLength = value);
		this.interactionDelayMiningDelay =
				OptionInstance.createBoolean(
						"option.wenhan-client.interaction-delay.mining_delay",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.interaction-delay.mining_delay.tooltip")),
						this.interactionDelayDraft.miningDelay,
						value -> this.interactionDelayDraft.miningDelay = value);
		this.interactionDelayPlacingDelay =
				OptionInstance.createBoolean(
						"option.wenhan-client.interaction-delay.placing_delay",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.interaction-delay.placing_delay.tooltip")),
						this.interactionDelayDraft.placingDelay,
						value -> this.interactionDelayDraft.placingDelay = value);
		this.noCollisionEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-collision.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-collision.enabled.tooltip")),
						this.noCollisionDraft.enabled,
						value -> this.noCollisionDraft.enabled = value);
		this.noFlightResetEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-flight-reset.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-flight-reset.enabled.tooltip")),
						this.noFlightResetDraft.enabled,
						value -> this.noFlightResetDraft.enabled = value);
		this.noJumpDelayEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-jump-delay.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-jump-delay.enabled.tooltip")),
						this.noJumpDelayDraft.enabled,
						value -> this.noJumpDelayDraft.enabled = value);
		this.noTextureRotationEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-texture-rotation.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-texture-rotation.enabled.tooltip")),
						this.noTextureRotationDraft.enabled,
						value -> this.noTextureRotationDraft.enabled = value);
		this.noTextureRotationDisableRandomOffset =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-texture-rotation.disable_random_offset",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-texture-rotation.disable_random_offset.tooltip")),
						this.noTextureRotationDraft.disableRandomOffset,
						value -> this.noTextureRotationDraft.disableRandomOffset = value);
		this.noTextureRotationKeepCollisionShapeOffsets =
				OptionInstance.createBoolean(
						"option.wenhan-client.no-texture-rotation.keep_collision_shape_offsets",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.no-texture-rotation.keep_collision_shape_offsets.tooltip")),
						this.noTextureRotationDraft.keepCollisionShapeOffsets,
						value -> this.noTextureRotationDraft.keepCollisionShapeOffsets = value);
		this.playerHighlighterEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.player-highlighter.enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.player-highlighter.enabled.tooltip")),
						this.playerHighlighterDraft.enabled,
						value -> this.playerHighlighterDraft.enabled = value);
		this.playerHighlighterKeep =
				OptionInstance.createBoolean(
						"option.wenhan-client.player-highlighter.keep",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.player-highlighter.keep.tooltip")),
						this.playerHighlighterDraft.keep,
						value -> this.playerHighlighterDraft.keep = value);
		this.playerHighlighterInformationHud =
				OptionInstance.createBoolean(
						"option.wenhan-client.player-highlighter.information_hud",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.player-highlighter.information_hud.tooltip")),
						this.playerHighlighterDraft.informationHud == null
								|| this.playerHighlighterDraft.informationHud,
						value -> this.playerHighlighterDraft.informationHud = value);
		this.transparentBlocksAlwaysRender =
				OptionInstance.createBoolean(
						"option.wenhan-client.transparent-blocks.always_render",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.transparent-blocks.always_render.tooltip")),
						this.transparentBlocksDraft.alwaysRender,
						value -> this.transparentBlocksDraft.alwaysRender = value);
		this.keepDirectionInMinecart =
				OptionInstance.createBoolean(
						"option.wenhan-client.minecart-direction.keep_direction_in_minecart",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.minecart-direction.keep_direction_in_minecart.tooltip")),
						this.minecartDirectionDraft.keepDirectionInMinecart,
						value -> this.minecartDirectionDraft.keepDirectionInMinecart = value);

		this.unrestrictedViewRotation =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.unrestricted_view_rotation",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.unrestricted_view_rotation.tooltip")),
						this.boatUtilsDraft.unrestrictedViewRotation,
						value -> this.boatUtilsDraft.unrestrictedViewRotation = value);
		this.viewDirectionLockEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.view_direction_lock_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.view_direction_lock_enabled.tooltip")),
						this.boatUtilsDraft.viewDirectionLockEnabled,
						value -> this.boatUtilsDraft.viewDirectionLockEnabled = value);
		this.directionHotkeysEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.direction_hotkeys_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.direction_hotkeys_enabled.tooltip")),
						this.boatUtilsDraft.directionHotkeysEnabled,
						value -> this.boatUtilsDraft.directionHotkeysEnabled = value);
		this.directionHotkeysUse45DegreeAngles =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.direction_hotkeys_use_45_degree_angles",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.direction_hotkeys_use_45_degree_angles.tooltip")),
						this.boatUtilsDraft.directionHotkeysUse45DegreeAngles,
						value -> this.boatUtilsDraft.directionHotkeysUse45DegreeAngles = value);
		this.blueIceSpeedEverywhere =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.blue_ice_speed_everywhere",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.blue_ice_speed_everywhere.tooltip")),
						this.boatUtilsDraft.blueIceSpeedEverywhere,
						value -> this.boatUtilsDraft.blueIceSpeedEverywhere = value);
		this.preventSinking =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.prevent_sinking",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.prevent_sinking.tooltip")),
						this.boatUtilsDraft.preventSinking,
						value -> this.boatUtilsDraft.preventSinking = value);
		this.handbrakeEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.handbrake_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.handbrake_enabled.tooltip")),
						this.boatUtilsDraft.handbrakeEnabled,
						value -> this.boatUtilsDraft.handbrakeEnabled = value);
		this.handbrakeBoostEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.handbrake_boost_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.handbrake_boost_enabled.tooltip")),
						this.boatUtilsDraft.handbrakeBoostEnabled,
						value -> this.boatUtilsDraft.handbrakeBoostEnabled = value);
		this.lateralFrictionEnabled =
				OptionInstance.createBoolean(
						"option.wenhan-client.boat-utils.lateral_friction_enabled",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.lateral_friction_enabled.tooltip")),
						this.boatUtilsDraft.lateralFrictionEnabled,
						value -> this.boatUtilsDraft.lateralFrictionEnabled = value);
		this.boatStepHeight =
				new OptionInstance<>(
						"option.wenhan-client.boat-utils.boat_step_height",
						value ->
								Tooltip.create(
										Component.translatable(
												"option.wenhan-client.boat-utils.boat_step_height.tooltip")),
						(optionText, value) ->
								Component.translatable(
										"option.wenhan-client.boat-utils.boat_step_height.value",
										String.format(Locale.ROOT, "%.1f", value / 10.0D)),
						new OptionInstance.IntRange(0, 100),
						Math.round(this.boatUtilsDraft.boatStepHeight * 10.0F),
						value -> this.boatUtilsDraft.boatStepHeight = value / 10.0F);
	}

	public static Screen create(Screen parent) {
		return new WenhanClientConfigScreen(parent);
	}

	@Override
	protected void addOptions() {
		this.list.addHeader(Component.translatable("title.wenhan-client.general.config"));
		this.list.addSmall(
				this.creativeFlyingEnabled,
				this.flightDisablerEnabled,
				this.elytraCancelEnabled,
				this.noFlightResetEnabled,
				this.noJumpDelayEnabled,
				this.noCollisionEnabled,
				this.attackThroughFoliageEnabled,
				this.noSlowdownEnabled,
				this.noSlimeBounceEnabled,
				this.noFogEnabled,
				this.transparentBlocksAlwaysRender,
				this.keepDirectionInMinecart,
				this.hideRealmsEnabled,
				this.interactionDelayMiningDelay,
				this.interactionDelayPlacingDelay);
		this.list.addHeader(Component.translatable("title.wenhan-client.better-step.config"));
		this.addBetterStepOptions();
		this.list.addHeader(Component.translatable("title.wenhan-client.hide-password.config"));
		this.list.addSmall(this.hidePasswordEnabled, this.hidePasswordHideLength);
		this.list.addHeader(
				Component.translatable("title.wenhan-client.player-highlighter.config"));
		this.list.addSmall(
				this.playerHighlighterEnabled,
				this.playerHighlighterKeep,
				this.playerHighlighterInformationHud);
		this.list.addHeader(
				Component.translatable("title.wenhan-client.no-texture-rotation.config"));
		this.list.addSmall(
				this.noTextureRotationEnabled,
				this.noTextureRotationDisableRandomOffset,
				this.noTextureRotationKeepCollisionShapeOffsets);
		this.list.addHeader(Component.translatable("title.wenhan-client.double-jump.config"));
		this.addDoubleJumpOptions();
		this.list.addHeader(
				Component.translatable("title.wenhan-client.fly-speed-modifier.config"));
		this.addFlySpeedModifierOptions();
		this.list.addHeader(Component.translatable("title.wenhan-client.boat-utils.config"));
		this.list.addSmall(
				this.unrestrictedViewRotation,
				this.viewDirectionLockEnabled,
				this.directionHotkeysEnabled,
				this.directionHotkeysUse45DegreeAngles,
				this.blueIceSpeedEverywhere,
				this.preventSinking,
				this.handbrakeEnabled,
				this.handbrakeBoostEnabled,
				this.lateralFrictionEnabled,
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
		this.applyInputValue(
				this.doubleJumpJumpCountInput,
				DoubleJumpConfig.MIN_JUMP_COUNT,
				value -> this.doubleJumpDraft.jumpCount = value);
		this.applyInputValue(
				this.doubleJumpCoyoteTimeInput,
				DoubleJumpConfig.MIN_COYOTE_TIME_TICKS,
				value -> this.doubleJumpDraft.coyoteTimeTicks = value);
		this.applyInputValue(
				this.doubleJumpCooldownTicksInput,
				DoubleJumpConfig.MIN_COOLDOWN_TICKS,
				value -> this.doubleJumpDraft.cooldownTicks = value);
		AttackThroughFoliageSubMod.setConfig(this.attackThroughFoliageDraft);
		this.applyBetterStepStepHeightInput();
		BetterStepSubMod.setConfig(this.betterStepDraft);
		NoSlowdownSubMod.setConfig(this.noSlowdownDraft);
		NoSlimeBounceSubMod.setConfig(this.noSlimeBounceDraft);
		CreativeFlyingSubMod.setConfig(this.creativeFlyingDraft);
		DoubleJumpConfig.set(this.doubleJumpDraft);
		ElytraCancelSubMod.setConfig(this.elytraCancelDraft);
		FlightDisablerSubMod.setConfig(this.flightDisablerDraft);
		FlySpeedModifierConfig.set(this.flySpeedModifierDraft);
		HideRealmsSubMod.setConfig(this.hideRealmsDraft);
		HidePasswordSubMod.setConfig(this.hidePasswordDraft);
		InteractionDelaySubMod.setConfig(this.interactionDelayDraft);
		MinecartDirectionSubMod.setConfig(this.minecartDirectionDraft);
		NoCollisionSubMod.setConfig(this.noCollisionDraft);
		NoFlightResetSubMod.setConfig(this.noFlightResetDraft);
		NoFogSubMod.setConfig(this.noFogDraft);
		NoJumpDelaySubMod.setConfig(this.noJumpDelayDraft);
		NoTextureRotationSubMod.setConfig(this.noTextureRotationDraft);
		PlayerHighlighterSubMod.setConfig(this.playerHighlighterDraft);
		TransparentBlocksSubMod.setConfig(this.transparentBlocksDraft);
		BoatUtilsConfig.set(this.boatUtilsDraft);
	}

	private void addFlySpeedModifierOptions() {
		this.list.addSmall(
				this.flySpeedModifierEnabled,
				this.flySpeedModifierFullRange,
				this.flySpeedModifierMaxSpeed,
				this.flySpeedModifierMinSpeed,
				this.flySpeedModifierInitialSpeed,
				this.flySpeedModifierResetOnAdjust,
				this.flySpeedModifierScrollStep,
				this.flySpeedModifierApplyToOtherMovement);
		this.rescaleFlySpeedModifierDynamicSliders();
	}

	private void addBetterStepOptions() {
		this.list.addSmall(this.betterStepAirStepUpEnabled, this.betterStepStepDownEnabled);
		this.betterStepStepHeightInput =
				new EditBox(
						this.font,
						0,
						0,
						150,
						20,
						Component.translatable(
								"option.wenhan-client.better-step.step_height_input"));
		this.betterStepStepHeightInput.setMaxLength(6);
		this.lastValidBetterStepStepHeightInput =
				formatBetterStepStepHeight(this.betterStepDraft.stepHeight);
		this.betterStepStepHeightInput.setValue(this.lastValidBetterStepStepHeightInput);
		this.betterStepStepHeightInput.setResponder(this::onBetterStepStepHeightInputChanged);
		AbstractWidget stepHeightWidget = this.betterStepStepHeight.createButton(this.options);
		this.list.addSmall(stepHeightWidget, this.betterStepStepHeightInput);
	}

	private void onBetterStepStepHeightSliderChanged(Integer value) {
		if (this.synchronizingBetterStepStepHeightSlider) {
			return;
		}
		this.betterStepDraft.stepHeight = BetterStepConfig.sliderToStepHeight(value);
		this.syncBetterStepStepHeightInputFromSlider(this.betterStepDraft.stepHeight);
	}

	private void onBetterStepStepHeightInputChanged(String value) {
		if (this.synchronizingBetterStepStepHeightInput) {
			return;
		}
		if (value.isEmpty()) {
			return;
		}

		Double parsedValue = this.parseBetterStepStepHeight(value);
		if (parsedValue == null) {
			this.restoreLastValidBetterStepStepHeightInput();
			return;
		}

		double sanitized = BetterStepConfig.roundToOneDecimal(parsedValue);
		this.betterStepDraft.stepHeight = sanitized;
		this.lastValidBetterStepStepHeightInput = formatBetterStepStepHeight(sanitized);
		this.setBetterStepStepHeightSliderValueSilently(sanitized);
	}

	private void setBetterStepStepHeightSliderValueSilently(double value) {
		this.synchronizingBetterStepStepHeightSlider = true;
		this.betterStepStepHeight.set(BetterStepConfig.stepHeightToSlider(value));
		this.synchronizingBetterStepStepHeightSlider = false;
	}

	private void syncBetterStepStepHeightInputFromSlider(double value) {
		if (this.betterStepStepHeightInput == null) {
			return;
		}

		this.synchronizingBetterStepStepHeightInput = true;
		this.lastValidBetterStepStepHeightInput = formatBetterStepStepHeight(value);
		this.betterStepStepHeightInput.setValue(this.lastValidBetterStepStepHeightInput);
		this.synchronizingBetterStepStepHeightInput = false;
	}

	private void restoreLastValidBetterStepStepHeightInput() {
		if (this.betterStepStepHeightInput == null) {
			return;
		}

		this.synchronizingBetterStepStepHeightInput = true;
		this.betterStepStepHeightInput.setValue(this.lastValidBetterStepStepHeightInput);
		this.synchronizingBetterStepStepHeightInput = false;
	}

	private void applyBetterStepStepHeightInput() {
		if (this.betterStepStepHeightInput == null
				|| this.betterStepStepHeightInput.getValue().isEmpty()) {
			return;
		}

		Double parsedValue =
				this.parseBetterStepStepHeight(this.betterStepStepHeightInput.getValue());
		if (parsedValue != null) {
			this.betterStepDraft.stepHeight = BetterStepConfig.roundToOneDecimal(parsedValue);
		}
	}

	private Double parseBetterStepStepHeight(String value) {
		try {
			double parsedValue = Double.parseDouble(value);
			if (!Double.isFinite(parsedValue)
					|| parsedValue < BetterStepConfig.MIN_STEP_HEIGHT
					|| parsedValue > BetterStepConfig.MAX_STEP_HEIGHT) {
				return null;
			}
			return parsedValue;
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private static String formatBetterStepStepHeight(double value) {
		return String.format(Locale.ROOT, "%.1f", BetterStepConfig.roundToOneDecimal(value));
	}

	private void onFlySpeedModifierFullRangeChanged(Boolean value) {
		this.flySpeedModifierDraft.fullRange = value;
		if (!value) {
			if (this.flySpeedModifierDraft.maxSpeed > FlySpeedModifierConfig.STANDARD_MAX_SPEED) {
				this.flySpeedModifierDraft.maxSpeed = FlySpeedModifierConfig.DEFAULT_MAX_SPEED;
				this.flySpeedModifierMaxSpeed.set(this.flySpeedModifierDraft.maxSpeed);
			}
			if (this.flySpeedModifierDraft.initialSpeed
					> FlySpeedModifierConfig.STANDARD_MAX_SPEED) {
				this.flySpeedModifierDraft.initialSpeed =
						FlySpeedModifierConfig.DEFAULT_INITIAL_SPEED;
				this.flySpeedModifierInitialSpeed.set(this.flySpeedModifierDraft.initialSpeed);
			}
		}
		this.constrainFlySpeedModifierInitialSpeed();
		this.rescaleFlySpeedModifierDynamicSliders();
	}

	private void onFlySpeedModifierMaxSpeedChanged(Double value) {
		this.flySpeedModifierDraft.maxSpeed = roundToTwoDecimals(value);
		this.constrainFlySpeedModifierInitialSpeed();
		this.rescaleFlySpeedModifierDynamicSliders();
	}

	private void onFlySpeedModifierMinSpeedChanged(Double value) {
		this.flySpeedModifierDraft.minSpeed = roundToTwoDecimals(value);
		this.constrainFlySpeedModifierInitialSpeed();
		this.rescaleFlySpeedModifierDynamicSliders();
	}

	private double toFlySpeedModifierMaxSpeed(double normalized) {
		double upperBound =
				this.flySpeedModifierDraft.fullRange
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		double result =
				FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED
						+ normalized * (upperBound - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED);
		return roundToTwoDecimals(result);
	}

	private double fromFlySpeedModifierMaxSpeed(double speed) {
		double upperBound =
				this.flySpeedModifierDraft.fullRange
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		double clamped =
				Math.max(FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED, Math.min(speed, upperBound));
		return (clamped - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED)
				/ (upperBound - FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED);
	}

	private double toFlySpeedModifierMinSpeed(double normalized) {
		return roundToTwoDecimals(normalized * FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED);
	}

	private double fromFlySpeedModifierMinSpeed(double speed) {
		return speed / FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED;
	}

	private double toFlySpeedModifierInitialSpeed(double normalized) {
		double lowerBound = this.flySpeedModifierDraft.minSpeed;
		double upperBound = this.effectiveFlySpeedModifierMaximumSpeed();
		if (upperBound <= lowerBound) {
			return roundToTwoDecimals(lowerBound);
		}
		return roundToTwoDecimals(lowerBound + normalized * (upperBound - lowerBound));
	}

	private double fromFlySpeedModifierInitialSpeed(double speed) {
		double lowerBound = this.flySpeedModifierDraft.minSpeed;
		double upperBound = this.effectiveFlySpeedModifierMaximumSpeed();
		if (upperBound <= lowerBound) {
			return 0.0D;
		}
		double clamped = Math.max(lowerBound, Math.min(speed, upperBound));
		return (clamped - lowerBound) / (upperBound - lowerBound);
	}

	private double toFlySpeedModifierScrollStep(double normalized) {
		double range =
				FlySpeedModifierConfig.MAX_ALLOWED_SCROLL_STEP
						- FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP;
		return roundToOneDecimal(
				FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP + normalized * range);
	}

	private double fromFlySpeedModifierScrollStep(double step) {
		double range =
				FlySpeedModifierConfig.MAX_ALLOWED_SCROLL_STEP
						- FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP;
		return (step - FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP) / range;
	}

	private void constrainFlySpeedModifierInitialSpeed() {
		double lowerBound = this.flySpeedModifierDraft.minSpeed;
		double upperBound = this.effectiveFlySpeedModifierMaximumSpeed();
		double constrained =
				Math.max(lowerBound, Math.min(this.flySpeedModifierDraft.initialSpeed, upperBound));
		if (Double.compare(constrained, this.flySpeedModifierDraft.initialSpeed) != 0) {
			this.flySpeedModifierDraft.initialSpeed = roundToTwoDecimals(constrained);
			this.flySpeedModifierInitialSpeed.set(this.flySpeedModifierDraft.initialSpeed);
		}
	}

	private double effectiveFlySpeedModifierMaximumSpeed() {
		double fullRangeUpperBound =
				this.flySpeedModifierDraft.fullRange
						? FlySpeedModifierConfig.FULL_RANGE_MAX_SPEED
						: FlySpeedModifierConfig.STANDARD_MAX_SPEED;
		return Math.max(
				this.flySpeedModifierDraft.minSpeed,
				Math.min(this.flySpeedModifierDraft.maxSpeed, fullRangeUpperBound));
	}

	private void rescaleFlySpeedModifierDynamicSliders() {
		this.resetSliderWidget(
				this.flySpeedModifierMaxSpeed,
				this.fromFlySpeedModifierMaxSpeed(this.flySpeedModifierDraft.maxSpeed));
		this.resetSliderWidget(
				this.flySpeedModifierInitialSpeed,
				this.fromFlySpeedModifierInitialSpeed(this.flySpeedModifierDraft.initialSpeed));
	}

	private void resetSliderWidget(OptionInstance<Double> option, double normalizedValue) {
		var widget = this.list.findOption(option);
		if (widget instanceof AbstractSliderButton slider) {
			((AbstractSliderButtonAccessor) slider)
					.flySpeedModifier$invokeSetValue(normalizedValue);
		}
	}

	private Component flySpeedModifierSpeedText(Component optionText, Double value) {
		return Component.literal(
				optionText.getString() + ": " + FlySpeedModifierConfig.formatSpeed(value));
	}

	private Component flySpeedModifierScrollStepText(Component optionText, Double value) {
		return Component.literal(
				optionText.getString() + ": " + FlySpeedModifierConfig.formatScrollStep(value));
	}

	private static double roundToOneDecimal(double value) {
		return Math.round(value * 10.0D) / 10.0D;
	}

	private static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0D) / 100.0D;
	}

	private void addDoubleJumpOptions() {
		AbstractWidget enabledWidget = this.doubleJumpEnabled.createButton(this.options);
		AbstractWidget infiniteJumpsWidget =
				this.doubleJumpInfiniteJumps.createButton(this.options);
		this.list.addSmall(enabledWidget, infiniteJumpsWidget);

		this.doubleJumpJumpCountInput =
				new EditBox(
						this.font,
						0,
						0,
						150,
						20,
						Component.translatable(
								"option.wenhan-client.double-jump.jump_count_input"));
		this.doubleJumpJumpCountInput.setMaxLength(10);
		this.lastValidDoubleJumpCountInput = Integer.toString(this.doubleJumpDraft.jumpCount);
		this.doubleJumpJumpCountInput.setValue(this.lastValidDoubleJumpCountInput);
		this.doubleJumpJumpCountInput.setResponder(this::onDoubleJumpCountInputChanged);
		AbstractWidget jumpCountWidget = this.doubleJumpJumpCount.createButton(this.options);
		this.list.addSmall(jumpCountWidget, this.doubleJumpJumpCountInput);

		this.doubleJumpCoyoteTimeInput =
				new EditBox(
						this.font,
						0,
						0,
						150,
						20,
						Component.translatable(
								"option.wenhan-client.double-jump.coyote_time_input"));
		this.doubleJumpCoyoteTimeInput.setMaxLength(10);
		this.lastValidDoubleJumpCoyoteTimeInput =
				Integer.toString(this.doubleJumpDraft.coyoteTimeTicks);
		this.doubleJumpCoyoteTimeInput.setValue(this.lastValidDoubleJumpCoyoteTimeInput);
		this.doubleJumpCoyoteTimeInput.setResponder(this::onDoubleJumpCoyoteTimeInputChanged);
		AbstractWidget coyoteTimeWidget = this.doubleJumpCoyoteTime.createButton(this.options);
		this.list.addSmall(coyoteTimeWidget, this.doubleJumpCoyoteTimeInput);

		AbstractWidget cooldownEnabledWidget =
				this.doubleJumpCooldownEnabled.createButton(this.options);
		this.doubleJumpCooldownTicksInput =
				new EditBox(
						this.font,
						0,
						0,
						150,
						20,
						Component.translatable("option.wenhan-client.double-jump.cooldown_ticks"));
		this.doubleJumpCooldownTicksInput.setMaxLength(10);
		this.lastValidDoubleJumpCooldownInput =
				Integer.toString(this.doubleJumpDraft.cooldownTicks);
		this.doubleJumpCooldownTicksInput.setValue(this.lastValidDoubleJumpCooldownInput);
		this.doubleJumpCooldownTicksInput.setResponder(this::onDoubleJumpCooldownInputChanged);
		this.list.addSmall(cooldownEnabledWidget, this.doubleJumpCooldownTicksInput);
	}

	private void onDoubleJumpCountSliderChanged(Integer value) {
		if (this.synchronizingDoubleJumpCountSlider) {
			return;
		}
		this.doubleJumpDraft.jumpCount = value;
		this.syncDoubleJumpCountInputFromSlider(value);
	}

	private void onDoubleJumpCoyoteTimeSliderChanged(Integer value) {
		if (this.synchronizingDoubleJumpCoyoteTimeSlider) {
			return;
		}
		this.doubleJumpDraft.coyoteTimeTicks = value;
		this.syncDoubleJumpCoyoteTimeInputFromSlider(value);
	}

	private void onDoubleJumpCountInputChanged(String value) {
		if (this.synchronizingDoubleJumpCountInput) {
			return;
		}

		Integer parsedValue =
				this.parseNonNegativeInteger(value, this::restoreLastValidDoubleJumpCountInput);
		if (parsedValue == null) {
			return;
		}

		this.lastValidDoubleJumpCountInput = value;
		if (parsedValue < DoubleJumpConfig.MIN_JUMP_COUNT) {
			return;
		}

		this.doubleJumpDraft.jumpCount = parsedValue;
		this.setDoubleJumpCountSliderValueSilently(parsedValue);
	}

	private void onDoubleJumpCoyoteTimeInputChanged(String value) {
		if (this.synchronizingDoubleJumpCoyoteTimeInput) {
			return;
		}

		Integer parsedValue =
				this.parseNonNegativeInteger(
						value, this::restoreLastValidDoubleJumpCoyoteTimeInput);
		if (parsedValue == null) {
			return;
		}

		this.lastValidDoubleJumpCoyoteTimeInput = value;
		this.doubleJumpDraft.coyoteTimeTicks = parsedValue;
		this.setDoubleJumpCoyoteTimeSliderValueSilently(parsedValue);
	}

	private void onDoubleJumpCooldownInputChanged(String value) {
		Integer parsedValue =
				this.parseNonNegativeInteger(value, this::restoreLastValidDoubleJumpCooldownInput);
		if (parsedValue == null) {
			return;
		}

		this.lastValidDoubleJumpCooldownInput = value;
		this.doubleJumpDraft.cooldownTicks = parsedValue;
	}

	private void setDoubleJumpCountSliderValueSilently(int value) {
		this.synchronizingDoubleJumpCountSlider = true;
		this.doubleJumpJumpCount.set(clampDoubleJumpCountToSlider(value));
		this.synchronizingDoubleJumpCountSlider = false;
	}

	private void setDoubleJumpCoyoteTimeSliderValueSilently(int value) {
		this.synchronizingDoubleJumpCoyoteTimeSlider = true;
		this.doubleJumpCoyoteTime.set(clampDoubleJumpCoyoteTimeToSlider(value));
		this.synchronizingDoubleJumpCoyoteTimeSlider = false;
	}

	private void syncDoubleJumpCountInputFromSlider(int value) {
		if (this.doubleJumpJumpCountInput == null) {
			return;
		}

		this.synchronizingDoubleJumpCountInput = true;
		this.lastValidDoubleJumpCountInput = Integer.toString(value);
		this.doubleJumpJumpCountInput.setValue(this.lastValidDoubleJumpCountInput);
		this.synchronizingDoubleJumpCountInput = false;
	}

	private void syncDoubleJumpCoyoteTimeInputFromSlider(int value) {
		if (this.doubleJumpCoyoteTimeInput == null) {
			return;
		}

		this.synchronizingDoubleJumpCoyoteTimeInput = true;
		this.lastValidDoubleJumpCoyoteTimeInput = Integer.toString(value);
		this.doubleJumpCoyoteTimeInput.setValue(this.lastValidDoubleJumpCoyoteTimeInput);
		this.synchronizingDoubleJumpCoyoteTimeInput = false;
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

	private void restoreLastValidDoubleJumpCountInput() {
		if (this.doubleJumpJumpCountInput == null) {
			return;
		}

		this.synchronizingDoubleJumpCountInput = true;
		this.doubleJumpJumpCountInput.setValue(this.lastValidDoubleJumpCountInput);
		this.synchronizingDoubleJumpCountInput = false;
	}

	private void restoreLastValidDoubleJumpCoyoteTimeInput() {
		if (this.doubleJumpCoyoteTimeInput == null) {
			return;
		}

		this.synchronizingDoubleJumpCoyoteTimeInput = true;
		this.doubleJumpCoyoteTimeInput.setValue(this.lastValidDoubleJumpCoyoteTimeInput);
		this.synchronizingDoubleJumpCoyoteTimeInput = false;
	}

	private void restoreLastValidDoubleJumpCooldownInput() {
		if (this.doubleJumpCooldownTicksInput != null) {
			this.doubleJumpCooldownTicksInput.setValue(this.lastValidDoubleJumpCooldownInput);
		}
	}

	private void applyInputValue(EditBox input, int minimum, IntConsumer setter) {
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

	private static int toDoubleJumpCount(double normalized) {
		double range = DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT - DoubleJumpConfig.MIN_JUMP_COUNT;
		return clampDoubleJumpCountToSlider(
				(int) Math.round(DoubleJumpConfig.MIN_JUMP_COUNT + normalized * range));
	}

	private static double fromDoubleJumpCount(int count) {
		double range = DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT - DoubleJumpConfig.MIN_JUMP_COUNT;
		return (clampDoubleJumpCountToSlider(count) - DoubleJumpConfig.MIN_JUMP_COUNT) / range;
	}

	private static int toDoubleJumpCoyoteTime(double normalized) {
		double range =
				DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS
						- DoubleJumpConfig.MIN_COYOTE_TIME_TICKS;
		return clampDoubleJumpCoyoteTimeToSlider(
				(int) Math.round(DoubleJumpConfig.MIN_COYOTE_TIME_TICKS + normalized * range));
	}

	private static double fromDoubleJumpCoyoteTime(int ticks) {
		double range =
				DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS
						- DoubleJumpConfig.MIN_COYOTE_TIME_TICKS;
		return (clampDoubleJumpCoyoteTimeToSlider(ticks) - DoubleJumpConfig.MIN_COYOTE_TIME_TICKS)
				/ range;
	}

	private static int clampDoubleJumpCountToSlider(int value) {
		if (value < DoubleJumpConfig.MIN_JUMP_COUNT) {
			return DoubleJumpConfig.MIN_JUMP_COUNT;
		}
		if (value > DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT) {
			return DoubleJumpConfig.SLIDER_MAX_JUMP_COUNT;
		}
		return value;
	}

	private static int clampDoubleJumpCoyoteTimeToSlider(int value) {
		if (value < DoubleJumpConfig.MIN_COYOTE_TIME_TICKS) {
			return DoubleJumpConfig.MIN_COYOTE_TIME_TICKS;
		}
		if (value > DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS) {
			return DoubleJumpConfig.SLIDER_MAX_COYOTE_TIME_TICKS;
		}
		return value;
	}
}

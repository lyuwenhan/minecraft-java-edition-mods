package com.example.wenhanclient.client.compat;

import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageConfig;
import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageSubMod;
import com.example.wenhanclient.autoaim.AutoAimConfig;
import com.example.wenhanclient.autoaim.AutoAimMode;
import com.example.wenhanclient.autoaim.AutoAimSubMod;
import com.example.wenhanclient.autoaim.AutoAttackMode;
import com.example.wenhanclient.autoaim.ViewRestoreMode;
import com.example.wenhanclient.betterstep.BetterStepConfig;
import com.example.wenhanclient.betterstep.BetterStepSubMod;
import com.example.wenhanclient.blockexplosionguard.BlockExplosionGuardConfig;
import com.example.wenhanclient.blockexplosionguard.BlockExplosionGuardSubMod;
import com.example.wenhanclient.boatutils.BoatUtilsConfig;
import com.example.wenhanclient.boatutils.BoatUtilsSubMod;
import com.example.wenhanclient.clientpseudounrestrict.ClientPseudoUnrestrictConfig;
import com.example.wenhanclient.clientpseudounrestrict.ClientPseudoUnrestrictSubMod;
import com.example.wenhanclient.coyote.CoyoteConfig;
import com.example.wenhanclient.coyote.CoyoteSubMod;
import com.example.wenhanclient.creativeflying.CreativeFlyingConfig;
import com.example.wenhanclient.creativeflying.CreativeFlyingSubMod;
import com.example.wenhanclient.doublejump.DoubleJumpConfig;
import com.example.wenhanclient.doublejump.DoubleJumpSubMod;
import com.example.wenhanclient.elytracancel.ElytraCancelConfig;
import com.example.wenhanclient.elytracancel.ElytraCancelSubMod;
import com.example.wenhanclient.flightdisabler.FlightDisablerConfig;
import com.example.wenhanclient.flightdisabler.FlightDisablerSubMod;
import com.example.wenhanclient.fluidplatform.FluidPlatformConfig;
import com.example.wenhanclient.fluidplatform.FluidPlatformSubMod;
import com.example.wenhanclient.flyspeedmodifier.FlySpeedModifierConfig;
import com.example.wenhanclient.flyspeedmodifier.FlySpeedModifierSubMod;
import com.example.wenhanclient.hidepassword.HidePasswordSubMod;
import com.example.wenhanclient.hidepassword.config.HidePasswordConfig;
import com.example.wenhanclient.hiderealms.HideRealmsConfig;
import com.example.wenhanclient.hiderealms.HideRealmsSubMod;
import com.example.wenhanclient.instantrespawn.InstantRespawnConfig;
import com.example.wenhanclient.instantrespawn.InstantRespawnSubMod;
import com.example.wenhanclient.interactiondelay.InteractionDelayConfig;
import com.example.wenhanclient.interactiondelay.InteractionDelaySubMod;
import com.example.wenhanclient.jumpheight.JumpHeightConfig;
import com.example.wenhanclient.jumpheight.JumpHeightSubMod;
import com.example.wenhanclient.minecartdirection.MinecartDirectionConfig;
import com.example.wenhanclient.minecartdirection.MinecartDirectionSubMod;
import com.example.wenhanclient.nocameracollision.NoCameraCollisionConfig;
import com.example.wenhanclient.nocameracollision.NoCameraCollisionSubMod;
import com.example.wenhanclient.nocollision.NoCollisionConfig;
import com.example.wenhanclient.nocollision.NoCollisionSubMod;
import com.example.wenhanclient.noflightreset.NoFlightResetConfig;
import com.example.wenhanclient.noflightreset.NoFlightResetSubMod;
import com.example.wenhanclient.nofog.NoFogConfig;
import com.example.wenhanclient.nofog.NoFogSubMod;
import com.example.wenhanclient.nojumpdelay.NoJumpDelayConfig;
import com.example.wenhanclient.nojumpdelay.NoJumpDelaySubMod;
import com.example.wenhanclient.nolevitation.NoLevitationConfig;
import com.example.wenhanclient.nolevitation.NoLevitationSubMod;
import com.example.wenhanclient.noslimebounce.NoSlimeBounceConfig;
import com.example.wenhanclient.noslimebounce.NoSlimeBounceSubMod;
import com.example.wenhanclient.noslowdown.NoSlowdownConfig;
import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;
import com.example.wenhanclient.notexturerotation.NoTextureRotationConfig;
import com.example.wenhanclient.notexturerotation.NoTextureRotationSubMod;
import com.example.wenhanclient.placementrestriction.PlacementRestrictionConfig;
import com.example.wenhanclient.placementrestriction.PlacementRestrictionMode;
import com.example.wenhanclient.placementrestriction.PlacementRestrictionSubMod;
import com.example.wenhanclient.playerhighlighter.PlayerHighlightMode;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterConfig;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterSubMod;
import com.example.wenhanclient.playerwaypointinfo.PlayerWaypointInfoConfig;
import com.example.wenhanclient.playerwaypointinfo.PlayerWaypointInfoSubMod;
import com.example.wenhanclient.stablemining.StableMiningConfig;
import com.example.wenhanclient.stablemining.StableMiningSubMod;
import com.example.wenhanclient.transparentblocks.TransparentBlocksConfig;
import com.example.wenhanclient.transparentblocks.TransparentBlocksSubMod;
import com.example.wenhanclient.whoiam.WhoIAmConfig;
import com.example.wenhanclient.whoiam.WhoIAmSubMod;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormatter;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class WenhanClientConfigScreen {
	private WenhanClientConfigScreen() {}

	public static Screen create(Screen parent) {
		Drafts drafts = Drafts.create();
		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("title.wenhan-client.config"))
				.category(generalCategory(drafts))
				.category(movementCategory(drafts))
				.category(hidePasswordCategory(drafts))
				.category(whoIAmCategory(drafts))
				.category(noTextureRotationCategory(drafts))
				.category(flySpeedModifierCategory(drafts))
				.category(boatUtilsCategory(drafts))
				.category(placementRestrictionCategory(drafts))
				.category(autoAimCategory(drafts))
				.category(clientPseudoUnrestrictCategory(drafts))
				.save(drafts::save)
				.build()
				.generateScreen(parent);
	}

	private static ConfigCategory generalCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.general.config"))
				.option(
						booleanOption(
								"option.wenhan-client.hide-realms.enabled",
								HideRealmsConfig.DEFAULT_ENABLED,
								() -> drafts.hideRealms.enabled,
								value -> drafts.hideRealms.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.instant-respawn.enabled",
								InstantRespawnConfig.DEFAULT_ENABLED,
								() -> drafts.instantRespawn.enabled,
								value -> drafts.instantRespawn.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.block-explosion-guard.enabled",
								BlockExplosionGuardConfig.DEFAULT_ENABLED,
								() -> drafts.blockExplosionGuard.enabled,
								value -> drafts.blockExplosionGuard.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-camera-collision.enabled",
								NoCameraCollisionConfig.DEFAULT_ENABLED,
								() -> drafts.noCameraCollision.enabled,
								value -> drafts.noCameraCollision.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.attack-through-foliage.enabled",
								AttackThroughFoliageConfig.DEFAULT_ENABLED,
								() -> drafts.attackThroughFoliage.enabled,
								value -> drafts.attackThroughFoliage.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-fog.enabled",
								NoFogConfig.DEFAULT_ENABLED,
								() -> drafts.noFog.enabled,
								value -> drafts.noFog.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.transparent-blocks.always_render",
								TransparentBlocksConfig.DEFAULT_ALWAYS_RENDER,
								() -> drafts.transparentBlocks.alwaysRender,
								value -> drafts.transparentBlocks.alwaysRender = value))
				.option(
						booleanOption(
								"option.wenhan-client.interaction-delay.mining_delay",
								InteractionDelayConfig.DEFAULT_MINING_DELAY,
								() -> drafts.interactionDelay.miningDelay,
								value -> drafts.interactionDelay.miningDelay = value))
				.option(
						booleanOption(
								"option.wenhan-client.interaction-delay.placing_delay",
								InteractionDelayConfig.DEFAULT_PLACING_DELAY,
								() -> drafts.interactionDelay.placingDelay,
								value -> drafts.interactionDelay.placingDelay = value))
				.option(
						booleanOption(
								"option.wenhan-client.stable-mining.enabled",
								StableMiningConfig.DEFAULT_ENABLED,
								() -> drafts.stableMining.enabled,
								value -> drafts.stableMining.enabled = value))
				.option(
						enumOption(
								"option.wenhan-client.player-highlighter.mode",
								PlayerHighlightMode.DISABLED,
								PlayerHighlightMode.class,
								() -> drafts.playerHighlighter.mode(),
								value -> drafts.playerHighlighter.setMode(value)))
				.option(
						booleanOption(
								"option.wenhan-client.player-waypoint-info.enabled",
								PlayerWaypointInfoConfig.DEFAULT_ENABLED,
								() -> drafts.playerWaypointInfo.enabled,
								value -> drafts.playerWaypointInfo.enabled = value))
				.build();
	}

	private static ConfigCategory movementCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.movement.config"))
				.option(
						booleanOption(
								"option.wenhan-client.creative-flying.enabled",
								CreativeFlyingConfig.DEFAULT_ENABLED,
								() -> drafts.creativeFlying.enabled,
								value -> drafts.creativeFlying.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.flight-disabler.creative_flight_enabled",
								FlightDisablerConfig.DEFAULT_CREATIVE_FLIGHT_ENABLED,
								() -> drafts.flightDisabler.creativeFlightEnabled,
								value -> drafts.flightDisabler.creativeFlightEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.flight-disabler.elytra_flight_enabled",
								FlightDisablerConfig.DEFAULT_ELYTRA_FLIGHT_ENABLED,
								() -> drafts.flightDisabler.elytraFlightEnabled,
								value -> drafts.flightDisabler.elytraFlightEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.elytra-cancel.enabled",
								ElytraCancelConfig.DEFAULT_ENABLED,
								() -> drafts.elytraCancel.enabled,
								value -> drafts.elytraCancel.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-flight-reset.enabled",
								NoFlightResetConfig.DEFAULT_ENABLED,
								() -> drafts.noFlightReset.enabled,
								value -> drafts.noFlightReset.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-jump-delay.enabled",
								NoJumpDelayConfig.DEFAULT_ENABLED,
								() -> drafts.noJumpDelay.enabled,
								value -> drafts.noJumpDelay.enabled = value))
				.option(
						integerFieldOption(
								"option.wenhan-client.coyote.time_ticks",
								CoyoteConfig.DEFAULT_TIME_TICKS,
								CoyoteConfig.MIN_TIME_TICKS,
								CoyoteConfig.MAX_TIME_TICKS,
								() -> drafts.coyote.timeTicks,
								value ->
										drafts.coyote.timeTicks =
												Math.max(CoyoteConfig.MIN_TIME_TICKS, value)))
				.option(
						booleanOption(
								"option.wenhan-client.no-levitation.enabled",
								NoLevitationConfig.DEFAULT_ENABLED,
								() -> drafts.noLevitation.enabled,
								value -> drafts.noLevitation.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-collision.enabled",
								NoCollisionConfig.DEFAULT_ENABLED,
								() -> drafts.noCollision.enabled,
								value -> drafts.noCollision.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.fluid-platform.enabled",
								FluidPlatformConfig.DEFAULT_ENABLED,
								() -> drafts.fluidPlatform.enabled,
								value -> drafts.fluidPlatform.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-slowdown.enabled",
								NoSlowdownConfig.DEFAULT_ENABLED,
								() -> drafts.noSlowdown.enabled,
								value -> drafts.noSlowdown.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-slime-bounce.enabled",
								NoSlimeBounceConfig.DEFAULT_ENABLED,
								() -> drafts.noSlimeBounce.enabled,
								value -> drafts.noSlimeBounce.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.minecart-direction.keep_direction_in_minecart",
								MinecartDirectionConfig.DEFAULT_KEEP_DIRECTION_IN_MINECART,
								() -> drafts.minecartDirection.keepDirectionInMinecart,
								value -> drafts.minecartDirection.keepDirectionInMinecart = value))
				.option(
						booleanOption(
								"option.wenhan-client.better-step.air_step_up_enabled",
								BetterStepConfig.DEFAULT_AIR_STEP_UP_ENABLED,
								() -> drafts.betterStep.airStepUpEnabled,
								value -> drafts.betterStep.airStepUpEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.better-step.step_down_enabled",
								BetterStepConfig.DEFAULT_STEP_DOWN_ENABLED,
								() -> drafts.betterStep.stepDownEnabled,
								value -> drafts.betterStep.stepDownEnabled = value))
				.option(
						doubleSliderOption(
								"option.wenhan-client.better-step.step_height",
								BetterStepConfig.DEFAULT_STEP_HEIGHT,
								BetterStepConfig.MIN_STEP_HEIGHT,
								BetterStepConfig.MAX_STEP_HEIGHT,
								0.1D,
								() -> drafts.betterStep.stepHeight,
								value -> drafts.betterStep.stepHeight = roundToOneDecimal(value),
								value ->
										Component.literal(
												String.format(Locale.ROOT, "%.1f", value))))
				.option(
						doubleSliderOption(
								"option.wenhan-client.better-step.sneaking_step_height",
								BetterStepConfig.DEFAULT_SNEAKING_STEP_HEIGHT,
								BetterStepConfig.MIN_STEP_HEIGHT,
								BetterStepConfig.MAX_STEP_HEIGHT,
								0.1D,
								() -> drafts.betterStep.sneakingStepHeight,
								value ->
										drafts.betterStep.sneakingStepHeight =
												roundToOneDecimal(value),
								value ->
										Component.literal(
												String.format(Locale.ROOT, "%.1f", value))))
				.option(
						booleanOption(
								"option.wenhan-client.jump-height.enabled",
								JumpHeightConfig.DEFAULT_ENABLED,
								() -> drafts.jumpHeight.enabled,
								value -> drafts.jumpHeight.enabled = value))
				.option(
						doubleFieldOption(
								"option.wenhan-client.jump-height.target_height",
								JumpHeightConfig.DEFAULT_TARGET_HEIGHT,
								JumpHeightConfig.MIN_TARGET_HEIGHT,
								JumpHeightConfig.MAX_TARGET_HEIGHT,
								() -> drafts.jumpHeight.targetHeight,
								value ->
										drafts.jumpHeight.targetHeight =
												clamp(
														value,
														JumpHeightConfig.MIN_TARGET_HEIGHT,
														JumpHeightConfig.MAX_TARGET_HEIGHT),
								value ->
										Component.literal(
												String.format(Locale.ROOT, "%.3f", value))))
				.option(
						booleanOption(
								"option.wenhan-client.double-jump.infinite_jumps",
								DoubleJumpConfig.DEFAULT_INFINITE_JUMPS,
								() -> drafts.doubleJump.infiniteJumps,
								value -> drafts.doubleJump.infiniteJumps = value))
				.option(
						integerFieldOption(
								"option.wenhan-client.double-jump.jump_count",
								DoubleJumpConfig.DEFAULT_JUMP_COUNT,
								DoubleJumpConfig.MIN_JUMP_COUNT,
								DoubleJumpConfig.MAX_JUMP_COUNT,
								() -> drafts.doubleJump.jumpCount,
								value ->
										drafts.doubleJump.jumpCount =
												Math.max(DoubleJumpConfig.MIN_JUMP_COUNT, value)))
				.option(
						booleanOption(
								"option.wenhan-client.double-jump.cooldown_enabled",
								DoubleJumpConfig.DEFAULT_COOLDOWN_ENABLED,
								() -> drafts.doubleJump.cooldownEnabled,
								value -> drafts.doubleJump.cooldownEnabled = value))
				.option(
						integerFieldOption(
								"option.wenhan-client.double-jump.cooldown_ticks",
								DoubleJumpConfig.DEFAULT_COOLDOWN_TICKS,
								DoubleJumpConfig.MIN_COOLDOWN_TICKS,
								DoubleJumpConfig.MAX_COOLDOWN_TICKS,
								() -> drafts.doubleJump.cooldownTicks,
								value ->
										drafts.doubleJump.cooldownTicks =
												Math.max(
														DoubleJumpConfig.MIN_COOLDOWN_TICKS,
														value)))
				.build();
	}

	private static ConfigCategory hidePasswordCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.hide-password.config"))
				.option(
						booleanOption(
								"option.wenhan-client.hide-password.enabled",
								HidePasswordConfig.DEFAULT_ENABLED,
								() -> drafts.hidePassword.enabled,
								value -> drafts.hidePassword.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.hide-password.hide_length",
								HidePasswordConfig.DEFAULT_HIDE_LENGTH,
								() -> drafts.hidePassword.hideLength,
								value -> drafts.hidePassword.hideLength = value))
				.build();
	}

	private static ConfigCategory whoIAmCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.who-i-am.config"))
				.option(
						booleanOption(
								"option.wenhan-client.who-i-am.show_local_player_name",
								WhoIAmConfig.DEFAULT_SHOW_LOCAL_PLAYER_NAME,
								() -> drafts.whoIAm.showLocalPlayerName,
								value -> drafts.whoIAm.showLocalPlayerName = value))
				.option(
						booleanOption(
								"option.wenhan-client.who-i-am.append_player_name_to_title",
								WhoIAmConfig.DEFAULT_APPEND_PLAYER_NAME_TO_TITLE,
								() -> drafts.whoIAm.appendPlayerNameToTitle,
								value -> drafts.whoIAm.appendPlayerNameToTitle = value))
				.build();
	}

	private static ConfigCategory noTextureRotationCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.no-texture-rotation.config"))
				.option(
						booleanOption(
								"option.wenhan-client.no-texture-rotation.enabled",
								NoTextureRotationConfig.DEFAULT_ENABLED,
								() -> drafts.noTextureRotation.enabled,
								value -> drafts.noTextureRotation.enabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-texture-rotation.disable_random_offset",
								NoTextureRotationConfig.DEFAULT_DISABLE_RANDOM_OFFSET,
								() -> drafts.noTextureRotation.disableRandomOffset,
								value -> drafts.noTextureRotation.disableRandomOffset = value))
				.option(
						booleanOption(
								"option.wenhan-client.no-texture-rotation.keep_collision_shape_offsets",
								NoTextureRotationConfig.DEFAULT_KEEP_COLLISION_SHAPE_OFFSETS,
								() -> drafts.noTextureRotation.keepCollisionShapeOffsets,
								value ->
										drafts.noTextureRotation.keepCollisionShapeOffsets = value))
				.build();
	}

	private static ConfigCategory clientPseudoUnrestrictCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.client-pseudo-unrestrict.config"))
				.option(
						booleanOption(
								"option.wenhan-client.client-pseudo-unrestrict.block_interaction_range",
								ClientPseudoUnrestrictConfig.DEFAULT_BLOCK_INTERACTION_RANGE,
								() -> drafts.clientPseudoUnrestrict.blockInteractionRange,
								value ->
										drafts.clientPseudoUnrestrict.blockInteractionRange =
												value))
				.option(
						booleanOption(
								"option.wenhan-client.client-pseudo-unrestrict.entity_interaction_range",
								ClientPseudoUnrestrictConfig.DEFAULT_ENTITY_INTERACTION_RANGE,
								() -> drafts.clientPseudoUnrestrict.entityInteractionRange,
								value ->
										drafts.clientPseudoUnrestrict.entityInteractionRange =
												value))
				.option(
						booleanOption(
								"option.wenhan-client.client-pseudo-unrestrict.instant_break",
								ClientPseudoUnrestrictConfig.DEFAULT_INSTANT_BREAK,
								() -> drafts.clientPseudoUnrestrict.instantBreak,
								value -> drafts.clientPseudoUnrestrict.instantBreak = value))
				.build();
	}

	private static ConfigCategory autoAimCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.auto-aim.config"))
				.option(
						enumOption(
								"option.wenhan-client.auto-aim.mode",
								AutoAimConfig.DEFAULT_MODE,
								AutoAimMode.class,
								() -> drafts.autoAim.mode,
								value -> drafts.autoAim.mode = value))
				.option(
						enumOption(
								"option.wenhan-client.auto-aim.view_restore",
								AutoAimConfig.DEFAULT_VIEW_RESTORE,
								ViewRestoreMode.class,
								() -> drafts.autoAim.viewRestore,
								value -> drafts.autoAim.viewRestore = value))
				.option(
						doubleFieldOption(
								"option.wenhan-client.auto-aim.max_attack_distance",
								AutoAimConfig.DEFAULT_MAX_ATTACK_DISTANCE,
								AutoAimConfig.MIN_MAX_ATTACK_DISTANCE,
								AutoAimConfig.MAX_MAX_ATTACK_DISTANCE,
								() -> drafts.autoAim.maxAttackDistance,
								value -> drafts.autoAim.maxAttackDistance = value,
								value -> Component.literal(Double.toString(value))))
				.option(
						booleanOption(
								"option.wenhan-client.auto-aim.highlight_target",
								AutoAimConfig.DEFAULT_HIGHLIGHT_TARGET,
								() -> drafts.autoAim.highlightTarget,
								value -> drafts.autoAim.highlightTarget = value))
				.option(
						colorOption(
								"option.wenhan-client.auto-aim.highlight_color",
								AutoAimConfig.DEFAULT_HIGHLIGHT_COLOR,
								() -> drafts.autoAim.highlightColor,
								value -> drafts.autoAim.highlightColor = value))
				.option(
						booleanOption(
								"option.wenhan-client.auto-aim.attack_through_walls",
								AutoAimConfig.DEFAULT_ATTACK_THROUGH_WALLS,
								() -> drafts.autoAim.attackThroughWalls,
								value -> drafts.autoAim.attackThroughWalls = value))
				.option(
						enumOption(
								"option.wenhan-client.auto-aim.auto_attack",
								AutoAimConfig.DEFAULT_AUTO_ATTACK,
								AutoAttackMode.class,
								() -> drafts.autoAim.autoAttack,
								value -> drafts.autoAim.autoAttack = value))
				.option(
						integerFieldOption(
								"option.wenhan-client.auto-aim.auto_attack_delay_ticks",
								AutoAimConfig.DEFAULT_AUTO_ATTACK_DELAY_TICKS,
								0,
								AutoAimConfig.MAX_AUTO_ATTACK_DELAY_TICKS,
								() -> drafts.autoAim.autoAttackDelayTicks,
								value -> drafts.autoAim.autoAttackDelayTicks = value))
				.build();
	}

	private static ConfigCategory placementRestrictionCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.placement-restriction.config"))
				.option(
						enumOption(
								"option.wenhan-client.placement-restriction.mode",
								PlacementRestrictionConfig.DEFAULT_MODE,
								PlacementRestrictionMode.class,
								() -> drafts.placementRestriction.mode,
								value -> drafts.placementRestriction.mode = value))
				.build();
	}

	private static ConfigCategory flySpeedModifierCategory(Drafts drafts) {
		double initialSpeedMinimum = flySpeedModifierInitialSpeedMinimum(drafts.flySpeedModifier);
		double initialSpeedMaximum = flySpeedModifierInitialSpeedMaximum(drafts.flySpeedModifier);
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.fly-speed-modifier.config"))
				.option(
						booleanOption(
								"option.wenhan-client.fly-speed-modifier.enabled",
								FlySpeedModifierConfig.DEFAULT_ENABLED,
								() -> drafts.flySpeedModifier.enabled,
								value -> drafts.flySpeedModifier.enabled = value))
				.option(
						doubleFieldOption(
								"option.wenhan-client.fly-speed-modifier.max_speed",
								FlySpeedModifierConfig.DEFAULT_MAX_SPEED,
								FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_MAX_SPEED,
								() -> drafts.flySpeedModifier.maxSpeed,
								value ->
										onFlySpeedModifierMaxSpeedChanged(
												drafts.flySpeedModifier, value),
								value ->
										Component.literal(
												FlySpeedModifierConfig.formatSpeed(value))))
				.option(
						doubleFieldOption(
								"option.wenhan-client.fly-speed-modifier.min_speed",
								FlySpeedModifierConfig.DEFAULT_MIN_SPEED,
								FlySpeedModifierConfig.MIN_ALLOWED_MIN_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED,
								() -> drafts.flySpeedModifier.minSpeed,
								value ->
										onFlySpeedModifierMinSpeedChanged(
												drafts.flySpeedModifier, value),
								value ->
										Component.literal(
												FlySpeedModifierConfig.formatSpeed(value))))
				.option(
						doubleFieldOption(
								"option.wenhan-client.fly-speed-modifier.initial_speed",
								FlySpeedModifierConfig.DEFAULT_INITIAL_SPEED,
								initialSpeedMinimum,
								initialSpeedMaximum,
								() -> drafts.flySpeedModifier.initialSpeed,
								value ->
										drafts.flySpeedModifier.initialSpeed =
												roundToTwoDecimals(
														constrainFlySpeedModifierInitialSpeed(
																drafts.flySpeedModifier, value)),
								value ->
										Component.literal(
												FlySpeedModifierConfig.formatSpeed(value))))
				.option(
						booleanOption(
								"option.wenhan-client.fly-speed-modifier.reset_on_adjust",
								FlySpeedModifierConfig.DEFAULT_RESET_ON_ADJUST,
								() -> drafts.flySpeedModifier.resetOnAdjust,
								value -> drafts.flySpeedModifier.resetOnAdjust = value))
				.option(
						doubleSliderOption(
								"option.wenhan-client.fly-speed-modifier.scroll_step",
								FlySpeedModifierConfig.DEFAULT_SCROLL_STEP,
								FlySpeedModifierConfig.MIN_ALLOWED_SCROLL_STEP,
								FlySpeedModifierConfig.MAX_ALLOWED_SCROLL_STEP,
								0.1D,
								() -> drafts.flySpeedModifier.scrollStep,
								value ->
										drafts.flySpeedModifier.scrollStep =
												roundToOneDecimal(value),
								value ->
										Component.literal(
												FlySpeedModifierConfig.formatScrollStep(value))))
				.option(
						booleanOption(
								"option.wenhan-client.fly-speed-modifier.apply_to_other_movement",
								FlySpeedModifierConfig.DEFAULT_APPLY_TO_OTHER_MOVEMENT,
								() -> drafts.flySpeedModifier.applyToOtherMovement,
								value -> drafts.flySpeedModifier.applyToOtherMovement = value))
				.build();
	}

	private static ConfigCategory boatUtilsCategory(Drafts drafts) {
		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.boat-utils.config"))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.unrestricted_view_rotation",
								BoatUtilsConfig.DEFAULT_UNRESTRICTED_VIEW_ROTATION,
								() -> drafts.boatUtils.unrestrictedViewRotation,
								value -> drafts.boatUtils.unrestrictedViewRotation = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.view_direction_lock_enabled",
								BoatUtilsConfig.DEFAULT_VIEW_DIRECTION_LOCK_ENABLED,
								() -> drafts.boatUtils.viewDirectionLockEnabled,
								value -> drafts.boatUtils.viewDirectionLockEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.camera_auto_recenter_enabled",
								BoatUtilsConfig.DEFAULT_CAMERA_AUTO_RECENTER_ENABLED,
								() -> drafts.boatUtils.cameraAutoRecenterEnabled,
								value -> drafts.boatUtils.cameraAutoRecenterEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.direction_hotkeys_enabled",
								BoatUtilsConfig.DEFAULT_DIRECTION_HOTKEYS_ENABLED,
								() -> drafts.boatUtils.directionHotkeysEnabled,
								value -> drafts.boatUtils.directionHotkeysEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.direction_hotkeys_use_45_degree_angles",
								BoatUtilsConfig.DEFAULT_DIRECTION_HOTKEYS_USE_45_DEGREE_ANGLES,
								() -> drafts.boatUtils.directionHotkeysUse45DegreeAngles,
								value ->
										drafts.boatUtils.directionHotkeysUse45DegreeAngles = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.blue_ice_speed_everywhere",
								BoatUtilsConfig.DEFAULT_BLUE_ICE_SPEED_EVERYWHERE,
								() -> drafts.boatUtils.blueIceSpeedEverywhere,
								value -> drafts.boatUtils.blueIceSpeedEverywhere = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.prevent_sinking",
								BoatUtilsConfig.DEFAULT_PREVENT_SINKING,
								() -> drafts.boatUtils.preventSinking,
								value -> drafts.boatUtils.preventSinking = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.handbrake_enabled",
								BoatUtilsConfig.DEFAULT_HANDBRAKE_ENABLED,
								() -> drafts.boatUtils.handbrakeEnabled,
								value -> drafts.boatUtils.handbrakeEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.handbrake_boost_enabled",
								BoatUtilsConfig.DEFAULT_HANDBRAKE_BOOST_ENABLED,
								() -> drafts.boatUtils.handbrakeBoostEnabled,
								value -> drafts.boatUtils.handbrakeBoostEnabled = value))
				.option(
						booleanOption(
								"option.wenhan-client.boat-utils.lateral_friction_enabled",
								BoatUtilsConfig.DEFAULT_LATERAL_FRICTION_ENABLED,
								() -> drafts.boatUtils.lateralFrictionEnabled,
								value -> drafts.boatUtils.lateralFrictionEnabled = value))
				.option(
						floatSliderOption(
								"option.wenhan-client.boat-utils.boat_step_height",
								BoatUtilsConfig.DEFAULT_BOAT_STEP_HEIGHT,
								0.0F,
								10.0F,
								0.1F,
								() -> drafts.boatUtils.boatStepHeight,
								value ->
										drafts.boatUtils.boatStepHeight =
												Math.round(value * 10.0F) / 10.0F,
								value ->
										Component.literal(
												String.format(Locale.ROOT, "%.1f", value))))
				.build();
	}

	private static Option<Boolean> booleanOption(
			String key, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(BooleanControllerBuilder::create)
				.build();
	}

	private static Option<Color> colorOption(
			String key, int defaultValue, Supplier<Integer> getter, Consumer<Integer> setter) {
		return Option.<Color>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(
						new Color(defaultValue & 0xFFFFFF),
						() -> new Color(getter.get() & 0xFFFFFF),
						value -> setter.accept(value.getRGB() & 0xFFFFFF))
				.controller(option -> ColorControllerBuilder.create(option).allowAlpha(false))
				.build();
	}

	private static Option<Integer> integerFieldOption(
			String key,
			int defaultValue,
			int min,
			int max,
			Supplier<Integer> getter,
			Consumer<Integer> setter) {
		return Option.<Integer>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(option -> IntegerFieldControllerBuilder.create(option).range(min, max))
				.build();
	}

	private static Option<Double> doubleSliderOption(
			String key,
			double defaultValue,
			double min,
			double max,
			double step,
			Supplier<Double> getter,
			Consumer<Double> setter,
			ValueFormatter<Double> formatter) {
		return Option.<Double>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(
						option ->
								DoubleSliderControllerBuilder.create(option)
										.range(min, max)
										.step(step)
										.formatValue(formatter))
				.build();
	}

	private static Option<Double> doubleFieldOption(
			String key,
			double defaultValue,
			double min,
			double max,
			Supplier<Double> getter,
			Consumer<Double> setter,
			ValueFormatter<Double> formatter) {
		return Option.<Double>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(
						option ->
								DoubleFieldControllerBuilder.create(option)
										.range(min, max)
										.formatValue(formatter))
				.build();
	}

	private static Option<Float> floatSliderOption(
			String key,
			float defaultValue,
			float min,
			float max,
			float step,
			Supplier<Float> getter,
			Consumer<Float> setter,
			ValueFormatter<Float> formatter) {
		return Option.<Float>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(
						option ->
								FloatSliderControllerBuilder.create(option)
										.range(min, max)
										.step(step)
										.formatValue(formatter))
				.build();
	}

	private static <T extends Enum<T>> Option<T> enumOption(
			String key,
			T defaultValue,
			Class<T> enumClass,
			Supplier<T> getter,
			Consumer<T> setter) {
		return Option.<T>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(
						option ->
								EnumControllerBuilder.create(option)
										.enumClass(enumClass)
										.formatValue(
												value ->
														Component.translatable(
																key
																		+ "."
																		+ value.name()
																				.toLowerCase(
																						Locale
																								.ROOT))))
				.build();
	}

	@SuppressWarnings("unused")
	private static Option<Integer> integerSliderOption(
			String key,
			int defaultValue,
			int min,
			int max,
			int step,
			Supplier<Integer> getter,
			Consumer<Integer> setter,
			ValueFormatter<Integer> formatter) {
		return Option.<Integer>createBuilder()
				.name(Component.translatable(key))
				.description(OptionDescription.of(Component.translatable(key + ".tooltip")))
				.binding(defaultValue, getter, setter)
				.controller(
						option ->
								IntegerSliderControllerBuilder.create(option)
										.range(min, max)
										.step(step)
										.formatValue(formatter))
				.build();
	}

	private static void onFlySpeedModifierMaxSpeedChanged(
			FlySpeedModifierConfig draft, double value) {
		draft.maxSpeed =
				roundToTwoDecimals(
						clamp(
								value,
								FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_MAX_SPEED));
		normalizeFlySpeedModifierSpeeds(draft);
	}

	private static void onFlySpeedModifierMinSpeedChanged(
			FlySpeedModifierConfig draft, double value) {
		draft.minSpeed =
				roundToTwoDecimals(
						clamp(
								value,
								FlySpeedModifierConfig.MIN_ALLOWED_MIN_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED));
		normalizeFlySpeedModifierSpeeds(draft);
	}

	private static void normalizeFlySpeedModifierSpeeds(FlySpeedModifierConfig draft) {
		draft.minSpeed =
				roundToTwoDecimals(
						clamp(
								draft.minSpeed,
								FlySpeedModifierConfig.MIN_ALLOWED_MIN_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_MIN_SPEED));
		draft.maxSpeed =
				roundToTwoDecimals(
						clamp(
								draft.maxSpeed,
								FlySpeedModifierConfig.MIN_ALLOWED_MAX_SPEED,
								FlySpeedModifierConfig.MAX_ALLOWED_MAX_SPEED));
		draft.initialSpeed =
				roundToTwoDecimals(
						constrainFlySpeedModifierInitialSpeed(draft, draft.initialSpeed));
	}

	private static double constrainFlySpeedModifierInitialSpeed(
			FlySpeedModifierConfig draft, double value) {
		return Math.max(
				flySpeedModifierInitialSpeedMinimum(draft),
				Math.min(value, flySpeedModifierInitialSpeedMaximum(draft)));
	}

	private static double flySpeedModifierInitialSpeedMinimum(FlySpeedModifierConfig draft) {
		return clamp(
				draft.minSpeed,
				FlySpeedModifierConfig.MIN_ALLOWED_INITIAL_SPEED,
				FlySpeedModifierConfig.MAX_ALLOWED_INITIAL_SPEED);
	}

	private static double flySpeedModifierInitialSpeedMaximum(FlySpeedModifierConfig draft) {
		double maxSpeed =
				clamp(
						draft.maxSpeed,
						FlySpeedModifierConfig.MIN_ALLOWED_INITIAL_SPEED,
						FlySpeedModifierConfig.MAX_ALLOWED_INITIAL_SPEED);
		return Math.max(flySpeedModifierInitialSpeedMinimum(draft), maxSpeed);
	}

	private static double clamp(double value, double min, double max) {
		if (!Double.isFinite(value)) {
			return min;
		}
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	private static double roundToOneDecimal(double value) {
		return Math.round(value * 10.0D) / 10.0D;
	}

	private static double roundToTwoDecimals(double value) {
		if (Math.abs(value) > Long.MAX_VALUE / 100.0D) {
			return value;
		}
		return Math.round(value * 100.0D) / 100.0D;
	}

	private static final class Drafts {
		private final BoatUtilsConfig boatUtils;
		private final AttackThroughFoliageConfig attackThroughFoliage;
		private final AutoAimConfig autoAim;
		private final BlockExplosionGuardConfig blockExplosionGuard;
		private final BetterStepConfig betterStep;
		private final ClientPseudoUnrestrictConfig clientPseudoUnrestrict;
		private final NoSlowdownConfig noSlowdown;
		private final NoSlimeBounceConfig noSlimeBounce;
		private final CreativeFlyingConfig creativeFlying;
		private final CoyoteConfig coyote;
		private final DoubleJumpConfig doubleJump;
		private final ElytraCancelConfig elytraCancel;
		private final FlightDisablerConfig flightDisabler;
		private final FluidPlatformConfig fluidPlatform;
		private final FlySpeedModifierConfig flySpeedModifier;
		private final HideRealmsConfig hideRealms;
		private final HidePasswordConfig hidePassword;
		private final InstantRespawnConfig instantRespawn;
		private final InteractionDelayConfig interactionDelay;
		private final JumpHeightConfig jumpHeight;
		private final MinecartDirectionConfig minecartDirection;
		private final NoCameraCollisionConfig noCameraCollision;
		private final NoCollisionConfig noCollision;
		private final NoFlightResetConfig noFlightReset;
		private final NoFogConfig noFog;
		private final NoJumpDelayConfig noJumpDelay;
		private final NoLevitationConfig noLevitation;
		private final NoTextureRotationConfig noTextureRotation;
		private final PlayerHighlighterConfig playerHighlighter;
		private final PlayerWaypointInfoConfig playerWaypointInfo;
		private final PlacementRestrictionConfig placementRestriction;
		private final StableMiningConfig stableMining;
		private final TransparentBlocksConfig transparentBlocks;
		private final WhoIAmConfig whoIAm;

		private Drafts() {
			this.boatUtils = BoatUtilsSubMod.config().copy();
			this.attackThroughFoliage = AttackThroughFoliageSubMod.config();
			this.autoAim = AutoAimSubMod.config();
			this.blockExplosionGuard = BlockExplosionGuardSubMod.config();
			this.betterStep = BetterStepSubMod.config();
			this.clientPseudoUnrestrict = ClientPseudoUnrestrictSubMod.config();
			this.noSlowdown = NoSlowdownSubMod.config();
			this.noSlimeBounce = NoSlimeBounceSubMod.config();
			this.creativeFlying = CreativeFlyingSubMod.config();
			this.coyote = CoyoteSubMod.config();
			this.doubleJump = DoubleJumpSubMod.config().copy();
			this.elytraCancel = ElytraCancelSubMod.config();
			this.flightDisabler = FlightDisablerSubMod.config();
			this.fluidPlatform = FluidPlatformSubMod.config();
			this.flySpeedModifier = FlySpeedModifierSubMod.config().copy();
			this.flySpeedModifier.fullRange = true;
			this.hideRealms = HideRealmsSubMod.config();
			this.hidePassword = HidePasswordSubMod.config();
			this.instantRespawn = InstantRespawnSubMod.config();
			this.interactionDelay = InteractionDelaySubMod.config();
			this.jumpHeight = JumpHeightSubMod.config();
			this.minecartDirection = MinecartDirectionSubMod.config();
			this.noCameraCollision = NoCameraCollisionSubMod.config();
			this.noCollision = NoCollisionSubMod.config();
			this.noFlightReset = NoFlightResetSubMod.config();
			this.noFog = NoFogSubMod.config();
			this.noJumpDelay = NoJumpDelaySubMod.config();
			this.noLevitation = NoLevitationSubMod.config();
			this.noTextureRotation = NoTextureRotationSubMod.config();
			this.playerHighlighter = PlayerHighlighterSubMod.config();
			this.playerWaypointInfo = PlayerWaypointInfoSubMod.config();
			this.placementRestriction = PlacementRestrictionSubMod.config();
			this.stableMining = StableMiningSubMod.config();
			this.transparentBlocks = TransparentBlocksSubMod.config();
			this.whoIAm = WhoIAmSubMod.config();
		}

		private static Drafts create() {
			return new Drafts();
		}

		private void save() {
			AttackThroughFoliageSubMod.setConfig(this.attackThroughFoliage);
			AutoAimSubMod.setConfig(this.autoAim);
			BlockExplosionGuardSubMod.setConfig(this.blockExplosionGuard);
			BetterStepSubMod.setConfig(this.betterStep);
			ClientPseudoUnrestrictSubMod.setConfig(this.clientPseudoUnrestrict);
			NoSlowdownSubMod.setConfig(this.noSlowdown);
			NoSlimeBounceSubMod.setConfig(this.noSlimeBounce);
			CreativeFlyingSubMod.setConfig(this.creativeFlying);
			CoyoteSubMod.setConfig(this.coyote);
			DoubleJumpSubMod.setConfig(this.doubleJump);
			ElytraCancelSubMod.setConfig(this.elytraCancel);
			FlightDisablerSubMod.setConfig(this.flightDisabler);
			FluidPlatformSubMod.setConfig(this.fluidPlatform);
			this.flySpeedModifier.fullRange = true;
			normalizeFlySpeedModifierSpeeds(this.flySpeedModifier);
			FlySpeedModifierSubMod.setConfig(this.flySpeedModifier);
			HideRealmsSubMod.setConfig(this.hideRealms);
			HidePasswordSubMod.setConfig(this.hidePassword);
			InstantRespawnSubMod.setConfig(this.instantRespawn);
			InteractionDelaySubMod.setConfig(this.interactionDelay);
			JumpHeightSubMod.setConfig(this.jumpHeight);
			MinecartDirectionSubMod.setConfig(this.minecartDirection);
			NoCameraCollisionSubMod.setConfig(this.noCameraCollision);
			NoCollisionSubMod.setConfig(this.noCollision);
			NoFlightResetSubMod.setConfig(this.noFlightReset);
			NoFogSubMod.setConfig(this.noFog);
			NoJumpDelaySubMod.setConfig(this.noJumpDelay);
			NoLevitationSubMod.setConfig(this.noLevitation);
			NoTextureRotationSubMod.setConfig(this.noTextureRotation);
			PlayerHighlighterSubMod.setConfig(this.playerHighlighter);
			PlayerWaypointInfoSubMod.setConfig(this.playerWaypointInfo);
			PlacementRestrictionSubMod.setConfig(this.placementRestriction);
			StableMiningSubMod.setConfig(this.stableMining);
			TransparentBlocksSubMod.setConfig(this.transparentBlocks);
			WhoIAmSubMod.setConfig(this.whoIAm);
			BoatUtilsSubMod.setConfig(this.boatUtils);
		}
	}
}

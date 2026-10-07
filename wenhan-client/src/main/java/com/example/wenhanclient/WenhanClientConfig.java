package com.example.wenhanclient;

import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageConfig;
import com.example.wenhanclient.autologin.AutoLoginConfig;
import com.example.wenhanclient.betterstep.BetterStepConfig;
import com.example.wenhanclient.boatutils.BoatUtilsConfig;
import com.example.wenhanclient.noslowdown.NoSlowdownConfig;
import com.example.wenhanclient.creativeflying.CreativeFlyingConfig;
import com.example.wenhanclient.doublejump.DoubleJumpConfig;
import com.example.wenhanclient.elytracancel.ElytraCancelConfig;
import com.example.wenhanclient.entityhighlighter.EntityHighlighterConfig;
import com.example.wenhanclient.flightdisabler.FlightDisablerConfig;
import com.example.wenhanclient.flyspeedmodifier.FlySpeedModifierConfig;
import com.example.wenhanclient.greet.GreetConfig;
import com.example.wenhanclient.hidepassword.config.HidePasswordConfig;
import com.example.wenhanclient.hiderealms.HideRealmsConfig;
import com.example.wenhanclient.interactiondelay.InteractionDelayConfig;
import com.example.wenhanclient.minecartdirection.MinecartDirectionConfig;
import com.example.wenhanclient.nocollision.NoCollisionConfig;
import com.example.wenhanclient.noflightreset.NoFlightResetConfig;
import com.example.wenhanclient.nojumpdelay.NoJumpDelayConfig;
import com.example.wenhanclient.notexturerotation.NoTextureRotationConfig;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterConfig;
import com.example.wenhanclient.transparentblocks.TransparentBlocksConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class WenhanClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("wenhan-client.json");

	public AttackThroughFoliageConfig attackThroughFoliage = new AttackThroughFoliageConfig();
	public AutoLoginConfig autoLogin = new AutoLoginConfig();
	public BetterStepConfig betterStep = new BetterStepConfig();
	public BoatUtilsConfig.Values boatUtils = BoatUtilsConfig.Values.defaults();
	public NoSlowdownConfig noSlowdown = new NoSlowdownConfig();
	public CreativeFlyingConfig creativeFlying = new CreativeFlyingConfig();
	public DoubleJumpConfig.Values doubleJump = DoubleJumpConfig.Values.defaults();
	public ElytraCancelConfig elytraCancel = new ElytraCancelConfig();
	public EntityHighlighterConfig entityHighlighter = new EntityHighlighterConfig();
	public FlightDisablerConfig flightDisabler = new FlightDisablerConfig();
	public FlySpeedModifierConfig.Values flySpeedModifier =
			FlySpeedModifierConfig.Values.defaults();
	public GreetConfig greet = new GreetConfig();
	public HideRealmsConfig hideRealms = new HideRealmsConfig();
	public HidePasswordConfig hidePassword = new HidePasswordConfig();
	public InteractionDelayConfig interactionDelay = new InteractionDelayConfig();
	public MinecartDirectionConfig minecartDirection = new MinecartDirectionConfig();
	public NoCollisionConfig noCollision = new NoCollisionConfig();
	public NoFlightResetConfig noFlightReset = new NoFlightResetConfig();
	public NoJumpDelayConfig noJumpDelay = new NoJumpDelayConfig();
	public NoTextureRotationConfig noTextureRotation = new NoTextureRotationConfig();
	public PlayerHighlighterConfig playerHighlighter = new PlayerHighlighterConfig();
	public TransparentBlocksConfig transparentBlocks = new TransparentBlocksConfig();

	public static WenhanClientConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			WenhanClientConfig cfg = new WenhanClientConfig();
			cfg.save();
			return cfg;
		}
		try {
			WenhanClientConfig cfg =
					GSON.fromJson(
							Files.readString(CONFIG_PATH, StandardCharsets.UTF_8),
							WenhanClientConfig.class);
			if (cfg == null) {
				cfg = new WenhanClientConfig();
			}
			cfg.fillMissing();
			return cfg;
		} catch (IOException | JsonParseException e) {
			throw new RuntimeException("Failed to load wenhan-client config", e);
		}
	}

	public void save() {
		fillMissing();
		try {
			Path parent = CONFIG_PATH.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(CONFIG_PATH, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save wenhan-client config", e);
		}
	}

	private void fillMissing() {
		if (attackThroughFoliage == null) {
			attackThroughFoliage = new AttackThroughFoliageConfig();
		}
		if (autoLogin == null) {
			autoLogin = new AutoLoginConfig();
		}
		if (autoLogin.servers == null) {
			autoLogin.servers = new java.util.HashMap<>();
		}
		if (betterStep == null) {
			betterStep = new BetterStepConfig();
		}
		betterStep = BetterStepConfig.sanitize(betterStep);
		if (boatUtils == null) {
			boatUtils = BoatUtilsConfig.Values.defaults();
		}
		boatUtils = BoatUtilsConfig.sanitize(boatUtils);
		if (noSlowdown == null) {
			noSlowdown = new NoSlowdownConfig();
		}
		if (creativeFlying == null) {
			creativeFlying = new CreativeFlyingConfig();
		}
		if (doubleJump == null) {
			doubleJump = DoubleJumpConfig.Values.defaults();
		}
		doubleJump = DoubleJumpConfig.sanitize(doubleJump);
		if (elytraCancel == null) {
			elytraCancel = new ElytraCancelConfig();
		}
		if (entityHighlighter == null) {
			entityHighlighter = new EntityHighlighterConfig();
		}
		entityHighlighter.fillMissing();
		if (flightDisabler == null) {
			flightDisabler = new FlightDisablerConfig();
		}
		if (flySpeedModifier == null) {
			flySpeedModifier = FlySpeedModifierConfig.Values.defaults();
		}
		flySpeedModifier = FlySpeedModifierConfig.sanitize(flySpeedModifier);
		if (greet == null) {
			greet = new GreetConfig();
		}
		greet.fillMissing();
		if (hideRealms == null) {
			hideRealms = new HideRealmsConfig();
		}
		if (hidePassword == null) {
			hidePassword = new HidePasswordConfig();
		}
		if (interactionDelay == null) {
			interactionDelay = new InteractionDelayConfig();
		}
		if (minecartDirection == null) {
			minecartDirection = new MinecartDirectionConfig();
		}
		if (noCollision == null) {
			noCollision = new NoCollisionConfig();
		}
		if (noFlightReset == null) {
			noFlightReset = new NoFlightResetConfig();
		}
		if (noJumpDelay == null) {
			noJumpDelay = new NoJumpDelayConfig();
		}
		if (noTextureRotation == null) {
			noTextureRotation = new NoTextureRotationConfig();
		}
		if (playerHighlighter == null) {
			playerHighlighter = new PlayerHighlighterConfig();
		}
		if (transparentBlocks == null) {
			transparentBlocks = new TransparentBlocksConfig();
		}
	}
}

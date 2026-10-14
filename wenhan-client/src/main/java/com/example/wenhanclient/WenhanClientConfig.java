package com.example.wenhanclient;

import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageConfig;
import com.example.wenhanclient.autologin.AutoLoginConfig;
import com.example.wenhanclient.betterstep.BetterStepConfig;
import com.example.wenhanclient.blockexplosionguard.BlockExplosionGuardConfig;
import com.example.wenhanclient.boatutils.BoatUtilsConfig;
import com.example.wenhanclient.clientpseudounrestrict.ClientPseudoUnrestrictConfig;
import com.example.wenhanclient.coyote.CoyoteConfig;
import com.example.wenhanclient.creativeflying.CreativeFlyingConfig;
import com.example.wenhanclient.doublejump.DoubleJumpConfig;
import com.example.wenhanclient.elytracancel.ElytraCancelConfig;
import com.example.wenhanclient.entityhighlighter.EntityHighlighterConfig;
import com.example.wenhanclient.flightdisabler.FlightDisablerConfig;
import com.example.wenhanclient.fluidplatform.FluidPlatformConfig;
import com.example.wenhanclient.flyspeedmodifier.FlySpeedModifierConfig;
import com.example.wenhanclient.greet.GreetConfig;
import com.example.wenhanclient.hidepassword.config.HidePasswordConfig;
import com.example.wenhanclient.hiderealms.HideRealmsConfig;
import com.example.wenhanclient.instantrespawn.InstantRespawnConfig;
import com.example.wenhanclient.interactiondelay.InteractionDelayConfig;
import com.example.wenhanclient.jumpheight.JumpHeightConfig;
import com.example.wenhanclient.minecartdirection.MinecartDirectionConfig;
import com.example.wenhanclient.nocameracollision.NoCameraCollisionConfig;
import com.example.wenhanclient.nocollision.NoCollisionConfig;
import com.example.wenhanclient.noflightreset.NoFlightResetConfig;
import com.example.wenhanclient.nofog.NoFogConfig;
import com.example.wenhanclient.nojumpdelay.NoJumpDelayConfig;
import com.example.wenhanclient.nolevitation.NoLevitationConfig;
import com.example.wenhanclient.noslimebounce.NoSlimeBounceConfig;
import com.example.wenhanclient.noslowdown.NoSlowdownConfig;
import com.example.wenhanclient.notexturerotation.NoTextureRotationConfig;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterConfig;
import com.example.wenhanclient.playerwaypointinfo.PlayerWaypointInfoConfig;
import com.example.wenhanclient.stablemining.StableMiningConfig;
import com.example.wenhanclient.transparentblocks.TransparentBlocksConfig;
import com.example.wenhanclient.whoiam.WhoIAmConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class WenhanClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("wenhan-client.json");

	public AttackThroughFoliageConfig attackThroughFoliage = new AttackThroughFoliageConfig();
	public AutoLoginConfig autoLogin = new AutoLoginConfig();
	public BetterStepConfig betterStep = new BetterStepConfig();
	public BlockExplosionGuardConfig blockExplosionGuard = new BlockExplosionGuardConfig();
	public BoatUtilsConfig boatUtils = new BoatUtilsConfig();
	public ClientPseudoUnrestrictConfig clientPseudoUnrestrict = new ClientPseudoUnrestrictConfig();
	public NoSlowdownConfig noSlowdown = new NoSlowdownConfig();
	public NoSlimeBounceConfig noSlimeBounce = new NoSlimeBounceConfig();
	public CreativeFlyingConfig creativeFlying = new CreativeFlyingConfig();
	public CoyoteConfig coyote = new CoyoteConfig();
	public DoubleJumpConfig doubleJump = new DoubleJumpConfig();
	public ElytraCancelConfig elytraCancel = new ElytraCancelConfig();
	public EntityHighlighterConfig entityHighlighter = new EntityHighlighterConfig();
	public FlightDisablerConfig flightDisabler = new FlightDisablerConfig();
	public FluidPlatformConfig fluidPlatform = new FluidPlatformConfig();
	public FlySpeedModifierConfig flySpeedModifier = new FlySpeedModifierConfig();
	public GreetConfig greet = new GreetConfig();
	public HideRealmsConfig hideRealms = new HideRealmsConfig();
	public HidePasswordConfig hidePassword = new HidePasswordConfig();
	public InstantRespawnConfig instantRespawn = new InstantRespawnConfig();
	public InteractionDelayConfig interactionDelay = new InteractionDelayConfig();
	public JumpHeightConfig jumpHeight = new JumpHeightConfig();
	public MinecartDirectionConfig minecartDirection = new MinecartDirectionConfig();
	public NoCameraCollisionConfig noCameraCollision = new NoCameraCollisionConfig();
	public NoCollisionConfig noCollision = new NoCollisionConfig();
	public NoFlightResetConfig noFlightReset = new NoFlightResetConfig();
	public NoFogConfig noFog = new NoFogConfig();
	public NoJumpDelayConfig noJumpDelay = new NoJumpDelayConfig();
	public NoLevitationConfig noLevitation = new NoLevitationConfig();
	public NoTextureRotationConfig noTextureRotation = new NoTextureRotationConfig();
	public PlayerHighlighterConfig playerHighlighter = new PlayerHighlighterConfig();
	public PlayerWaypointInfoConfig playerWaypointInfo = new PlayerWaypointInfoConfig();
	public StableMiningConfig stableMining = new StableMiningConfig();
	public TransparentBlocksConfig transparentBlocks = new TransparentBlocksConfig();
	public WhoIAmConfig whoIAm = new WhoIAmConfig();

	public static WenhanClientConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			WenhanClientConfig cfg = new WenhanClientConfig();
			cfg.save();
			return cfg;
		}
		try {
			String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
			JsonObject root = GSON.fromJson(json, JsonObject.class);
			WenhanClientConfig cfg = GSON.fromJson(root, WenhanClientConfig.class);
			if (cfg == null) {
				cfg = new WenhanClientConfig();
			}
			if (cfg.coyote == null) {
				cfg.coyote = new CoyoteConfig();
			}
			if (!root.has("coyote") && root.has("doubleJump")) {
				JsonObject doubleJump = root.getAsJsonObject("doubleJump");
				if (doubleJump.has("coyoteTimeTicks")) {
					cfg.coyote.timeTicks = doubleJump.get("coyoteTimeTicks").getAsInt();
				}
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
			autoLogin.servers = new HashMap<>();
		}
		if (betterStep == null) {
			betterStep = new BetterStepConfig();
		}
		betterStep.sanitize();
		if (blockExplosionGuard == null) {
			blockExplosionGuard = new BlockExplosionGuardConfig();
		}
		if (boatUtils == null) {
			boatUtils = new BoatUtilsConfig();
		}
		boatUtils.sanitize();
		if (clientPseudoUnrestrict == null) {
			clientPseudoUnrestrict = new ClientPseudoUnrestrictConfig();
		}
		if (noSlowdown == null) {
			noSlowdown = new NoSlowdownConfig();
		}
		if (noSlimeBounce == null) {
			noSlimeBounce = new NoSlimeBounceConfig();
		}
		if (creativeFlying == null) {
			creativeFlying = new CreativeFlyingConfig();
		}
		if (coyote == null) {
			coyote = new CoyoteConfig();
		}
		coyote.sanitize();
		if (doubleJump == null) {
			doubleJump = new DoubleJumpConfig();
		}
		doubleJump.sanitize();
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
		if (fluidPlatform == null) {
			fluidPlatform = new FluidPlatformConfig();
		}
		if (flySpeedModifier == null) {
			flySpeedModifier = new FlySpeedModifierConfig();
		}
		flySpeedModifier.sanitize();
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
		if (instantRespawn == null) {
			instantRespawn = new InstantRespawnConfig();
		}
		if (interactionDelay == null) {
			interactionDelay = new InteractionDelayConfig();
		}
		if (jumpHeight == null) {
			jumpHeight = new JumpHeightConfig();
		}
		jumpHeight.sanitize();
		if (minecartDirection == null) {
			minecartDirection = new MinecartDirectionConfig();
		}
		if (noCameraCollision == null) {
			noCameraCollision = new NoCameraCollisionConfig();
		}
		if (noCollision == null) {
			noCollision = new NoCollisionConfig();
		}
		if (noFlightReset == null) {
			noFlightReset = new NoFlightResetConfig();
		}
		if (noFog == null) {
			noFog = new NoFogConfig();
		}
		if (noJumpDelay == null) {
			noJumpDelay = new NoJumpDelayConfig();
		}
		if (noLevitation == null) {
			noLevitation = new NoLevitationConfig();
		}
		if (noTextureRotation == null) {
			noTextureRotation = new NoTextureRotationConfig();
		}
		if (playerHighlighter == null) {
			playerHighlighter = new PlayerHighlighterConfig();
		}
		if (playerWaypointInfo == null) {
			playerWaypointInfo = new PlayerWaypointInfoConfig();
		}
		if (stableMining == null) {
			stableMining = new StableMiningConfig();
		}
		if (transparentBlocks == null) {
			transparentBlocks = new TransparentBlocksConfig();
		}
		if (whoIAm == null) {
			whoIAm = new WhoIAmConfig();
		}
	}
}

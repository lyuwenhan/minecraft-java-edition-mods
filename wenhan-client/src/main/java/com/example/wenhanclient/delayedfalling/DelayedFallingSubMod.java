package com.example.wenhanclient.delayedfalling;

import com.example.wenhanclient.WenhanClientMod;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class DelayedFallingSubMod {
	private static final double SUPPORT_PROBE_DEPTH = 1.0E-4D;
	private static final double SUPPORT_PROBE_INSET = 1.0E-4D;
	private static DelayedFallingConfig config = new DelayedFallingConfig();
	private static LocalPlayer trackedPlayer;
	private static boolean delaying;
	private static int ticksRemaining;

	private DelayedFallingSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.delayedFalling.copy();
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
	}

	public static DelayedFallingConfig config() {
		return config.copy();
	}

	public static void setConfig(DelayedFallingConfig newConfig) {
		config = newConfig == null ? new DelayedFallingConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.delayedFalling = config.copy();
		WenhanClientMod.CONFIG.save();
		if (config.delayTicks == 0) {
			reset();
		}
	}

	public static void beginAiStep(LocalPlayer player) {
		if (trackedPlayer != player) {
			reset();
			trackedPlayer = player;
		}

		if (config.delayTicks == 0 || !canDelay(player)) {
			delaying = false;
			ticksRemaining = 0;
			return;
		}

		if (player.onGround()) {
			delaying = false;
			ticksRemaining = 0;
			return;
		}

		if (delaying && hasSupportBelow(player)) {
			delaying = false;
			ticksRemaining = 0;
			player.setOnGround(true);
			player.resetFallDistance();
			return;
		}

		Vec3 movement = player.getDeltaMovement();
		if (movement.y > 0.0D) {
			delaying = false;
			ticksRemaining = 0;
			return;
		}

		if (delaying) {
			player.setDeltaMovement(movement.x, 0.0D, movement.z);
			player.resetFallDistance();
		}
	}

	public static void endAiStep(LocalPlayer player) {
		if (trackedPlayer != player || !delaying) {
			return;
		}
		Vec3 movement = player.getDeltaMovement();
		player.setDeltaMovement(movement.x, 0.0D, movement.z);
		player.resetFallDistance();
		if (ticksRemaining > 0) {
			ticksRemaining--;
		}
		if (ticksRemaining == 0) {
			delaying = false;
		}
	}

	public static boolean isDelaying(LocalPlayer player) {
		return player != null && trackedPlayer == player && delaying;
	}

	public static void startAfterWalkingOffEdge(LocalPlayer player, double previousY) {
		if (config.delayTicks == 0 || !canDelay(player)) {
			return;
		}
		if (trackedPlayer != player) {
			reset();
			trackedPlayer = player;
		}

		delaying = true;
		ticksRemaining = config.delayTicks;
		player.setPos(player.getX(), previousY, player.getZ());
		Vec3 movement = player.getDeltaMovement();
		player.setDeltaMovement(movement.x, 0.0D, movement.z);
		player.resetFallDistance();
	}

	public static void stopDelay(LocalPlayer player) {
		if (trackedPlayer == player) {
			delaying = false;
			ticksRemaining = 0;
		}
	}

	private static boolean canDelay(LocalPlayer player) {
		return !player.isPassenger()
				&& !player.isFallFlying()
				&& !player.isInWater()
				&& !player.isInLava()
				&& !player.getAbilities().flying;
	}

	private static boolean hasSupportBelow(LocalPlayer player) {
		AABB bounds = player.getBoundingBox();
		AABB probe =
				new AABB(
						bounds.minX + SUPPORT_PROBE_INSET,
						bounds.minY - SUPPORT_PROBE_DEPTH,
						bounds.minZ + SUPPORT_PROBE_INSET,
						bounds.maxX - SUPPORT_PROBE_INSET,
						bounds.minY,
						bounds.maxZ - SUPPORT_PROBE_INSET);
		return !player.level().noCollision(player, probe);
	}

	private static void reset() {
		trackedPlayer = null;
		delaying = false;
		ticksRemaining = 0;
	}
}

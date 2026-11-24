package com.example.wenhanclient.freecaminteraction;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.autoaim.AutoAimSubMod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FreecamInteractionSubMod {
	private static final long RESTORE_DELAY_NANOS = 500_000_000L;
	private static final long AUTO_AIM_DELAY_NANOS = 250_000_000L;
	private static FreecamInteractionConfig config = new FreecamInteractionConfig();
	private static LocalPlayer trackedPlayer;
	private static boolean hasRotation;
	private static boolean requestedThisTick;
	private static int interactionDepth;
	private static long lastRequestNanos;
	private static float savedYaw;
	private static float savedPitch;

	private FreecamInteractionSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.freecamInteraction.copy();
		ClientTickEvents.START_CLIENT_TICK.register(FreecamInteractionSubMod::beginTick);
		ClientTickEvents.END_CLIENT_TICK.register(FreecamInteractionSubMod::endTick);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
	}

	public static FreecamInteractionConfig config() {
		return config.copy();
	}

	public static void setConfig(FreecamInteractionConfig newConfig) {
		FreecamInteractionConfig updated =
				newConfig == null ? new FreecamInteractionConfig() : newConfig.copy();
		if (updated.enabled != config.enabled || updated.restoreView != config.restoreView) {
			finish(Minecraft.getInstance());
		}
		config = updated;
		WenhanClientMod.CONFIG.freecamInteraction = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	private static boolean canControl(Minecraft client) {
		return config.enabled
				&& client != null
				&& client.player != null
				&& client.level != null
				&& client.player.isAlive()
				&& FreecamCompat.canInteract();
	}

	public static boolean enterInteraction() {
		if (!canControl(Minecraft.getInstance())) {
			return false;
		}
		interactionDepth++;
		return true;
	}

	public static void leaveInteraction(boolean entered) {
		if (entered) {
			interactionDepth = Math.max(0, interactionDepth - 1);
		}
	}

	public static boolean isControlling() {
		return interactionDepth > 0 || requestedThisTick;
	}

	public static boolean isFreecamActive() {
		return FreecamCompat.isActive();
	}

	public static boolean shouldUseInteractionTarget() {
		Minecraft client = Minecraft.getInstance();
		return canControl(client)
				&& (client.options.keyAttack.isDown()
						|| client.options.keyUse.isDown()
						|| client.options.keyPickItem.isDown());
	}

	public static boolean isAutoAimReady() {
		return !isControlling()
				&& (!hasRotation
						|| !config.restoreView
						|| System.nanoTime() - lastRequestNanos >= AUTO_AIM_DELAY_NANOS);
	}

	public static boolean tryAcquireAutoAim() {
		if (!isAutoAimReady()) {
			return false;
		}
		clearRotation();
		return true;
	}

	public static void faceBlock(BlockPos pos) {
		Minecraft client = Minecraft.getInstance();
		Vec3 target =
				client.hitResult instanceof BlockHitResult hit && hit.getBlockPos().equals(pos)
						? hit.getLocation()
						: Vec3.atCenterOf(pos);
		face(target);
	}

	public static void faceCrosshair() {
		Minecraft client = Minecraft.getInstance();
		if (!canControl(client)) {
			return;
		}
		HitResult hit = client.hitResult;
		if (hit != null && hit.getType() != HitResult.Type.MISS) {
			face(hit.getLocation());
		} else {
			Entity camera = client.getCameraEntity();
			if (camera != null) {
				face(
						camera.getEyePosition()
								.add(
										camera.getViewVector(1.0F)
												.scale(client.player.blockInteractionRange())));
			}
		}
	}

	public static void face(Vec3 target) {
		Minecraft client = Minecraft.getInstance();
		if (!canControl(client) || AutoAimSubMod.isAttackingAutomatically()) {
			return;
		}
		LocalPlayer player = client.player;
		Vec3 delta = target.subtract(player.getEyePosition());
		if (delta.lengthSqr() < 1.0E-12D) {
			return;
		}
		if (!hasRotation || trackedPlayer != player) {
			trackedPlayer = player;
			savedYaw = player.getYRot();
			savedPitch = player.getXRot();
			AutoAimSubMod.suspendForFreecam();
		}
		hasRotation = true;
		requestedThisTick = true;
		lastRequestNanos = System.nanoTime();
		float yaw =
				Mth.wrapDegrees((float) (Math.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F);
		float pitch =
				Mth.clamp(
						(float)
								(-Math.atan2(
												delta.y,
												Math.sqrt(delta.x * delta.x + delta.z * delta.z))
										* Mth.RAD_TO_DEG),
						-90.0F,
						90.0F);
		rotate(player, yaw, pitch);
	}

	private static void rotate(LocalPlayer player, float yaw, float pitch) {
		player.setYRot(yaw);
		player.setXRot(pitch);
		if (player.connection != null) {
			player.connection.send(
					new ServerboundMovePlayerPacket.Rot(
							yaw, pitch, player.onGround(), player.horizontalCollision));
		}
	}

	private static void beginTick(Minecraft client) {
		requestedThisTick = false;
		if (hasRotation && (trackedPlayer != client.player || client.level == null)) {
			reset();
		}
		if (!canControl(client)) {
			finish(client);
			return;
		}
		if (client.player.isUsingItem()) {
			faceCrosshair();
		}
	}

	private static void endTick(Minecraft client) {
		if (hasRotation
				&& !isControlling()
				&& System.nanoTime() - lastRequestNanos >= RESTORE_DELAY_NANOS) {
			finish(client);
		}
	}

	private static void finish(Minecraft client) {
		if (hasRotation
				&& config.restoreView
				&& client != null
				&& trackedPlayer == client.player
				&& client.level != null
				&& trackedPlayer.isAlive()) {
			rotate(trackedPlayer, savedYaw, savedPitch);
		}
		clearRotation();
	}

	private static void clearRotation() {
		hasRotation = false;
		trackedPlayer = null;
	}

	private static void reset() {
		clearRotation();
		requestedThisTick = false;
		interactionDepth = 0;
	}
}

package com.example.wenhanclient.autoaim;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.freecaminteraction.FreecamInteractionSubMod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;

public final class AutoAimSubMod {
	private static AutoAimConfig config = new AutoAimConfig();
	private static LivingEntity target;
	private static LivingEntity lastAttackedTarget;
	private static int tickCounter;
	private static int lastAttackTick = Integer.MIN_VALUE;
	private static int cooldownReadyTick = -1;
	private static int targetSelectedTick = Integer.MIN_VALUE;
	private static boolean attackingAutomatically;
	private static boolean hasSavedView;
	private static boolean mouseMovedWhileAiming;
	private static float savedYaw;
	private static float savedPitch;

	private AutoAimSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.autoAim.copy();
		ClientTickEvents.END_CLIENT_TICK.register(AutoAimSubMod::onEndClientTick);
	}

	public static AutoAimConfig config() {
		return config.copy();
	}

	public static boolean isAttackingAutomatically() {
		return attackingAutomatically;
	}

	public static void suspendForFreecam() {
		target = null;
		hasSavedView = false;
		mouseMovedWhileAiming = false;
		cooldownReadyTick = -1;
	}

	public static void setConfig(AutoAimConfig newConfig) {
		AutoAimMode previousMode = config.mode;
		config = newConfig == null ? new AutoAimConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.autoAim = config.copy();
		WenhanClientMod.CONFIG.save();
		if (config.mode != previousMode) {
			clearState(Minecraft.getInstance());
		}
	}

	public static void recordAttack(Entity attacked) {
		Minecraft client = Minecraft.getInstance();
		if (config.mode == AutoAimMode.DISABLED
				|| attackingAutomatically
				|| isHoldingBlockPlacement(client)
				|| !(attacked instanceof LivingEntity living)) {
			return;
		}
		if (!isValidTarget(client, living)) {
			return;
		}
		lastAttackedTarget = living;
		lastAttackTick = tickCounter;
		cooldownReadyTick = -1;
		if (config.mode == AutoAimMode.AFTER_ATTACK) {
			setTarget(client, living);
		}
	}

	public static void recordMouseMovement(double deltaX, double deltaY) {
		if (hasSavedView
				&& target != null
				&& (Math.abs(deltaX) > 1.0E-4D || Math.abs(deltaY) > 1.0E-4D)) {
			mouseMovedWhileAiming = true;
		}
	}

	public static boolean shouldCancelMouseMovement(Minecraft client) {
		return !FreecamInteractionSubMod.isFreecamActive()
				&& FreecamInteractionSubMod.isAutoAimReady()
				&& !FreecamInteractionSubMod.shouldUseInteractionTarget()
				&& !isHoldingBlockPlacement(client)
				&& isValidTarget(client, target);
	}

	public static boolean isHighlightedTarget(Entity entity) {
		return config.highlightTarget
				&& !isHoldingBlockPlacement(Minecraft.getInstance())
				&& entity == target
				&& isValidTarget(Minecraft.getInstance(), target);
	}

	public static Integer getTargetHighlightColor(Entity entity) {
		return isHighlightedTarget(entity) ? config.highlightColor : null;
	}

	public static LivingEntity getCrosshairTarget(Minecraft client) {
		if (!FreecamInteractionSubMod.isAutoAimReady()
				|| FreecamInteractionSubMod.shouldUseInteractionTarget()
				|| isHoldingBlockPlacement(client)
				|| !isValidTarget(client, target)) {
			return null;
		}
		return target;
	}

	public static boolean tryAttackLockedTarget(Minecraft client) {
		if (isHoldingBlockPlacement(client) || !isValidTarget(client, target)) {
			return false;
		}
		if (!FreecamInteractionSubMod.tryAcquireAutoAim()) {
			return false;
		}
		attack(client, target);
		return true;
	}

	private static void onEndClientTick(Minecraft client) {
		tickCounter++;
		if (FreecamInteractionSubMod.isControlling()) {
			cooldownReadyTick = -1;
			return;
		}
		if (!isClientReady(client) || config.mode == AutoAimMode.DISABLED) {
			clearState(client);
			return;
		}
		if (isHoldingBlockPlacement(client)) {
			cooldownReadyTick = -1;
			return;
		}

		boolean newlySelected = false;
		if (config.mode == AutoAimMode.AUTOMATIC) {
			LivingEntity previousTarget = target;
			setTarget(client, findNearestTarget(client));
			newlySelected = target != null && target != previousTarget;
		} else if (!isValidTarget(client, target)) {
			setTarget(client, null);
		}

		if (target == null) {
			return;
		}
		if (!FreecamInteractionSubMod.tryAcquireAutoAim()) {
			return;
		}

		aimAtTarget(client, target);
		tryAutoAttack(client, target, newlySelected);
	}

	private static void tryAutoAttack(
			Minecraft client, LivingEntity currentTarget, boolean newlySelected) {
		if (config.autoAttack == AutoAttackMode.DISABLED || lastAttackTick == tickCounter) {
			return;
		}

		if (config.autoAttack == AutoAttackMode.AFTER_COOLDOWN) {
			if (client.player.getAttackStrengthScale(0.0F) < 1.0F) {
				cooldownReadyTick = -1;
				return;
			}
			if (cooldownReadyTick < 0) {
				cooldownReadyTick = tickCounter;
			}
			if (tickCounter - cooldownReadyTick < config.autoAttackDelayTicks) {
				return;
			}
		} else {
			int delayStartTick =
					newlySelected || currentTarget != lastAttackedTarget
							? targetSelectedTick
							: lastAttackTick;
			if (tickCounter - delayStartTick < config.autoAttackDelayTicks) {
				return;
			}
		}

		attack(client, currentTarget);
	}

	private static void attack(Minecraft client, LivingEntity attackedTarget) {
		if (client.gameMode == null || client.player == null || lastAttackTick == tickCounter) {
			return;
		}
		attackingAutomatically = true;
		try {
			client.player.connection.send(new ServerboundAttackPacket(attackedTarget.getId()));
			client.player.resetAttackStrengthTicker();
		} finally {
			attackingAutomatically = false;
		}
		lastAttackedTarget = attackedTarget;
		lastAttackTick = tickCounter;
		cooldownReadyTick = -1;
	}

	private static LivingEntity findNearestTarget(Minecraft client) {
		LivingEntity nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Entity entity : client.level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity living) || !isValidTarget(client, living)) {
				continue;
			}
			double distance = client.player.distanceToSqr(living);
			if (distance < nearestDistance) {
				nearest = living;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	private static boolean isClientReady(Minecraft client) {
		return client != null
				&& client.level != null
				&& client.player != null
				&& client.gameMode != null
				&& !client.gameMode.isSpectator()
				&& client.gui.screen() == null;
	}

	private static boolean isHoldingBlockPlacement(Minecraft client) {
		return client != null
				&& client.player != null
				&& client.options.keyUse.isDown()
				&& (client.player.getMainHandItem().getItem() instanceof BlockItem
						|| client.player.getOffhandItem().getItem() instanceof BlockItem);
	}

	private static boolean isValidTarget(Minecraft client, LivingEntity candidate) {
		if (!isClientReady(client)
				|| candidate == null
				|| candidate == client.player
				|| (client.getCameraEntity() != client.player
						&& candidate == client.getCameraEntity())
				|| !candidate.isAlive()
				|| !candidate.isAttackable()
				|| !candidate.isPickable()
				|| candidate.level() != client.level) {
			return false;
		}

		double maximumDistance =
				Math.min(
						config.maxAttackDistance,
						client.player
								.getAttackRangeWith(client.player.getMainHandItem())
								.effectiveMaxRange(client.player));
		return candidate.getBoundingBox().distanceToSqr(client.player.getEyePosition())
						<= maximumDistance * maximumDistance
				&& client.player.isWithinAttackRange(
						client.player.getMainHandItem(), candidate.getBoundingBox(), 0.0D);
	}

	private static void aimAtTarget(Minecraft client, LivingEntity aimedTarget) {
		if (!hasSavedView) {
			savedYaw = client.player.getYRot();
			savedPitch = client.player.getXRot();
			hasSavedView = true;
			mouseMovedWhileAiming = false;
		}
		Vec3 eye = client.player.getEyePosition();
		Vec3 targetEye = aimedTarget.getEyePosition();
		Vec3 delta = targetEye.subtract(eye);
		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float yaw =
				Mth.wrapDegrees((float) (Math.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F);
		float pitch =
				Mth.clamp(
						(float) (-(Math.atan2(delta.y, horizontal) * Mth.RAD_TO_DEG)),
						-90.0F,
						90.0F);
		client.player.setYRot(yaw);
		client.player.setXRot(pitch);
	}

	private static void setTarget(Minecraft client, LivingEntity newTarget) {
		if (target == newTarget) {
			return;
		}
		if (newTarget == null) {
			restoreViewIfConfigured(client);
		}
		target = newTarget;
		targetSelectedTick = newTarget == null ? Integer.MIN_VALUE : tickCounter;
	}

	private static void restoreViewIfConfigured(Minecraft client) {
		boolean shouldRestore =
				config.viewRestore == ViewRestoreMode.ALWAYS
						|| (config.viewRestore == ViewRestoreMode.IF_MOUSE_UNMOVED
								&& !mouseMovedWhileAiming);
		if (hasSavedView
				&& shouldRestore
				&& client != null
				&& client.player != null
				&& FreecamInteractionSubMod.tryAcquireAutoAim()) {
			client.player.setYRot(savedYaw);
			client.player.setXRot(savedPitch);
		}
		hasSavedView = false;
		mouseMovedWhileAiming = false;
	}

	private static void clearState(Minecraft client) {
		setTarget(client, null);
		lastAttackedTarget = null;
		lastAttackTick = Integer.MIN_VALUE;
		cooldownReadyTick = -1;
		targetSelectedTick = Integer.MIN_VALUE;
	}
}

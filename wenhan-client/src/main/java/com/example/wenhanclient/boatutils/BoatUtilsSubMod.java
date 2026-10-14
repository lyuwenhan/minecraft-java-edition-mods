package com.example.wenhanclient.boatutils;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.boatutils.mixin.AbstractBoatAccessor;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public final class BoatUtilsSubMod {
	public static final String MOD_ID = "boat-utils";
	private static final int CAMERA_AUTO_RECENTER_DELAY_TICKS = 30;
	private static final float CAMERA_AUTO_RECENTER_YAW_STEP = 0.12F;
	private static final float CAMERA_AUTO_RECENTER_PITCH_STEP = 0.16F;
	private static final float CAMERA_AUTO_RECENTER_SNAP_EPSILON = 0.05F;
	private static final float HANDBRAKE_ROTATION_SNAP_EPSILON = 0.05F;

	private static AbstractBoat handbrakeBoat;
	private static int handbrakeTicks;
	private static boolean handbrakeWasDown;
	private static int cameraIdleTicks;
	private static BoatUtilsConfig config = new BoatUtilsConfig();

	private BoatUtilsSubMod() {}

	public static void init() {
		config =
				WenhanClientMod.CONFIG.boatUtils == null
						? new BoatUtilsConfig()
						: WenhanClientMod.CONFIG.boatUtils;
		config.sanitize();
		WenhanClientMod.CONFIG.boatUtils = config.copy();
		BoatDirectionHotkeys.initialize();
		ClientTickEvents.START_CLIENT_TICK.register(BoatUtilsSubMod::applyFollowView);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsSubMod::applyFollowView);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsSubMod::applyCameraAutoRecenter);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsSubMod::applyLateralFriction);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsSubMod::clearInvalidHandbrakeState);
		ClientTickEvents.END_CLIENT_TICK.register(BoatDirectionHotkeys::handleClientTick);
	}

	public static BoatUtilsConfig config() {
		return config;
	}

	public static void setConfig(BoatUtilsConfig newConfig) {
		config = newConfig == null ? new BoatUtilsConfig() : newConfig;
		config.sanitize();
		WenhanClientMod.CONFIG.boatUtils = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	private static void applyFollowView(Minecraft client) {
		if (!config.viewDirectionLockEnabled) {
			return;
		}
		if (client.player == null) {
			return;
		}

		Entity vehicle = client.player.getVehicle();
		if (!(vehicle instanceof AbstractBoat boat)) {
			return;
		}

		if (boat.getControllingPassenger() != client.player) {
			return;
		}

		float viewYaw = client.player.getYRot();
		boat.setYRot(viewYaw);
		boat.yRotO = viewYaw;
		boat.setYBodyRot(viewYaw);
	}

	private static void applyLateralFriction(Minecraft client) {
		if (!config.lateralFrictionEnabled || client.player == null) {
			return;
		}

		Entity vehicle = client.player.getVehicle();
		if (!(vehicle instanceof AbstractBoat boat)
				|| boat.getControllingPassenger() != client.player) {
			return;
		}

		Vec3 movement = boat.getDeltaMovement();
		float yawRadians = boat.getYRot() * Mth.DEG_TO_RAD;
		double sinYaw = Mth.sin(yawRadians);
		double cosYaw = Mth.cos(yawRadians);

		double forwardX = -sinYaw;
		double forwardZ = cosYaw;
		double rightX = -cosYaw;
		double rightZ = -sinYaw;

		double forwardSpeed = movement.x * forwardX + movement.z * forwardZ;
		double lateralSpeed = movement.x * rightX + movement.z * rightZ;
		double lateralMagnitude = Math.max(Math.abs(lateralSpeed) * 0.96D - 0.01D, 0.0D);
		double newLateralSpeed = Math.copySign(lateralMagnitude, lateralSpeed);

		boat.setDeltaMovement(
				forwardX * forwardSpeed + rightX * newLateralSpeed,
				movement.y,
				forwardZ * forwardSpeed + rightZ * newLateralSpeed);
	}

	public static void markCameraMovedByMouse() {
		cameraIdleTicks = 0;
	}

	private static void applyCameraAutoRecenter(Minecraft client) {
		if (!config.cameraAutoRecenterEnabled
				|| config.viewDirectionLockEnabled
				|| client.player == null
				|| client.gui.screen() != null) {
			resetCameraAutoRecenterState();
			return;
		}

		Entity vehicle = client.player.getVehicle();
		if (!(vehicle instanceof AbstractBoat boat)
				|| boat.getControllingPassenger() != client.player) {
			resetCameraAutoRecenterState();
			return;
		}

		if (cameraIdleTicks < CAMERA_AUTO_RECENTER_DELAY_TICKS) {
			cameraIdleTicks++;
			return;
		}

		float targetYaw = boat.getYRot();
		float yawDelta = Mth.wrapDegrees(targetYaw - client.player.getYRot());
		float pitchDelta = -client.player.getXRot();

		float newYaw = client.player.getYRot() + yawDelta * CAMERA_AUTO_RECENTER_YAW_STEP;
		float newPitch = client.player.getXRot() + pitchDelta * CAMERA_AUTO_RECENTER_PITCH_STEP;

		if (Math.abs(yawDelta) <= CAMERA_AUTO_RECENTER_SNAP_EPSILON) {
			newYaw = targetYaw;
		}
		if (Math.abs(pitchDelta) <= CAMERA_AUTO_RECENTER_SNAP_EPSILON) {
			newPitch = 0.0F;
		}

		client.player.setYRot(newYaw);
		client.player.setXRot(newPitch);
	}

	public static void applyHandbrakeAfterThrust(AbstractBoat boat) {
		Minecraft client = Minecraft.getInstance();
		if (!isControlledByLocalPlayer(client, boat)) {
			resetHandbrakeState();
			return;
		}

		if (handbrakeBoat != boat) {
			resetHandbrakeState();
			handbrakeBoat = boat;
		}

		boolean handbrakeDown = config.handbrakeEnabled && client.options.keyJump.isDown();
		if (handbrakeDown) {
			if (config.handbrakeBoostEnabled) {
				handbrakeTicks++;
			}
			handbrakeWasDown = true;
			applyFixedHandbrake(boat);
			return;
		}

		if (handbrakeWasDown
				&& config.handbrakeBoostEnabled
				&& handbrakeTicks > 0
				&& client.options.keyUp.isDown()) {
			applyReleaseBoost(boat, handbrakeTicks);
		}
		resetHandbrakeState();
	}

	private static void clearInvalidHandbrakeState(Minecraft client) {
		if (handbrakeBoat == null) {
			return;
		}
		if (!isControlledByLocalPlayer(client, handbrakeBoat)) {
			resetHandbrakeState();
		}
	}

	private static boolean isControlledByLocalPlayer(Minecraft client, AbstractBoat boat) {
		return client.player != null
				&& client.player.getVehicle() == boat
				&& boat.getControllingPassenger() == client.player;
	}

	private static void applyFixedHandbrake(AbstractBoat boat) {
		Vec3 movement = boat.getDeltaMovement();
		double speed = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
		double slowdownMultiplier =
				0.99D
						/ Math.max(
								FlySpeedModifierIntegration.applyOtherMovementMultiplier(1.0D),
								1.0D);
		double slowdownAmount = FlySpeedModifierIntegration.applyOtherMovementMultiplier(0.06D);
		double newSpeed = Math.max(speed * slowdownMultiplier - slowdownAmount, 0.0D);

		if (speed <= 1.0E-12D || newSpeed == 0.0D) {
			boat.setDeltaMovement(0.0D, movement.y, 0.0D);
		} else {
			double scale = newSpeed / speed;
			boat.setDeltaMovement(movement.x * scale, movement.y, movement.z * scale);
		}

		AbstractBoatAccessor accessor = (AbstractBoatAccessor) boat;
		float deltaRotation = accessor.boatUtils$getDeltaRotation();
		float newDeltaRotation =
				(float)
						Math.copySign(
								Math.max(Math.abs(deltaRotation) * 0.99F - 2.0F, 0.0F),
								deltaRotation);
		if (Math.abs(newDeltaRotation) <= HANDBRAKE_ROTATION_SNAP_EPSILON) {
			newDeltaRotation = 0.0F;
		}
		accessor.boatUtils$setDeltaRotation(newDeltaRotation);
	}

	private static void applyReleaseBoost(AbstractBoat boat, int time) {
		Vec3 movement = boat.getDeltaMovement();

		double boost =
				FlySpeedModifierIntegration.applyOtherMovementMultiplier(calculateDriftBoost(time));

		float yawRadians = boat.getYRot() * Mth.DEG_TO_RAD;
		double forwardX = -Mth.sin(yawRadians);
		double forwardZ = Mth.cos(yawRadians);

		boat.setDeltaMovement(
				movement.x + forwardX * boost, movement.y, movement.z + forwardZ * boost);
	}

	private static double calculateDriftBoost(int time) {
		if (time < 2) {
			return 0.0D;
		}

		double t = time - 2.0D;

		return 2 * (1.0D - Math.exp(-0.02 * t));
	}

	public static void clearHandbrakeStateFor(AbstractBoat boat) {
		if (handbrakeBoat == boat) {
			resetHandbrakeState();
		}
	}

	private static void resetHandbrakeState() {
		handbrakeBoat = null;
		handbrakeTicks = 0;
		handbrakeWasDown = false;
	}

	private static void resetCameraAutoRecenterState() {
		cameraIdleTicks = 0;
	}
}

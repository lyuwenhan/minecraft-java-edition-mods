package com.example.boatutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public final class BoatUtilsMod implements ClientModInitializer {
	public static final String MOD_ID = "boat-utils";

	private static AbstractBoat handbrakeBoat;
	private static int handbrakeTicks;
	private static boolean handbrakeWasDown;

	@Override
	public void onInitializeClient() {
		BoatUtilsConfig.load();
		BoatDirectionHotkeys.initialize();
		ClientTickEvents.START_CLIENT_TICK.register(BoatUtilsMod::applyFollowView);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsMod::applyFollowView);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsMod::applyLateralFriction);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsMod::clearInvalidHandbrakeState);
		ClientTickEvents.END_CLIENT_TICK.register(BoatDirectionHotkeys::handleClientTick);
	}

	private static void applyFollowView(Minecraft client) {
		if (!BoatUtilsConfig.viewDirectionLockEnabled()) {
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
		boat.setYBodyRot(viewYaw);
	}

	private static void applyLateralFriction(Minecraft client) {
		if (!BoatUtilsConfig.lateralFrictionEnabled() || client.player == null) {
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

		boolean handbrakeDown =
				BoatUtilsConfig.handbrakeEnabled() && client.options.keyJump.isDown();
		if (handbrakeDown) {
			if (BoatUtilsConfig.handbrakeBoostEnabled()) {
				handbrakeTicks++;
			}
			handbrakeWasDown = true;
			applyFixedHandbrake(boat);
			return;
		}

		if (handbrakeWasDown
				&& BoatUtilsConfig.handbrakeBoostEnabled()
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
			return;
		}

		double scale = newSpeed / speed;
		boat.setDeltaMovement(movement.x * scale, movement.y, movement.z * scale);
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
}

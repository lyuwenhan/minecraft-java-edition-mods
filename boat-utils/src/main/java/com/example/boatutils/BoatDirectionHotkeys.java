package com.example.boatutils;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

import com.example.boatutils.mixin.AbstractBoatAccessor;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public final class BoatDirectionHotkeys {
	private static final float HALF_TURN_DEGREES = 180.0F;
	private static final float CARDINAL_ANGLE_STEP = 90.0F;
	private static final float DIAGONAL_ANGLE_STEP = 45.0F;

	private static KeyMapping snapToNearest;
	private static KeyMapping turnBack;
	private static KeyMapping turnLeft;
	private static KeyMapping turnRight;
	private static boolean initialized;

	private static boolean snapWasDown;
	private static boolean turnBackWasDown;
	private static boolean turnLeftWasDown;
	private static boolean turnRightWasDown;

	private BoatDirectionHotkeys() {}

	public static void initialize() {
		if (initialized) {
			return;
		}

		KeyMapping.Category category =
				KeyMapping.Category.register(
						Identifier.fromNamespaceAndPath(BoatUtilsMod.MOD_ID, "direction_hotkeys"));

		snapToNearest =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.boat-utils.snap_to_nearest_direction",
								InputConstants.Type.KEYSYM,
								GLFW_KEY_UP,
								category));
		turnBack =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.boat-utils.turn_back",
								InputConstants.Type.KEYSYM,
								GLFW_KEY_DOWN,
								category));
		turnLeft =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.boat-utils.turn_left_to_direction",
								InputConstants.Type.KEYSYM,
								GLFW_KEY_LEFT,
								category));
		turnRight =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.boat-utils.turn_right_to_direction",
								InputConstants.Type.KEYSYM,
								GLFW_KEY_RIGHT,
								category));
		initialized = true;
	}

	public static void handleClientTick(Minecraft client) {
		if (!initialized) {
			return;
		}

		boolean snapDown = snapToNearest.isDown();
		boolean turnBackDown = turnBack.isDown();
		boolean turnLeftDown = turnLeft.isDown();
		boolean turnRightDown = turnRight.isDown();

		if (client.screen == null && BoatUtilsConfig.directionHotkeysEnabled()) {
			AbstractBoat boat = getControlledBoat(client);
			if (boat != null) {
				if (snapDown && !snapWasDown) {
					applyDirection(boat, nearestSuitableAngle(boat.getYRot()), true);
				}
				if (turnBackDown && !turnBackWasDown) {
					applyDirection(
							boat, Mth.wrapDegrees(boat.getYRot() + HALF_TURN_DEGREES), false);
				}
				if (turnLeftDown && !turnLeftWasDown) {
					applyDirection(boat, nextSuitableAngleToLeft(boat.getYRot()), true);
				}
				if (turnRightDown && !turnRightWasDown) {
					applyDirection(boat, nextSuitableAngleToRight(boat.getYRot()), true);
				}
			}
		}

		snapWasDown = snapDown;
		turnBackWasDown = turnBackDown;
		turnLeftWasDown = turnLeftDown;
		turnRightWasDown = turnRightDown;
	}

	private static AbstractBoat getControlledBoat(Minecraft client) {
		if (client.player == null) {
			return null;
		}

		Entity vehicle = client.player.getVehicle();
		if (!(vehicle instanceof AbstractBoat boat)) {
			return null;
		}
		if (boat.getControllingPassenger() != client.player) {
			return null;
		}
		return boat;
	}

	private static float nearestSuitableAngle(float yaw) {
		float step = suitableAngleStep();
		return Mth.wrapDegrees(Math.round(Mth.wrapDegrees(yaw) / step) * step);
	}

	private static float nextSuitableAngleToLeft(float yaw) {
		float step = suitableAngleStep();
		double angleIndex = Mth.wrapDegrees(yaw) / step;
		double targetIndex = Math.floor(Math.nextDown(angleIndex));
		return Mth.wrapDegrees((float) (targetIndex * step));
	}

	private static float nextSuitableAngleToRight(float yaw) {
		float step = suitableAngleStep();
		double angleIndex = Mth.wrapDegrees(yaw) / step;
		double targetIndex = Math.ceil(Math.nextUp(angleIndex));
		return Mth.wrapDegrees((float) (targetIndex * step));
	}

	private static float suitableAngleStep() {
		return BoatUtilsConfig.directionHotkeysUse45DegreeAngles()
				? DIAGONAL_ANGLE_STEP
				: CARDINAL_ANGLE_STEP;
	}

	private static void applyDirection(AbstractBoat boat, float targetYaw, boolean centerInBlock) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}

		if (centerInBlock) {
			double centeredX = Math.floor(boat.getX()) + 0.5D;
			double centeredZ = Math.floor(boat.getZ()) + 0.5D;
			boat.setPos(centeredX, boat.getY(), centeredZ);
		}

		boat.setYRot(targetYaw);
		boat.setYBodyRot(targetYaw);
		client.player.setYRot(targetYaw);
		client.player.setYBodyRot(targetYaw);
		client.player.setYHeadRot(targetYaw);

		boat.setDeltaMovement(0.0D, 0.0D, 0.0D);
		((AbstractBoatAccessor) boat).boatUtils$setDeltaRotation(0.0F);
		BoatUtilsMod.clearHandbrakeStateFor(boat);
	}
}

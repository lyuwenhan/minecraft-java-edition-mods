package com.example.wenhanclient.voidfloor;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class VoidFloorSubMod {
	private static final double OVERWORLD_FLOOR_Y = -105.0D;
	private static final double NETHER_AND_END_FLOOR_Y = -41.0D;
	private static VoidFloorConfig config = new VoidFloorConfig();

	private VoidFloorSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.voidFloor.copy();
	}

	public static VoidFloorConfig config() {
		return config.copy();
	}

	public static void setConfig(VoidFloorConfig newConfig) {
		config = newConfig == null ? new VoidFloorConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.voidFloor = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static Vec3 restrictDownwardMovement(LocalPlayer player, Vec3 movement) {
		if (!config.enabled || !player.getAbilities().mayfly || movement.y >= 0.0D) {
			return movement;
		}

		double floorY;
		if (player.level().dimension() == Level.OVERWORLD) {
			floorY = OVERWORLD_FLOOR_Y;
		} else if (player.level().dimension() == Level.NETHER
				|| player.level().dimension() == Level.END) {
			floorY = NETHER_AND_END_FLOOR_Y;
		} else {
			return movement;
		}

		double feetY = player.getBoundingBox().minY;
		if (feetY < floorY || feetY + movement.y > floorY) {
			return movement;
		}
		return new Vec3(movement.x, floorY - feetY, movement.z);
	}
}

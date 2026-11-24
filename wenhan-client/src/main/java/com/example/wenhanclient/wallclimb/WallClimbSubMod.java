package com.example.wenhanclient.wallclimb;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class WallClimbSubMod {
	private static final double WALL_REACH = 0.05D;
	private static final double VERTICAL_INSET = 1.0E-4D;
	private static WallClimbConfig config = new WallClimbConfig();

	private WallClimbSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.wallClimb.copy();
	}

	public static WallClimbConfig config() {
		return config.copy();
	}

	public static void setConfig(WallClimbConfig newConfig) {
		config = newConfig == null ? new WallClimbConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.wallClimb = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean canClimbWall(LivingEntity entity) {
		return config.enabled
				&& entity instanceof LocalPlayer player
				&& !player.isFallFlying()
				&& (player.horizontalCollision || hasWallBeside(player));
	}

	private static boolean hasWallBeside(LocalPlayer player) {
		AABB bounds = player.getBoundingBox();
		AABB probe =
				new AABB(
						bounds.minX - WALL_REACH,
						bounds.minY + VERTICAL_INSET,
						bounds.minZ - WALL_REACH,
						bounds.maxX + WALL_REACH,
						bounds.maxY + WALL_REACH,
						bounds.maxZ + WALL_REACH);
		return player.level().getBlockCollisions(player, probe).iterator().hasNext();
	}
}

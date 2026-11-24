package com.example.wenhanclient.blockexplosionguard;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockExplosionGuardSubMod {
	private static BlockExplosionGuardConfig config = new BlockExplosionGuardConfig();

	private BlockExplosionGuardSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.blockExplosionGuard.copy();
	}

	public static BlockExplosionGuardConfig config() {
		return config.copy();
	}

	public static void setConfig(BlockExplosionGuardConfig newConfig) {
		config = newConfig == null ? new BlockExplosionGuardConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.blockExplosionGuard = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static boolean shouldBlockUse(Level level, BlockPos pos) {
		if (!config.enabled || level == null || pos == null) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof AbstractBedBlock) {
			return level.dimension() != Level.OVERWORLD;
		}
		if (state.getBlock() instanceof RespawnAnchorBlock) {
			return level.dimension() != Level.NETHER;
		}
		return false;
	}
}

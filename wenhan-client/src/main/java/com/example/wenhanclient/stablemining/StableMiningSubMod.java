package com.example.wenhanclient.stablemining;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class StableMiningSubMod {
	private static StableMiningConfig config = new StableMiningConfig();
	private static BlockPos miningBlock;

	private StableMiningSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.stableMining.copy();
	}

	public static StableMiningConfig config() {
		return config.copy();
	}

	public static void setConfig(StableMiningConfig newConfig) {
		config = newConfig == null ? new StableMiningConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.stableMining = config.copy();
		WenhanClientMod.CONFIG.save();
		if (!config.enabled) {
			clearMiningBlock();
		}
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static void recordMiningBlock(BlockPos pos) {
		if (!isEnabled()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (canInstantlyMine(client, pos)) {
			clearMiningBlock();
			return;
		}
		miningBlock = pos == null ? null : pos.immutable();
	}

	public static void clearMiningBlock() {
		miningBlock = null;
	}

	public static BlockHitResult getMiningBlockHitResult(Minecraft client) {
		if (!hasValidMiningBlock(client)) {
			return null;
		}

		Vec3 eye = client.player.getEyePosition();
		Vec3 center = Vec3.atCenterOf(miningBlock);
		Direction direction = Direction.getApproximateNearest(eye.subtract(center));
		return new BlockHitResult(center, direction, miningBlock, false);
	}

	public static boolean shouldCancelMouseMovement(Minecraft client) {
		return hasValidMiningBlock(client);
	}

	public static void aimAtMiningBlock(Minecraft client) {
		if (!hasValidMiningBlock(client)) {
			return;
		}

		Vec3 eye = client.player.getEyePosition();
		Vec3 center = Vec3.atCenterOf(miningBlock);
		Vec3 delta = center.subtract(eye);
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

	private static boolean hasValidMiningBlock(Minecraft client) {
		if (!isEnabled()
				|| miningBlock == null
				|| client.level == null
				|| client.player == null
				|| client.gui.screen() != null
				|| !client.options.keyAttack.isDown()) {
			clearMiningBlock();
			return false;
		}

		if (client.level.getBlockState(miningBlock).isAir()
				|| isBeyondMiningDistance(client, Vec3.atCenterOf(miningBlock))) {
			clearMiningBlock();
			return false;
		}
		return true;
	}

	private static boolean canInstantlyMine(Minecraft client, BlockPos pos) {
		if (client.level == null || client.player == null || pos == null) {
			return false;
		}
		if (hasInstantBuild(client)) {
			return true;
		}

		BlockState state = client.level.getBlockState(pos);
		return !state.isAir() && state.getDestroyProgress(client.player, client.level, pos) >= 1.0F;
	}

	private static boolean hasInstantBuild(Minecraft client) {
		GameType gameType = client.gameMode == null ? null : client.gameMode.getPlayerMode();
		return (gameType != null && gameType.isCreative())
				|| client.player.getAbilities().instabuild;
	}

	private static boolean isBeyondMiningDistance(Minecraft client, Vec3 center) {
		double reach = client.player.blockInteractionRange();
		Vec3 eye = client.player.getEyePosition();
		double closestX = Mth.clamp(eye.x, miningBlock.getX(), miningBlock.getX() + 1.0D);
		double closestY = Mth.clamp(eye.y, miningBlock.getY(), miningBlock.getY() + 1.0D);
		double closestZ = Mth.clamp(eye.z, miningBlock.getZ(), miningBlock.getZ() + 1.0D);
		return eye.distanceToSqr(closestX, closestY, closestZ) > reach * reach;
	}
}

package com.example.wenhanclient.scaffoldplacement;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.placementrestriction.PlacementRestrictionSubMod;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public final class ScaffoldPlacementSubMod {
	private static final Direction[] HORIZONTAL_DIRECTIONS = {
		Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};

	private static ScaffoldPlacementConfig config = new ScaffoldPlacementConfig();
	private static BlockPos focusedPos;

	private ScaffoldPlacementSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.scaffoldPlacement.copy();
		LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(
				(context, hitResult) -> {
					BlockPos currentFocus = focusedPos;
					if (currentFocus != null
							&& hitResult instanceof BlockHitResult blockHit
							&& currentFocus.equals(blockHit.getBlockPos())) {
						context.levelState().blockOutlineRenderState =
								new BlockOutlineRenderState(
										currentFocus, false, false, Shapes.block());
					}
				});
	}

	public static ScaffoldPlacementConfig config() {
		return config.copy();
	}

	public static void setConfig(ScaffoldPlacementConfig newConfig) {
		config = newConfig == null ? new ScaffoldPlacementConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.scaffoldPlacement = config.copy();
		WenhanClientMod.CONFIG.save();
		if (!config.enabled && !config.liquidSurfacePlacement) {
			focusedPos = null;
		}
	}

	public static BlockHitResult getCrosshairTarget(Minecraft client) {
		focusedPos = null;
		if ((!config.enabled && !config.liquidSurfacePlacement)
				|| client == null
				|| client.level == null
				|| client.player == null
				|| client.gui.screen() != null
				|| (client.hitResult != null
						&& client.hitResult.getType() == HitResult.Type.BLOCK)) {
			return null;
		}

		InteractionHand hand = blockItemHand(client.player);
		if (hand == null) {
			return null;
		}

		Vec3 eye = client.player.getEyePosition();
		Vec3 end =
				eye.add(
						client.player
								.getViewVector(1.0F)
								.scale(client.player.blockInteractionRange()));
		BlockPos target =
				BlockGetter.traverseBlocks(
						eye,
						end,
						client,
						(ignored, pos) ->
								isVirtualTarget(client, hand, pos) ? pos.immutable() : null,
						ignored -> null);
		if (target == null) {
			return null;
		}

		AABB bounds = new AABB(target);
		Vec3 location = bounds.clip(eye, end).orElseGet(() -> Vec3.atCenterOf(target));
		Direction face = Direction.getApproximateNearest(eye.subtract(location));
		focusedPos = target;
		return new BlockHitResult(location, face, target, false);
	}

	private static boolean isVirtualTarget(Minecraft client, InteractionHand hand, BlockPos pos) {
		BlockState state = client.level.getBlockState(pos);
		if (!state.canBeReplaced()
				|| client.player.getBoundingBox().intersects(new AABB(pos))
				|| !isEnabledTarget(client, pos, state)
				|| !PlacementRestrictionSubMod.canPlaceAt(pos)
				|| !client.player.isWithinBlockInteractionRange(pos, 0.0D)) {
			return false;
		}

		ItemStack stack = client.player.getItemInHand(hand);
		Vec3 center = Vec3.atCenterOf(pos);
		Direction face =
				Direction.getApproximateNearest(client.player.getEyePosition().subtract(center));
		BlockPlaceContext context =
				new BlockPlaceContext(
						client.player, hand, stack, new BlockHitResult(center, face, pos, false));
		return context.canPlace();
	}

	private static boolean isEnabledTarget(Minecraft client, BlockPos pos, BlockState state) {
		return (config.enabled && hasSelectableHorizontalNeighbor(client, pos))
				|| (config.liquidSurfacePlacement && isAtAirLiquidBoundary(client, pos, state));
	}

	private static boolean hasSelectableHorizontalNeighbor(Minecraft client, BlockPos pos) {
		for (Direction direction : HORIZONTAL_DIRECTIONS) {
			BlockPos neighbor = pos.relative(direction);
			BlockState state = client.level.getBlockState(neighbor);
			if (!state.getShape(client.level, neighbor).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isAtAirLiquidBoundary(Minecraft client, BlockPos pos, BlockState state) {
		boolean targetIsAir = state.isAir();
		FluidState targetFluid = state.getFluidState();
		boolean targetIsLiquid = !targetFluid.isEmpty();
		if (!targetIsAir && !targetIsLiquid) {
			return false;
		}

		if (targetIsLiquid && !isPlayerInSameLiquid(client.player, targetFluid)) {
			return false;
		}

		for (Direction direction : Direction.values()) {
			BlockState neighbor = client.level.getBlockState(pos.relative(direction));
			FluidState neighborFluid = neighbor.getFluidState();
			if ((targetIsAir
							&& !neighborFluid.isEmpty()
							&& !isPlayerInSameLiquid(client.player, neighborFluid))
					|| (targetIsLiquid && neighbor.isAir())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isPlayerInSameLiquid(LocalPlayer player, FluidState fluid) {
		if (fluid.is(FluidTags.WATER)) {
			return player.isInWater();
		}
		if (fluid.is(FluidTags.LAVA)) {
			return player.isInLava();
		}
		return false;
	}

	private static InteractionHand blockItemHand(LocalPlayer player) {
		if (player.getMainHandItem().getItem() instanceof BlockItem) {
			return InteractionHand.MAIN_HAND;
		}
		if (player.getOffhandItem().getItem() instanceof BlockItem) {
			return InteractionHand.OFF_HAND;
		}
		return null;
	}
}

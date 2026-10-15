package com.example.wenhanclient.placementrestriction;

import com.example.wenhanclient.WenhanClientMod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public final class PlacementRestrictionSubMod {
	private enum PlacementAxis {
		UNSET,
		X,
		Y,
		Z
	}

	private static PlacementRestrictionConfig config = new PlacementRestrictionConfig();
	private static BlockPos anchor;
	private static PlacementAxis axis = PlacementAxis.UNSET;
	private static BlockPos focusedPos;

	private PlacementRestrictionSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.placementRestriction.copy();
		ClientTickEvents.END_CLIENT_TICK.register(PlacementRestrictionSubMod::onEndClientTick);
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

	public static PlacementRestrictionConfig config() {
		return config.copy();
	}

	public static void setConfig(PlacementRestrictionConfig newConfig) {
		PlacementRestrictionMode previousMode = config.mode;
		config = newConfig == null ? new PlacementRestrictionConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.placementRestriction = config.copy();
		WenhanClientMod.CONFIG.save();
		if (config.mode != previousMode) {
			resetSession();
		}
	}

	public static boolean isEnabled() {
		return config.mode != PlacementRestrictionMode.DISABLED;
	}

	public static BlockPos getPlacementPos(
			LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
		if (player == null || hand == null || hitResult == null) {
			return null;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof BlockItem)) {
			return null;
		}
		return new BlockPlaceContext(player, hand, stack, hitResult).getClickedPos().immutable();
	}

	public static boolean canPlaceAt(BlockPos pos) {
		if (!isEnabled() || anchor == null || pos == null) {
			return true;
		}

		return switch (config.mode) {
			case DISABLED -> true;
			case HORIZONTAL_PLANE -> pos.getY() == anchor.getY();
			case STRAIGHT_LINE -> isOnStraightLine(pos);
			case VERTICAL_PLANE -> isOnVerticalPlane(pos);
		};
	}

	public static void recordPlacement(BlockPos pos) {
		if (!isEnabled() || pos == null) {
			return;
		}
		if (anchor == null) {
			anchor = pos.immutable();
			axis = PlacementAxis.UNSET;
			return;
		}

		if (axis != PlacementAxis.UNSET) {
			return;
		}
		if (config.mode == PlacementRestrictionMode.STRAIGHT_LINE) {
			if (pos.getX() != anchor.getX()
					&& pos.getY() == anchor.getY()
					&& pos.getZ() == anchor.getZ()) {
				axis = PlacementAxis.X;
			} else if (pos.getY() != anchor.getY()
					&& pos.getX() == anchor.getX()
					&& pos.getZ() == anchor.getZ()) {
				axis = PlacementAxis.Y;
			} else if (pos.getZ() != anchor.getZ()
					&& pos.getX() == anchor.getX()
					&& pos.getY() == anchor.getY()) {
				axis = PlacementAxis.Z;
			}
		} else if (config.mode == PlacementRestrictionMode.VERTICAL_PLANE) {
			if (pos.getX() != anchor.getX() && pos.getZ() == anchor.getZ()) {
				axis = PlacementAxis.X;
			} else if (pos.getZ() != anchor.getZ() && pos.getX() == anchor.getX()) {
				axis = PlacementAxis.Z;
			}
		}
	}

	public static boolean shouldOverrideCrosshair(Minecraft client) {
		return hasActiveSession(client) && anchor != null && blockItemHand(client.player) != null;
	}

	public static BlockHitResult adjustFocus(Minecraft client) {
		focusedPos = null;
		if (!shouldOverrideCrosshair(client)) {
			return null;
		}
		InteractionHand hand = blockItemHand(client.player);

		Vec3 eye = client.player.getEyePosition();
		Vec3 view = client.player.getViewVector(1.0F);
		double reach = client.player.blockInteractionRange();
		Vec3 maximumEnd = eye.add(view.scale(reach));
		BlockHitResult blockHit =
				client.level.clip(
						new ClipContext(
								eye,
								maximumEnd,
								ClipContext.Block.OUTLINE,
								ClipContext.Fluid.NONE,
								client.player));
		Vec3 end = maximumEnd;
		if (blockHit.getType() != HitResult.Type.MISS) {
			if (canFocusHit(client, hand, blockHit)) {
				return blockHit;
			}
			end = blockHit.getLocation();
		}
		Vec3 rayEnd = end;
		BlockPos focused =
				BlockGetter.traverseBlocks(
						eye,
						rayEnd,
						client,
						(ignored, pos) ->
								isFocusableAirPosition(client, hand, eye, rayEnd, pos)
										? pos.immutable()
										: null,
						ignored -> null);
		if (focused == null) {
			Direction missDirection = Direction.getApproximateNearest(view);
			return BlockHitResult.miss(end, missDirection, BlockPos.containing(end));
		}
		focusedPos = focused;

		AABB bounds = new AABB(focused);
		Vec3 location = bounds.clip(eye, end).orElseGet(() -> Vec3.atCenterOf(focused));
		Direction face = Direction.getApproximateNearest(eye.subtract(location));
		return new BlockHitResult(location, face, focused, false);
	}

	public static void resetSession() {
		anchor = null;
		axis = PlacementAxis.UNSET;
		focusedPos = null;
	}

	private static void onEndClientTick(Minecraft client) {
		if (!hasActiveSession(client)) {
			resetSession();
		}
	}

	private static boolean hasActiveSession(Minecraft client) {
		return isEnabled()
				&& client != null
				&& client.level != null
				&& client.player != null
				&& client.gui.screen() == null
				&& client.options.keyUse.isDown();
	}

	private static boolean isOnStraightLine(BlockPos pos) {
		return switch (axis) {
			case UNSET ->
					(pos.getY() == anchor.getY() && pos.getZ() == anchor.getZ())
							|| (pos.getX() == anchor.getX() && pos.getZ() == anchor.getZ())
							|| (pos.getX() == anchor.getX() && pos.getY() == anchor.getY());
			case X -> pos.getY() == anchor.getY() && pos.getZ() == anchor.getZ();
			case Y -> pos.getX() == anchor.getX() && pos.getZ() == anchor.getZ();
			case Z -> pos.getX() == anchor.getX() && pos.getY() == anchor.getY();
		};
	}

	private static boolean isOnVerticalPlane(BlockPos pos) {
		return switch (axis) {
			case UNSET -> pos.getX() == anchor.getX() || pos.getZ() == anchor.getZ();
			case X -> pos.getZ() == anchor.getZ();
			case Y -> pos.getX() == anchor.getX() || pos.getZ() == anchor.getZ();
			case Z -> pos.getX() == anchor.getX();
		};
	}

	private static boolean isFocusableAirPosition(
			Minecraft client, InteractionHand hand, Vec3 eye, Vec3 end, BlockPos pos) {
		if (!client.level.getBlockState(pos).canBeReplaced()
				|| client.player.getBoundingBox().intersects(pos)) {
			return false;
		}
		AABB bounds = new AABB(pos);
		Vec3 location = bounds.clip(eye, end).orElseGet(() -> Vec3.atCenterOf(pos));
		Direction face = Direction.getApproximateNearest(eye.subtract(location));
		return canFocusHit(client, hand, new BlockHitResult(location, face, pos, false));
	}

	private static boolean canFocusHit(
			Minecraft client, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack stack = client.player.getItemInHand(hand);
		BlockPlaceContext context = new BlockPlaceContext(client.player, hand, stack, hitResult);
		BlockPos placementPos = context.getClickedPos();
		return context.canPlace()
				&& canPlaceAt(placementPos)
				&& client.player.isWithinBlockInteractionRange(placementPos, 0.0D);
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

package com.example.wenhanclient.placementrestriction.mixin;

import com.example.wenhanclient.placementrestriction.PlacementRestrictionSubMod;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Unique private BlockPos placementRestriction$pendingPos;
	@Unique private BlockState placementRestriction$previousState;

	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void placementRestriction$validatePlacement(
			LocalPlayer player,
			InteractionHand hand,
			BlockHitResult hitResult,
			CallbackInfoReturnable<InteractionResult> callbackInfo) {
		this.placementRestriction$pendingPos = null;
		this.placementRestriction$previousState = null;
		if (!PlacementRestrictionSubMod.isEnabled()
				|| !(player.getItemInHand(hand).getItem() instanceof BlockItem blockItem)) {
			return;
		}

		BlockPos placementPos = PlacementRestrictionSubMod.getPlacementPos(player, hand, hitResult);
		if (!PlacementRestrictionSubMod.canPlaceAt(placementPos)) {
			callbackInfo.setReturnValue(InteractionResult.FAIL);
			return;
		}

		this.placementRestriction$pendingPos = placementPos;
		this.placementRestriction$previousState = player.level().getBlockState(placementPos);
	}

	@Inject(method = "useItemOn", at = @At("RETURN"))
	private void placementRestriction$recordPlacement(
			LocalPlayer player,
			InteractionHand hand,
			BlockHitResult hitResult,
			CallbackInfoReturnable<InteractionResult> callbackInfo) {
		BlockPos placementPos = this.placementRestriction$pendingPos;
		BlockState previousState = this.placementRestriction$previousState;
		this.placementRestriction$pendingPos = null;
		this.placementRestriction$previousState = null;
		if (placementPos != null
				&& previousState != null
				&& callbackInfo.getReturnValue().consumesAction()
				&& !player.level().getBlockState(placementPos).equals(previousState)) {
			PlacementRestrictionSubMod.recordPlacement(placementPos);
		}
	}
}

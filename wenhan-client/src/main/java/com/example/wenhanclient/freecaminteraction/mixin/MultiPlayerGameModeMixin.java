package com.example.wenhanclient.freecaminteraction.mixin;

import com.example.wenhanclient.freecaminteraction.FreecamInteractionSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Inject(
			method = {"startDestroyBlock", "continueDestroyBlock"},
			at = @At("HEAD"))
	private void freecamInteraction$mine(
			BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> ci) {
		FreecamInteractionSubMod.faceBlock(pos);
	}

	@Inject(method = "destroyBlock", at = @At("HEAD"))
	private void freecamInteraction$destroy(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		FreecamInteractionSubMod.faceBlock(pos);
	}

	@Inject(method = "useItemOn", at = @At("HEAD"))
	private void freecamInteraction$useBlock(
			LocalPlayer player,
			InteractionHand hand,
			BlockHitResult hit,
			CallbackInfoReturnable<InteractionResult> ci) {
		if (player == Minecraft.getInstance().player) {
			FreecamInteractionSubMod.face(hit.getLocation());
		}
	}

	@Inject(method = "attack", at = @At("HEAD"))
	private void freecamInteraction$attackEntity(Player player, Entity target, CallbackInfo ci) {
		if (player == Minecraft.getInstance().player && target != player) {
			FreecamInteractionSubMod.face(target.getBoundingBox().getCenter());
		}
	}

	@Inject(method = "interact", at = @At("HEAD"))
	private void freecamInteraction$useEntity(
			Player player,
			Entity target,
			EntityHitResult hit,
			InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> ci) {
		if (player == Minecraft.getInstance().player && target != player) {
			FreecamInteractionSubMod.face(hit.getLocation());
		}
	}

	@Inject(method = "useItem", at = @At("HEAD"))
	private void freecamInteraction$useItem(
			Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
		if (player == Minecraft.getInstance().player) {
			FreecamInteractionSubMod.faceCrosshair();
		}
	}

	@Inject(method = "releaseUsingItem", at = @At("HEAD"))
	private void freecamInteraction$releaseItem(Player player, CallbackInfo ci) {
		if (player == Minecraft.getInstance().player) {
			FreecamInteractionSubMod.faceCrosshair();
		}
	}

	@Inject(method = "piercingAttack", at = @At("HEAD"))
	private void freecamInteraction$piercingAttack(
			SwingAnimation animation, PiercingWeapon weapon, CallbackInfo ci) {
		FreecamInteractionSubMod.faceCrosshair();
	}

	@Inject(method = "handlePickItemFromBlock", at = @At("HEAD"))
	private void freecamInteraction$pickBlock(BlockPos pos, boolean includeData, CallbackInfo ci) {
		FreecamInteractionSubMod.faceBlock(pos);
	}

	@Inject(method = "handlePickItemFromEntity", at = @At("HEAD"))
	private void freecamInteraction$pickEntity(
			Entity target, boolean includeData, CallbackInfo ci) {
		FreecamInteractionSubMod.face(target.getBoundingBox().getCenter());
	}

	@Inject(method = "dropItem", at = @At("HEAD"))
	private void freecamInteraction$dropItem(
			LocalPlayer player, boolean entireStack, CallbackInfo ci) {
		if (player == Minecraft.getInstance().player) {
			FreecamInteractionSubMod.faceCrosshair();
		}
	}

	@Inject(method = "handleCreativeModeItemDrop", at = @At("HEAD"))
	private void freecamInteraction$creativeDrop(ItemStack stack, CallbackInfo ci) {
		FreecamInteractionSubMod.faceCrosshair();
	}
}

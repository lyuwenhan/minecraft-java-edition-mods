package com.example.wenhanclient.blockexplosionguard.mixin;

import com.example.wenhanclient.blockexplosionguard.BlockExplosionGuardSubMod;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void blockExplosionGuard$blockUnsafeExplosiveUse(
			LocalPlayer player,
			InteractionHand hand,
			BlockHitResult hitResult,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (player != null
				&& hitResult != null
				&& BlockExplosionGuardSubMod.shouldBlockUse(
						player.level(), hitResult.getBlockPos())) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}

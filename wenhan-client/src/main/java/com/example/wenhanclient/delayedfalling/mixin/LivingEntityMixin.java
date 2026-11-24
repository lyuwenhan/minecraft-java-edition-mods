package com.example.wenhanclient.delayedfalling.mixin;

import com.example.wenhanclient.delayedfalling.DelayedFallingSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 1100)
public abstract class LivingEntityMixin {
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void delayedFalling$beginAiStep(CallbackInfo callbackInfo) {
		if ((Object) this instanceof LocalPlayer player) {
			DelayedFallingSubMod.beginAiStep(player);
		}
	}

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void delayedFalling$endAiStep(CallbackInfo callbackInfo) {
		if ((Object) this instanceof LocalPlayer player) {
			DelayedFallingSubMod.endAiStep(player);
		}
	}

	@Inject(method = "getEffectiveGravity", at = @At("HEAD"), cancellable = true)
	private void delayedFalling$removeGravity(CallbackInfoReturnable<Double> callbackInfo) {
		if ((Object) this instanceof LocalPlayer player
				&& DelayedFallingSubMod.isDelaying(player)) {
			callbackInfo.setReturnValue(0.0D);
		}
	}
}

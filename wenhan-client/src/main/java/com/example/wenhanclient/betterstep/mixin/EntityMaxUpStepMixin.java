package com.example.wenhanclient.betterstep.mixin;

import com.example.wenhanclient.betterstep.BetterStepSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class EntityMaxUpStepMixin {
	@Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
	private void betterstep$applyPlayerStepHeight(CallbackInfoReturnable<Float> callbackInfo) {
		if (!((Object) this instanceof Player)) {
			return;
		}
		if ((Object) this != Minecraft.getInstance().player) {
			return;
		}

		float stepHeight = (float) BetterStepSubMod.stepHeight();
		if (stepHeight < 0.0F) {
			return;
		}

		callbackInfo.setReturnValue(stepHeight);
	}
}

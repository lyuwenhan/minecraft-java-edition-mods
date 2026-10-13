package com.example.wenhanclient.jumpheight.mixin;

import com.example.wenhanclient.jumpheight.JumpHeightSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "getJumpPower(F)F", at = @At("RETURN"), cancellable = true)
	private void jumpheight$applyConfiguredJumpStrength(
			float jumpFactor, CallbackInfoReturnable<Float> cir) {
		if ((Object) this instanceof LocalPlayer player && JumpHeightSubMod.isEnabled()) {
			cir.setReturnValue(JumpHeightSubMod.getJumpStrength(player));
		}
	}
}

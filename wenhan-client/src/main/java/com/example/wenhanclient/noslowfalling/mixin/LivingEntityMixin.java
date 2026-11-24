package com.example.wenhanclient.noslowfalling.mixin;

import com.example.wenhanclient.noslowfalling.NoSlowFallingSubMod;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void noSlowFalling$hideSlowFallingPresence(
			Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		if (NoSlowFallingSubMod.shouldCancel((LivingEntity) (Object) this, effect)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getEffect", at = @At("HEAD"), cancellable = true)
	private void noSlowFalling$hideSlowFallingInstance(
			Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
		if (NoSlowFallingSubMod.shouldCancel((LivingEntity) (Object) this, effect)) {
			cir.setReturnValue(null);
		}
	}
}

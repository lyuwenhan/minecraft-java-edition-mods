package com.example.wenhanclient.nolevitation.mixin;

import com.example.wenhanclient.nolevitation.NoLevitationSubMod;

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
	private void noLevitation$hideLevitationPresence(
			Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		if (NoLevitationSubMod.shouldCancel((LivingEntity) (Object) this, effect)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getEffect", at = @At("HEAD"), cancellable = true)
	private void noLevitation$hideLevitationInstance(
			Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
		if (NoLevitationSubMod.shouldCancel((LivingEntity) (Object) this, effect)) {
			cir.setReturnValue(null);
		}
	}
}

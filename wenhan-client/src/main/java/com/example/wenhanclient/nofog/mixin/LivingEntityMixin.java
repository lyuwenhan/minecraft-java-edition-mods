package com.example.wenhanclient.nofog.mixin;

import com.example.wenhanclient.nofog.NoFogSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void noFog$hideLocalScreenEffectPresence(
			Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		if (shouldHideEffect(effect)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getEffect", at = @At("HEAD"), cancellable = true)
	private void noFog$hideLocalScreenEffectInstance(
			Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
		if (shouldHideEffect(effect)) {
			cir.setReturnValue(null);
		}
	}

	private boolean shouldHideEffect(Holder<MobEffect> effect) {
		return NoFogSubMod.isEnabled()
				&& isLocalPlayer()
				&& (effect == MobEffects.BLINDNESS
						|| effect == MobEffects.DARKNESS
						|| effect == MobEffects.NAUSEA);
	}

	private boolean isLocalPlayer() {
		return (Object) this == Minecraft.getInstance().player;
	}
}

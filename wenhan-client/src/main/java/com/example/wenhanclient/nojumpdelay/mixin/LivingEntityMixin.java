package com.example.wenhanclient.nojumpdelay.mixin;

import com.example.wenhanclient.nojumpdelay.NoJumpDelaySubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow private int noJumpDelay;

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void noJumpDelay$clearLocalPlayerJumpDelay(CallbackInfo ci) {
		if ((Object) this instanceof LocalPlayer && NoJumpDelaySubMod.isEnabled()) {
			this.noJumpDelay = 0;
		}
	}
}

package com.example.forwardlock.mixin;

import com.example.forwardlock.ForwardLockClient;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityMixin {
	@Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
	private void forwardLock$preventSprintStop(boolean sprinting, CallbackInfo ci) {
		if (!sprinting
				&& (Object) this instanceof LocalPlayer player
				&& ForwardLockClient.shouldKeepSprinting(player)) {
			ci.cancel();
		}
	}
}

package com.example.wenhanclient.flightdisabler.mixin;

import com.example.wenhanclient.flightdisabler.FlightDisablerSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFallFlyingMixin {
	@Inject(method = "travelFallFlying", at = @At("HEAD"), cancellable = true)
	private void flightDisabler$cancelLocalElytraTravel(CallbackInfo ci) {
		if (!FlightDisablerSubMod.isEnabled()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || (Object) this != client.player) {
			return;
		}
		FlightDisablerSubMod.disableFlight(client);
		ci.cancel();
	}
}

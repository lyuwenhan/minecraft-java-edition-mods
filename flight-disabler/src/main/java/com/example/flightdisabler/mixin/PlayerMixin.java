package com.example.flightdisabler.mixin;

import com.example.flightdisabler.FlightDisablerMod;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

	@Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
	private void flightdisabler$disableStartFallFlying(CallbackInfoReturnable<Boolean> cir) {
		if (!FlightDisablerMod.isEnabled()) {
			return;
		}

		cir.setReturnValue(false);
	}
}

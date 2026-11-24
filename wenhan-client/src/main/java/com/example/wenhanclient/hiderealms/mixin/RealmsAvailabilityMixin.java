package com.example.wenhanclient.hiderealms.mixin;

import com.example.wenhanclient.hiderealms.HideRealmsSubMod;
import com.mojang.realmsclient.RealmsAvailability;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(RealmsAvailability.class)
public abstract class RealmsAvailabilityMixin {
	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private static void hideRealms$skipAvailabilityCheck(
			CallbackInfoReturnable<CompletableFuture<RealmsAvailability.Result>> cir) {
		if (HideRealmsSubMod.isEnabled()) {
			cir.setReturnValue(
					CompletableFuture.completedFuture(
							new RealmsAvailability.Result(
									RealmsAvailability.Type.AUTHENTICATION_ERROR)));
		}
	}
}

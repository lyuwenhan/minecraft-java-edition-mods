package com.example.wenhanclient.hiderealms.mixin;

import com.example.wenhanclient.hiderealms.HideRealmsSubMod;
import com.mojang.realmsclient.client.RealmsClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(RealmsClient.class)
public abstract class RealmsClientMixin {
	@Inject(method = "getFeatureFlags", at = @At("HEAD"), cancellable = true)
	private void hideRealms$getFeatureFlags(CallbackInfoReturnable<Set<String>> cir) {
		if (HideRealmsSubMod.isEnabled()) {
			cir.setReturnValue(Set.of());
		}
	}

	@Inject(method = "fetchFeatureFlags", at = @At("HEAD"), cancellable = true)
	private void hideRealms$fetchFeatureFlags(CallbackInfoReturnable<Set<String>> cir) {
		if (HideRealmsSubMod.isEnabled()) {
			cir.setReturnValue(Set.of());
		}
	}
}

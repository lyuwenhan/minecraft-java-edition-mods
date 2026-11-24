package com.example.wenhanclient.nocameracollision.mixin;

import com.example.wenhanclient.nocameracollision.NoCameraCollisionSubMod;

import net.minecraft.client.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
	private void noCameraCollision$keepRequestedCameraDistance(
			float desiredCameraDistance, CallbackInfoReturnable<Float> cir) {
		if (NoCameraCollisionSubMod.isEnabled()) {
			cir.setReturnValue(desiredCameraDistance);
		}
	}
}

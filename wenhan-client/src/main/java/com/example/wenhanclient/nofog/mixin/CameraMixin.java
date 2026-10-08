package com.example.wenhanclient.nofog.mixin;

import com.example.wenhanclient.nofog.NoFogSubMod;

import net.minecraft.client.Camera;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Inject(method = "modifyFovBasedOnDeathOrFluid", at = @At("HEAD"), cancellable = true)
	private void noFog$removeFluidFovDistortion(
			float partialTick, float fov, CallbackInfoReturnable<Float> cir) {
		if (NoFogSubMod.isEnabled()) {
			cir.setReturnValue(fov);
		}
	}
}

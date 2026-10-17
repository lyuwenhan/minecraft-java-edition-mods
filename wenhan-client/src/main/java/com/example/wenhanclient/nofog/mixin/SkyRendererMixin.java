package com.example.wenhanclient.nofog.mixin;

import com.example.wenhanclient.nofog.NoFogSubMod;
import com.mojang.renderpearl.api.commands.RenderPass;

import net.minecraft.client.renderer.SkyRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
	private void noFog$removeVoidMask(RenderPass renderPass, CallbackInfo ci) {
		if (NoFogSubMod.isEnabled()) {
			ci.cancel();
		}
	}
}

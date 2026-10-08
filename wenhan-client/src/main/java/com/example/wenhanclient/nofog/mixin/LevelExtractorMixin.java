package com.example.wenhanclient.nofog.mixin;

import com.example.wenhanclient.nofog.NoFogSubMod;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.PlayerRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
	@Inject(method = "extractPlayerState", at = @At("RETURN"))
	private void noFog$removePortalAndNauseaDistortion(
			Camera camera,
			DeltaTracker deltaTracker,
			float partialTick,
			PlayerRenderState playerRenderState,
			CallbackInfo ci) {
		if (NoFogSubMod.isEnabled()) {
			playerRenderState.portalEffectIntensity = 0.0F;
			playerRenderState.nauseaEffectIntensity = 0.0F;
		}
	}
}

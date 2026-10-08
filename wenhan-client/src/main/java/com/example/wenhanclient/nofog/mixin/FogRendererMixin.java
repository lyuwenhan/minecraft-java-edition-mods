package com.example.wenhanclient.nofog.mixin;

import com.example.wenhanclient.nofog.NoFogSubMod;

import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
	private static final float NO_FOG_START = 1_000_000.0F;
	private static final float NO_FOG_END = 1_000_001.0F;

	@Inject(method = "setupFog", at = @At("RETURN"))
	private void noFog$removeFogDistances(CallbackInfoReturnable<FogData> cir) {
		if (!NoFogSubMod.isEnabled()) {
			return;
		}

		disableFog(cir.getReturnValue());
	}

	@ModifyVariable(method = "getBuffer", at = @At("HEAD"), argsOnly = true)
	private FogRenderer.FogMode noFog$useEmptyFogBuffer(FogRenderer.FogMode mode) {
		if (!NoFogSubMod.isEnabled()) {
			return mode;
		}

		return FogRenderer.FogMode.NONE;
	}

	private static void disableFog(FogData data) {
		data.environmentalStart = NO_FOG_START;
		data.environmentalEnd = NO_FOG_END;
		data.renderDistanceStart = NO_FOG_START;
		data.renderDistanceEnd = NO_FOG_END;
		data.skyEnd = NO_FOG_END;
		data.cloudEnd = NO_FOG_END;
	}
}

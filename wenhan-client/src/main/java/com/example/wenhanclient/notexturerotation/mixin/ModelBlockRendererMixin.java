package com.example.wenhanclient.notexturerotation.mixin;

import com.example.wenhanclient.notexturerotation.NoTextureRotationSubMod;

import net.minecraft.client.renderer.block.ModelBlockRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
	private static final long STABLE_MODEL_RANDOM_SEED = 0L;

	@ModifyArg(
			method = "tesselateBlock",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;setSeed(J)V"),
			index = 0)
	private long noTextureRotation$useStableModelSeed(long seed) {
		if (!NoTextureRotationSubMod.isEnabled()) {
			return seed;
		}

		return STABLE_MODEL_RANDOM_SEED;
	}
}

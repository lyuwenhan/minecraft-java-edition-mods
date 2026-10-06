package com.example.wenhanclient.notexturerotation.mixin;

import com.example.wenhanclient.notexturerotation.NoTextureRotationSubMod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(
		targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer",
		remap = false)
public abstract class SodiumBlockRendererMixin {
	private static final long STABLE_MODEL_RANDOM_SEED = 0L;

	@ModifyArg(
			method = "renderModel",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/util/RandomSource;setSeed(J)V",
							remap = true),
			index = 0,
			require = 0,
			remap = false)
	private long noTextureRotation$useStableSodiumModelSeed(long seed) {
		if (!NoTextureRotationSubMod.isEnabled()) {
			return seed;
		}

		return STABLE_MODEL_RANDOM_SEED;
	}
}

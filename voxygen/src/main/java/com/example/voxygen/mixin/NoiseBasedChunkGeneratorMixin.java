package com.example.voxygen.mixin;

import com.example.voxygen.VoxygenWorldgenRules;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
abstract class NoiseBasedChunkGeneratorMixin {
	@Inject(method = "generateCarvers", at = @At("HEAD"), cancellable = true)
	private void voxygen$skipCarvers(
			ChunkAccess chunk,
			Blender blender,
			NoiseChunk noiseChunk,
			RandomState randomState,
			BiomeManager biomeManager,
			WorldGenRegion region,
			MaterialRule materialRule,
			CallbackInfo callback) {
		if (VoxygenWorldgenRules.isVirtualChunk(chunk)) {
			callback.cancel();
		}
	}
}

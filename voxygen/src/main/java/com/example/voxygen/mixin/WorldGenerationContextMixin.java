package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenerationContext.class)
abstract class WorldGenerationContextMixin {
	@Inject(method = "of", at = @At("HEAD"), cancellable = true)
	private static void voxygen$createForVirtualWorld(
			LevelAccessor level, CallbackInfoReturnable<WorldGenerationContext> callback) {
		if (!(level instanceof WorldGenLevel worldGenLevel) || worldGenLevel.getLevel() != null) {
			return;
		}

		ChunkGenerator generator = WorldgenSeedContext.generator();
		if (generator != null) {
			callback.setReturnValue(new WorldGenerationContext(generator, level));
		}
	}
}

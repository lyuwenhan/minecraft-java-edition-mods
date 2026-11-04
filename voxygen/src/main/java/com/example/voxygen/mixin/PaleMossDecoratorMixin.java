package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.PaleMossDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaleMossDecorator.class)
abstract class PaleMossDecoratorMixin {
	@Inject(method = "lambda$place$1", at = @At("HEAD"), cancellable = true)
	private static void voxygen$useVirtualGenerator(
			WorldGenLevel world,
			RandomSource random,
			BlockPos position,
			Holder.Reference<Feature> feature,
			CallbackInfo callback) {
		ChunkGenerator generator = WorldgenSeedContext.generator();
		if (generator == null) {
			return;
		}
		feature.value().place(world, generator, random, position.above());
		callback.cancel();
	}
}

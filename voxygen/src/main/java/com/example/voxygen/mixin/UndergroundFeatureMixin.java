package com.example.voxygen.mixin;

import com.example.voxygen.VoxygenWorldgenRules;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({OreFeature.class, ScatteredOreFeature.class, GeodeFeature.class, MonsterRoomFeature.class})
abstract class UndergroundFeatureMixin {
	@Inject(method = "place", at = @At("HEAD"), cancellable = true)
	private void voxygen$skipUndergroundFeatureDirectly(
			WorldGenLevel world,
			ChunkGenerator generator,
			RandomSource random,
			BlockPos origin,
			CallbackInfoReturnable<Boolean> callback) {
		if (VoxygenWorldgenRules.isVirtualGeneration(world, origin)) {
			callback.setReturnValue(false);
		}
	}
}

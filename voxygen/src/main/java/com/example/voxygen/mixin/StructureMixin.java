package com.example.voxygen.mixin;

import com.example.voxygen.VoxygenWorldgenRules;
import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Structure.class)
abstract class StructureMixin {
	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void voxygen$skipUndergroundStructure(
			Holder<Structure> structure,
			ResourceKey<Level> dimension,
			RegistryAccess registries,
			ChunkGenerator generator,
			BiomeSource biomeSource,
			Climate.Sampler climateSampler,
			RandomState randomState,
			StructureTemplateManager templates,
			long seed,
			ChunkPos chunkPos,
			int references,
			LevelHeightAccessor height,
			Predicate<Holder<Biome>> validBiome,
			CallbackInfoReturnable<StructureStart> callback) {
		if (WorldgenSeedContext.isVirtualWorldActive()
				&& VoxygenWorldgenRules.shouldSkipStructure(structure)) {
			callback.setReturnValue(StructureStart.INVALID_START);
		}
	}
}

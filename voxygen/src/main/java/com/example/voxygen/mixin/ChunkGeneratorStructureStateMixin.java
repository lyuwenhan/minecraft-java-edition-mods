package com.example.voxygen.mixin;

import com.example.voxygen.VirtualStructureState;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGeneratorStructureState.class)
abstract class ChunkGeneratorStructureStateMixin implements VirtualStructureState {
	@Shadow @Final private RandomState randomState;

	@Shadow @Final private BiomeSource biomeSource;

	@Shadow @Final private long concentricRingsSeed;

	@Unique private boolean voxygen$virtual;

	@Override
	public void voxygen$markVirtual() {
		voxygen$virtual = true;
	}

	@Inject(method = "generateRingPositions", at = @At("HEAD"), cancellable = true)
	private void voxygen$generateVirtualRingPositions(
			Holder<StructureSet> structureSet,
			ConcentricRingsStructurePlacement placement,
			CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> callback) {
		if (!voxygen$virtual) {
			return;
		}

		int count = placement.count();
		if (count == 0) {
			callback.setReturnValue(CompletableFuture.completedFuture(List.of()));
			return;
		}

		int distance = placement.distance();
		int spread = placement.spread();
		HolderSet<Biome> preferredBiomes = placement.preferredBiomes();
		RandomSource random = RandomSource.create();
		random.setSeed(concentricRingsSeed);
		double angle = random.nextDouble() * Math.PI * 2.0D;
		int positionInRing = 0;
		int ring = 0;
		List<CompletableFuture<ChunkPos>> positions = new ArrayList<>(count);

		for (int index = 0; index < count; index++) {
			double radius =
					(4 * distance + distance * ring * 6)
							+ (random.nextDouble() - 0.5D) * distance * 2.5D;
			int chunkX = (int) Math.round(Math.cos(angle) * radius);
			int chunkZ = (int) Math.round(Math.sin(angle) * radius);
			RandomSource searchRandom = random.fork();
			positions.add(
					CompletableFuture.supplyAsync(
							() ->
									voxygen$findPreferredBiome(
											chunkX, chunkZ, preferredBiomes, searchRandom),
							Util.backgroundExecutor().forName("structureRings")));

			angle += Math.PI * 2.0D / spread;
			positionInRing++;
			if (positionInRing == spread) {
				ring++;
				positionInRing = 0;
				spread += 2 * spread / (ring + 1);
				spread = Math.min(spread, count - index);
				angle += random.nextDouble() * Math.PI * 2.0D;
			}
		}

		callback.setReturnValue(Util.sequence(positions));
	}

	@Unique
	private ChunkPos voxygen$findPreferredBiome(
			int chunkX, int chunkZ, HolderSet<Biome> preferredBiomes, RandomSource random) {
		Pair<BlockPos, Holder<Biome>> biome =
				biomeSource.findBiomeHorizontal(
						SectionPos.sectionToBlockCoord(chunkX, 8),
						0,
						SectionPos.sectionToBlockCoord(chunkZ, 8),
						112,
						preferredBiomes::contains,
						random,
						randomState);
		if (biome == null) {
			return new ChunkPos(chunkX, chunkZ);
		}
		BlockPos position = biome.getFirst();
		return new ChunkPos(
				SectionPos.blockToSectionCoord(position.getX()),
				SectionPos.blockToSectionCoord(position.getZ()));
	}
}

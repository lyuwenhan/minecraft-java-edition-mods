package com.example.voxygen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.OverworldFunctionSet;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FossilFeature;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.VerticalGradientCondition;
import net.minecraft.world.level.levelgen.material.rule.ConditionRule;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import net.minecraft.world.level.levelgen.material.rule.SequenceRule;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class VoxygenWorldgenRules {
	private static final Set<String> DISABLED_CAVE_FEATURES =
			Set.of(
					"minecraft:rooted_sulfur_spring",
					"minecraft:rooted_azalea_tree",
					"minecraft:sulfur_spike_cluster",
					"minecraft:sulfur_spike",
					"minecraft:sulfur_pool",
					"minecraft:cave_vine",
					"minecraft:cave_vine_in_moss",
					"minecraft:cave_vines",
					"minecraft:moss_vegetation",
					"minecraft:moss_patch",
					"minecraft:moss_patch_bonemeal",
					"minecraft:dripleaf",
					"minecraft:clay_with_dripleaves",
					"minecraft:clay_pool_with_dripleaves",
					"minecraft:lush_caves_clay",
					"minecraft:lush_caves_ceiling_vegetation",
					"minecraft:lush_caves_vegetation",
					"minecraft:moss_patch_ceiling",
					"minecraft:spore_blossom",
					"minecraft:classic_vines_cave_feature");
	private static final Set<String> DISABLED_PLACED_FEATURES =
			Set.of("minecraft:lake_lava_underground");
	private static final Map<ChunkAccess, ResourceKey<Level>> VIRTUAL_CHUNKS =
			new ConcurrentHashMap<>();

	private VoxygenWorldgenRules() {}

	public static void registerChunk(ChunkAccess chunk, ResourceKey<Level> dimension) {
		VIRTUAL_CHUNKS.put(chunk, dimension);
	}

	public static void unregisterChunk(ChunkAccess chunk) {
		if (chunk != null) {
			VIRTUAL_CHUNKS.remove(chunk);
		}
	}

	public static boolean isVirtualChunk(ChunkAccess chunk) {
		return chunk != null && VIRTUAL_CHUNKS.containsKey(chunk);
	}

	public static ChunkGenerator simplifyGenerator(
			ChunkGenerator original, RegistryAccess registries, ResourceKey<Level> dimension) {
		if (!Level.OVERWORLD.equals(dimension)
				|| !(original instanceof NoiseBasedChunkGenerator noiseGenerator)) {
			return original;
		}

		Holder<NoiseGeneratorSettings> settingsHolder = noiseGenerator.generatorSettings();
		NoiseGeneratorSettings settings = settingsHolder.value();
		MaterialRule materialRule = stripUndergroundMaterialRules(settings.materialRule().value());
		if (materialRule == null) {
			throw new IllegalStateException("The overworld material rule became empty");
		}
		MaterialRule fixedBedrock =
				MaterialRules.ifTrue(
						MaterialRules.not(
								MaterialRules.yBlockCheck(VerticalAnchor.aboveBottom(2), 0)),
						MaterialRules.state(Blocks.BEDROCK.defaultBlockState()));
		MaterialRule fixedDeepslate =
				MaterialRules.ifTrue(
						MaterialRules.not(MaterialRules.yBlockCheck(VerticalAnchor.absolute(0), 0)),
						MaterialRules.state(Blocks.DEEPSLATE.defaultBlockState()));
		materialRule = MaterialRules.sequence(fixedBedrock, materialRule, fixedDeepslate);

		NoiseRouter router = withoutNoiseCaves(settingsHolder, registries, settings.noiseRouter());
		NoiseGeneratorSettings simplified =
				new NoiseGeneratorSettings(
						settings.noiseSettings(),
						settings.defaultBlock(),
						settings.defaultFluid(),
						router,
						Holder.direct(materialRule),
						settings.spawnTarget(),
						settings.seaLevel(),
						settings.disableMobGeneration(),
						Optional.empty(),
						settings.useLegacyRandomSource(),
						settings.debugFunctions());
		return new NoiseBasedChunkGenerator(
				noiseGenerator.getBiomeSource(), Holder.direct(simplified));
	}

	private static NoiseRouter withoutNoiseCaves(
			Holder<NoiseGeneratorSettings> settings,
			RegistryAccess registries,
			NoiseRouter original) {
		boolean amplified = settings.is(NoiseGeneratorSettings.AMPLIFIED);
		OverworldFunctionSet<ResourceKey<DensityFunction>> functions =
				amplified
						? NoiseRouterData.AMPLIFIED_OVERWORLD_FUNCTIONS
						: settings.is(NoiseGeneratorSettings.LARGE_BIOMES)
								? NoiseRouterData.LARGE_OVERWORLD_FUNCTIONS
								: NoiseRouterData.OVERWORLD_FUNCTIONS;
		DensityFunction slopedCheese =
				NoiseRouterData.getFunction(
						registries.lookupOrThrow(Registries.DENSITY_FUNCTION),
						functions.slopedCheese());
		DensityFunction finalDensity =
				DensityFunctions.add(
						NoiseRouterData.postProcess(
								NoiseRouterData.slideOverworld(amplified, slopedCheese), 4, 8),
						DensityFunctions.beardifier());
		return new NoiseRouter(
				original.temperature(),
				original.vegetation(),
				original.continents(),
				original.erosion(),
				original.depth(),
				original.ridges(),
				original.chunkSurfaceLevel(),
				finalDensity);
	}

	private static MaterialRule stripUndergroundMaterialRules(MaterialRule rule) {
		if (rule instanceof OreVeinRule) {
			return null;
		}
		if (rule instanceof MaterialRule.HolderHolder holderRule) {
			String id =
					holderRule
							.holder()
							.unwrapKey()
							.map(key -> key.identifier().toString())
							.orElse("");
			if (id.endsWith("bedrock_floor")
					|| id.endsWith("bedrock_roof")
					|| id.endsWith("overworld/sulfur_cave_bands")
					|| id.endsWith("overworld/biome_surface/sulfur_caves")) {
				return null;
			}
			return stripUndergroundMaterialRules(holderRule.holder().value());
		}
		if (rule instanceof ConditionRule conditionRule) {
			if (conditionRule.ifTrue() instanceof VerticalGradientCondition verticalGradient
					&& verticalGradient.randomName().getPath().equals("deepslate")) {
				return null;
			}
			MaterialRule nested = stripUndergroundMaterialRules(conditionRule.thenRun());
			return nested == null ? null : new ConditionRule(conditionRule.ifTrue(), nested);
		}
		if (rule instanceof SequenceRule sequenceRule) {
			List<MaterialRule> retained = new ArrayList<>();
			for (MaterialRule nested : sequenceRule.sequence()) {
				MaterialRule stripped = stripUndergroundMaterialRules(nested);
				if (stripped != null) {
					retained.add(stripped);
				}
			}
			return retained.isEmpty() ? null : new SequenceRule(List.copyOf(retained));
		}
		return rule;
	}

	public static boolean shouldSkipFeature(Feature feature) {
		return feature instanceof AbstractOreFeature
				|| feature instanceof MonsterRoomFeature
				|| feature instanceof GeodeFeature
				|| feature instanceof FossilFeature;
	}

	public static boolean shouldSkipFeature(Holder<Feature> feature) {
		String id = feature.unwrapKey().map(key -> key.identifier().toString()).orElse("");
		return shouldSkipFeature(feature.value()) || DISABLED_CAVE_FEATURES.contains(id);
	}

	public static String placedFeatureId(WorldGenLevel world, PlacedFeature feature) {
		return world.registryAccess()
				.lookupOrThrow(Registries.PLACED_FEATURE)
				.getResourceKey(feature)
				.map(key -> key.identifier().toString())
				.orElse("");
	}

	public static boolean shouldSkipPlacedFeature(WorldGenLevel world, PlacedFeature feature) {
		return DISABLED_PLACED_FEATURES.contains(placedFeatureId(world, feature))
				|| shouldSkipFeature(feature.feature());
	}

	public static boolean isVirtualGeneration(WorldGenLevel world, BlockPos origin) {
		if (WorldgenSeedContext.isVirtualWorldActive()) {
			return true;
		}
		return isVirtualChunk(world.getChunk(origin));
	}

	public static boolean shouldSkipStructure(Holder<?> structure) {
		String id = structure.unwrapKey().map(key -> key.identifier().toString()).orElse("");
		return id.equals("minecraft:mineshaft")
				|| id.equals("minecraft:mineshaft_mesa")
				|| id.equals("minecraft:stronghold")
				|| id.equals("minecraft:ancient_city")
				|| id.equals("minecraft:trial_chambers")
				|| id.equals("minecraft:buried_treasure");
	}

	public static boolean shouldRejectGeneratedBlock(BlockState state) {
		return state.is(BlockTags.ORES)
				|| state.getBlock() instanceof InfestedBlock
				|| state.is(Blocks.SPAWNER)
				|| state.is(Blocks.TRIAL_SPAWNER);
	}
}

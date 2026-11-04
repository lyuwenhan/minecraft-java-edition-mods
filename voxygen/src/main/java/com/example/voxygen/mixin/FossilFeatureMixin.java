package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FossilFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FossilFeature.class)
abstract class FossilFeatureMixin {
	@Inject(method = "place", at = @At("HEAD"), cancellable = true)
	private void voxygen$placeInVirtualWorld(
			WorldGenLevel world,
			ChunkGenerator generator,
			RandomSource random,
			BlockPos origin,
			CallbackInfoReturnable<Boolean> callback) {
		StructureTemplateManager templates = WorldgenSeedContext.templates();
		if (templates == null) {
			return;
		}

		FossilFeature feature = (FossilFeature) (Object) this;
		Rotation rotation = Rotation.getRandom(random);
		int structureIndex = random.nextInt(feature.fossilStructures().size());
		StructureTemplate fossil =
				templates.getOrCreate(feature.fossilStructures().get(structureIndex));
		StructureTemplate overlay =
				templates.getOrCreate(feature.overlayStructures().get(structureIndex));

		ChunkPos chunk = ChunkPos.containing(origin);
		BoundingBox placementBounds =
				new BoundingBox(
						chunk.getMinBlockX() - 16,
						world.getMinY(),
						chunk.getMinBlockZ() - 16,
						chunk.getMaxBlockX() + 16,
						world.getMaxY(),
						chunk.getMaxBlockZ() + 16);
		StructurePlaceSettings settings =
				new StructurePlaceSettings()
						.setRotation(rotation)
						.setBoundingBox(placementBounds)
						.setRandom(random);

		Vec3i size = fossil.getSize(rotation);
		BlockPos centeredOrigin = origin.offset(-size.getX() / 2, 0, -size.getZ() / 2);
		int minimumHeight = origin.getY();
		for (int x = 0; x < size.getX(); x++) {
			for (int z = 0; z < size.getZ(); z++) {
				minimumHeight =
						Math.min(
								minimumHeight,
								world.getHeight(
										Heightmap.Types.OCEAN_FLOOR_WG,
										centeredOrigin.getX() + x,
										centeredOrigin.getZ() + z));
			}
		}

		int y = Math.max(minimumHeight - 15 - random.nextInt(10), world.getMinY() + 10);
		BlockPos placementOrigin =
				fossil.getZeroPositionWithTransform(centeredOrigin.atY(y), Mirror.NONE, rotation);
		BoundingBox fossilBounds = fossil.getBoundingBox(settings, placementOrigin);
		int[] emptyCorners = {0};
		fossilBounds.forAllCorners(
				corner -> {
					var state = world.getBlockState(corner);
					if (state.isAir() || state.is(Blocks.LAVA) || state.is(Blocks.WATER)) {
						emptyCorners[0]++;
					}
				});
		if (emptyCorners[0] > feature.maxEmptyCornersAllowed()) {
			callback.setReturnValue(false);
			return;
		}

		settings.clearProcessors();
		for (StructureProcessor processor : feature.fossilProcessors().value().list()) {
			settings.addProcessor(processor);
		}
		fossil.placeInWorld(world, placementOrigin, placementOrigin, settings, random, 260);

		settings.clearProcessors();
		for (StructureProcessor processor : feature.overlayProcessors().value().list()) {
			settings.addProcessor(processor);
		}
		overlay.placeInWorld(world, placementOrigin, placementOrigin, settings, random, 260);
		callback.setReturnValue(true);
	}
}

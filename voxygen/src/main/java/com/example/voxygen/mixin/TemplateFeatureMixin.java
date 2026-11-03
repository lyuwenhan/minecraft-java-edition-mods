package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.TemplateFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemplateFeature.class)
abstract class TemplateFeatureMixin {
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

		TemplateFeature feature = (TemplateFeature) (Object) this;
		TemplateFeature.TemplateEntry entry = feature.templates().getRandomOrThrow(random);
		Rotation rotation = Util.getRandom(entry.rotations(), random);
		StructureTemplate template = templates.getOrCreate(entry.template());
		Vec3i xOffset = rotatedOffset(rotation, Direction.Axis.X, template);
		Vec3i zOffset = rotatedOffset(rotation, Direction.Axis.Z, template);
		BlockPos placement = origin.offset(xOffset).offset(zOffset);
		StructurePlaceSettings settings =
				new StructurePlaceSettings().setRotation(rotation).setRandom(random);
		feature.processors()
				.ifPresent(
						processors -> {
							for (StructureProcessor processor : processors.value().list()) {
								settings.addProcessor(processor);
							}
						});
		callback.setReturnValue(
				template.placeInWorld(world, placement, placement, settings, random, 3));
	}

	private static Vec3i rotatedOffset(
			Rotation rotation, Direction.Axis axis, StructureTemplate template) {
		return rotation.rotate(axis.getNegative())
				.getUnitVec3i()
				.multiply(template.getSize().get(axis) / 2);
	}
}

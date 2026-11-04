package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureTemplate.class)
abstract class StructureTemplateEntityMixin {
	@Inject(method = "placeEntities", at = @At("HEAD"), cancellable = true)
	private void voxygen$skipVirtualTemplateEntities(
			ServerLevelAccessor world,
			BlockPos position,
			Mirror mirror,
			Rotation rotation,
			BlockPos pivot,
			BoundingBox bounds,
			boolean finalizeEntities,
			ProblemReporter reporter,
			CallbackInfo callback) {
		if (WorldgenSeedContext.isVirtualWorldActive()) {
			callback.cancel();
		}
	}
}

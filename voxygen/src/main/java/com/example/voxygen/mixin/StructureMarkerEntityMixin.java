package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
		targets = {
			"net.minecraft.world.level.levelgen.structure.structures.EndCityPieces$EndCityPiece",
			"net.minecraft.world.level.levelgen.structure.structures.OceanRuinPieces$OceanRuinPiece",
			"net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces$WoodlandMansionPiece"
		})
abstract class StructureMarkerEntityMixin {
	@Inject(
			method = "handleDataMarker",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/ServerLevelAccessor;getLevel()Lnet/minecraft/server/level/ServerLevel;"),
			cancellable = true)
	private void voxygen$skipVirtualStructureEntity(
			String marker,
			BlockPos position,
			ServerLevelAccessor world,
			RandomSource random,
			BoundingBox bounds,
			CallbackInfo callback) {
		if (WorldgenSeedContext.isVirtualWorldActive()) {
			callback.cancel();
		}
	}
}

package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

final class DirectStructureEntityMixin {
	private DirectStructureEntityMixin() {}
}

@Mixin(EndSpikeFeature.class)
abstract class EndSpikeEntityMixin {
	@Inject(
			method = "placeSpike",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/ServerLevelAccessor;getLevel()Lnet/minecraft/server/level/ServerLevel;"),
			cancellable = true)
	private void voxygen$skipVirtualCrystal(
			ServerLevelAccessor world,
			RandomSource random,
			EndSpikeFeature.EndSpike spike,
			CallbackInfo callback) {
		if (WorldgenSeedContext.isVirtualWorldActive()) {
			callback.cancel();
		}
	}
}

@Mixin(
		targets =
				"net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces$MineShaftCorridor")
abstract class MineshaftEntityMixin {
	@Inject(
			method = "createChest",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/WorldGenLevel;getLevel()Lnet/minecraft/server/level/ServerLevel;"),
			cancellable = true)
	private void voxygen$skipVirtualChestMinecart(
			WorldGenLevel world,
			BoundingBox bounds,
			RandomSource random,
			int x,
			int y,
			int z,
			ResourceKey<LootTable> lootTable,
			CallbackInfoReturnable<Boolean> callback) {
		if (WorldgenSeedContext.isVirtualWorldActive()) {
			callback.setReturnValue(true);
		}
	}
}

@Mixin(
		targets =
				"net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces$OceanMonumentPiece")
abstract class OceanMonumentEntityMixin {
	@Inject(
			method = "spawnElder",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/WorldGenLevel;getLevel()Lnet/minecraft/server/level/ServerLevel;"),
			cancellable = true)
	private void voxygen$skipVirtualElderGuardian(
			WorldGenLevel world, BoundingBox bounds, int x, int y, int z, CallbackInfo callback) {
		if (WorldgenSeedContext.isVirtualWorldActive()) {
			callback.cancel();
		}
	}
}

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.structures.SwampHutPiece")
abstract class SwampHutEntityMixin {
	@Inject(
			method = "postProcess",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/WorldGenLevel;getLevel()Lnet/minecraft/server/level/ServerLevel;"),
			cancellable = true)
	private void voxygen$skipVirtualHutEntities(
			WorldGenLevel world,
			StructureManager structures,
			ChunkGenerator generator,
			RandomSource random,
			BoundingBox bounds,
			ChunkPos chunk,
			BlockPos position,
			CallbackInfo callback) {
		if (WorldgenSeedContext.isVirtualWorldActive()) {
			callback.cancel();
		}
	}
}

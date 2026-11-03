package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CappedProcessor.class)
abstract class CappedProcessorMixin {
	@Redirect(
			method = "finalizeProcessing",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/server/level/ServerLevel;getSeed()J"))
	private long voxygen$useVirtualSeed(ServerLevel level) {
		return WorldgenSeedContext.seedOrLevel(level);
	}
}

package com.example.voxygen.mixin;

import com.example.voxygen.WorldgenSeedContext;

import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(RuinedPortalStructure.class)
abstract class RuinedPortalStructureMixin {
	@Inject(
			method = "findGenerationPoint",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/levelgen/structure/structures/RuinedPortalStructure$Setup;airPocketProbability()F"),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void voxygen$skipBuriedPortal(
			Structure.GenerationContext context,
			CallbackInfoReturnable<Optional<Structure.GenerationStub>> callback,
			WorldgenRandom random,
			RuinedPortalStructure.Setup chosenSetup,
			RuinedPortalStructure.Setup setup) {
		if (!WorldgenSeedContext.isVirtualWorldActive()) {
			return;
		}

		RuinedPortalPiece.VerticalPlacement placement = setup.placement();
		if (placement == RuinedPortalPiece.VerticalPlacement.UNDERGROUND
				|| placement == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
			callback.setReturnValue(Optional.empty());
		}
	}
}

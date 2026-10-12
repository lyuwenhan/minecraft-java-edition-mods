package com.example.wenhanclient.fluidplatform.mixin;

import com.example.wenhanclient.fluidplatform.FluidPlatformSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFluidInteraction.class)
public abstract class EntityFluidInteractionMixin {
	@Redirect(
			method = "update",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	private float fluidplatform$getFullFluidHeight(
			FluidState fluidState,
			BlockGetter blockGetter,
			BlockPos pos,
			Entity entity,
			boolean ignoreCurrent) {
		if (fluidplatform$shouldUseFullFluidHeight(fluidState, entity)) {
			return 1.0F;
		}
		return fluidState.getHeight(blockGetter, pos);
	}

	@Redirect(
			method = "update",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/material/FluidState;getHeightForCamera(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	private float fluidplatform$getFullFluidHeightForCamera(
			FluidState fluidState,
			BlockGetter blockGetter,
			BlockPos pos,
			Entity entity,
			boolean ignoreCurrent) {
		if (fluidplatform$shouldUseFullFluidHeight(fluidState, entity)) {
			return 1.0F;
		}
		return fluidState.getHeightForCamera(blockGetter, pos);
	}

	private static boolean fluidplatform$shouldUseFullFluidHeight(
			FluidState fluidState, Entity entity) {
		return entity instanceof LocalPlayer
				&& FluidPlatformSubMod.isEnabled()
				&& (fluidState.is(FluidTags.WATER) || fluidState.is(FluidTags.LAVA));
	}
}

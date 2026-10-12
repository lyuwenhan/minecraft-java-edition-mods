package com.example.wenhanclient.fluidplatform.mixin;

import com.example.wenhanclient.fluidplatform.FluidPlatformSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityCollisionContext.class)
public abstract class EntityCollisionContextMixin {
	@Shadow @Final private Entity entity;

	@Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
	private void fluidplatform$allowLocalPlayerToStandOnFluid(
			FluidState aboveFluid, FluidState currentFluid, CallbackInfoReturnable<Boolean> cir) {
		if (this.entity instanceof LocalPlayer player
				&& FluidPlatformSubMod.isEnabled()
				&& !player.isShiftKeyDown()
				&& (currentFluid.is(FluidTags.WATER) || currentFluid.is(FluidTags.LAVA))
				&& !aboveFluid.getType().isSame(currentFluid.getType())) {
			cir.setReturnValue(true);
		}
	}
}

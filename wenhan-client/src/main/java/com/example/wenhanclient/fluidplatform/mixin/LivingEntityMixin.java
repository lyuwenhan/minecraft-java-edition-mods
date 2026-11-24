package com.example.wenhanclient.fluidplatform.mixin;

import com.example.wenhanclient.fluidplatform.FluidPlatformSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "getLiquidCollisionShape", at = @At("HEAD"), cancellable = true)
	private void fluidplatform$getLocalPlayerLiquidCollisionShape(
			CallbackInfoReturnable<VoxelShape> cir) {
		if ((Object) this instanceof LocalPlayer player
				&& FluidPlatformSubMod.isEnabled()
				&& !player.isShiftKeyDown()) {
			cir.setReturnValue(Shapes.block());
		}
	}
}

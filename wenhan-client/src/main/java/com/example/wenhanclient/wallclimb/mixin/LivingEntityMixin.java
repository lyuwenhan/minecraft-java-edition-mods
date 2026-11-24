package com.example.wenhanclient.wallclimb.mixin;

import com.example.wenhanclient.wallclimb.WallClimbSubMod;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
	private void wallClimb$allowClimbingWalls(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (WallClimbSubMod.canClimbWall((LivingEntity) (Object) this)) {
			callbackInfo.setReturnValue(true);
		}
	}
}

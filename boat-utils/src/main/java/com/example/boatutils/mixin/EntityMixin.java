package com.example.boatutils.mixin;

import com.example.boatutils.BoatUtilsConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
	private void boatUtils$applyBoatStepHeight(CallbackInfoReturnable<Float> callbackInfo) {
		if (!((Object) this instanceof AbstractBoat)) {
			return;
		}

		callbackInfo.setReturnValue(BoatUtilsConfig.boatStepHeight());
	}

	@Redirect(
			method = "collide",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onGround()Z"))
	private boolean boatUtils$allowBoatStepWithoutGroundCheck(Entity entity) {
		if (!(entity instanceof AbstractBoat)) {
			return entity.onGround();
		}

		return BoatUtilsConfig.boatStepHeight() > 0.0F;
	}
}

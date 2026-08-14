package com.example.flyspeedmodifier.mixin;

import com.example.flyspeedmodifier.FreecamSpeedController;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class ElytraMovementSpeedMixin {
	@WrapOperation(
			method = "travelFallFlying",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/LivingEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
	private void flySpeedModifier$scaleElytraHorizontalDisplacement(
			LivingEntity instance, MoverType moverType, Vec3 movement, Operation<Void> original) {
		if (!flySpeedModifier$isLocalPlayer(instance)) {
			original.call(instance, moverType, movement);
			return;
		}

		if (!FreecamSpeedController.shouldModifyOtherMovement()) {
			original.call(instance, moverType, movement);
			return;
		}

		double multiplier = FreecamSpeedController.otherMovementMultiplier();
		Vec3 scaledMovement =
				new Vec3(movement.x * multiplier, movement.y, movement.z * multiplier);
		original.call(instance, moverType, scaledMovement);
	}

	@Unique
	private boolean flySpeedModifier$isLocalPlayer(LivingEntity entity) {
		Minecraft client = Minecraft.getInstance();
		return client.player != null && entity == client.player;
	}
}

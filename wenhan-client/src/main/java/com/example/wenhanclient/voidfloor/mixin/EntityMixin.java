package com.example.wenhanclient.voidfloor.mixin;

import com.example.wenhanclient.voidfloor.VoidFloorSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 1100)
public abstract class EntityMixin {
	@Unique private boolean voidFloor$blockedDownwardMovement;

	@ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
	private Vec3 voidFloor$restrictDownwardMovement(Vec3 movement) {
		voidFloor$blockedDownwardMovement = false;
		if (!((Object) this instanceof LocalPlayer player)) {
			return movement;
		}
		Vec3 restricted = VoidFloorSubMod.restrictDownwardMovement(player, movement);
		voidFloor$blockedDownwardMovement = restricted != movement;
		return restricted;
	}

	@Inject(method = "move", at = @At("TAIL"))
	private void voidFloor$finishLanding(
			MoverType moverType, Vec3 movement, CallbackInfo callbackInfo) {
		if (!voidFloor$blockedDownwardMovement || !((Object) this instanceof LocalPlayer player)) {
			return;
		}
		Vec3 velocity = player.getDeltaMovement();
		if (velocity.y < 0.0D) {
			player.setDeltaMovement(velocity.x, 0.0D, velocity.z);
		}
		player.setOnGround(true);
		player.resetFallDistance();
	}
}

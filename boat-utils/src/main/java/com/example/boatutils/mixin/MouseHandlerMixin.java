package com.example.boatutils.mixin;

import com.example.boatutils.BoatUtilsConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Unique private boolean boatUtils$horizontalViewChanged;

	@Inject(method = "turnPlayer", at = @At("HEAD"))
	private void boatUtils$resetHorizontalViewChange(double frameTime, CallbackInfo callbackInfo) {
		this.boatUtils$horizontalViewChanged = false;
	}

	@ModifyArg(
			method = "turnPlayer",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"),
			index = 0)
	private double boatUtils$trackHorizontalViewRotation(double horizontalRotation) {
		Minecraft client = Minecraft.getInstance();
		AbstractBoat boat = boatUtils$getControlledBoat(client);
		if (boat == null) {
			return horizontalRotation;
		}
		if (!BoatUtilsConfig.viewDirectionLockEnabled()) {
			return horizontalRotation;
		}

		this.boatUtils$horizontalViewChanged = horizontalRotation != 0.0D;
		return horizontalRotation;
	}

	@Inject(method = "turnPlayer", at = @At("RETURN"))
	private void boatUtils$makeBoatFollowViewImmediately(
			double frameTime, CallbackInfo callbackInfo) {
		Minecraft client = Minecraft.getInstance();
		AbstractBoat boat = boatUtils$getControlledBoat(client);
		if (boat == null) {
			return;
		}
		if (!BoatUtilsConfig.viewDirectionLockEnabled()) {
			return;
		}

		if (this.boatUtils$horizontalViewChanged) {
			((AbstractBoatAccessor) boat).boatUtils$setDeltaRotation(0.0F);
		}

		float viewYaw = client.player.getYRot();
		boat.setYRot(viewYaw);
		boat.setYBodyRot(viewYaw);
	}

	private static AbstractBoat boatUtils$getControlledBoat(Minecraft client) {
		if (client.player == null) {
			return null;
		}

		Entity vehicle = client.player.getVehicle();
		if (!(vehicle instanceof AbstractBoat boat)) {
			return null;
		}
		if (boat.getControllingPassenger() != client.player) {
			return null;
		}
		return boat;
	}
}

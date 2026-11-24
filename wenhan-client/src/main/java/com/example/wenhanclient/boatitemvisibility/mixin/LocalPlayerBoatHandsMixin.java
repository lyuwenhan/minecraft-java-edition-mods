package com.example.wenhanclient.boatitemvisibility.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerBoatHandsMixin {
	@Shadow private boolean handsBusy;

	@Inject(method = "rideTick", at = @At("TAIL"))
	private void boatItemVisibility$keepHeldItemsVisibleWhenControllingBoat(CallbackInfo ci) {
		Entity vehicle = ((LocalPlayer) (Object) this).getControlledVehicle();
		if (vehicle instanceof AbstractBoat) {
			this.handsBusy = false;
		}
	}
}

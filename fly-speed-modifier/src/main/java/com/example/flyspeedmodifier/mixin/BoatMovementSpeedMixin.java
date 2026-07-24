package com.example.flyspeedmodifier.mixin;

import com.example.flyspeedmodifier.FreecamSpeedController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AbstractBoat.class)
public abstract class BoatMovementSpeedMixin {
	@ModifyArgs(
			method = "controlBoat",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
	private void flySpeedModifier$multiplyBoatPropulsion(Args args) {
		if (!flySpeedModifier$isControlledByLocalPlayer()) {
			return;
		}
		if (!FreecamSpeedController.shouldModifyOtherMovement()) {
			return;
		}

		double multiplier = FreecamSpeedController.otherMovementMultiplier();
		args.set(0, args.<Double>get(0) * multiplier);
		args.set(2, args.<Double>get(2) * multiplier);
	}

	private boolean flySpeedModifier$isControlledByLocalPlayer() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return false;
		}

		AbstractBoat self = (AbstractBoat) (Object) this;
		Entity controllingPassenger = self.getControllingPassenger();
		return controllingPassenger == client.player;
	}
}

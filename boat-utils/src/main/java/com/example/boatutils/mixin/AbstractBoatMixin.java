package com.example.boatutils.mixin;

import com.example.boatutils.BoatUtilsConfig;
import com.example.boatutils.BoatUtilsMod;
import com.example.boatutils.FlySpeedModifierIntegration;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin {
	private static final float BOAT_UTILS_BLUE_ICE_FRICTION = 0.989F;
	private static final double BOAT_UTILS_UNDERWATER_UPWARD_SPEED = 0.12D;

	@Shadow private AbstractBoat.Status status;

	@Shadow private float outOfControlTicks;

	@Shadow private boolean inputLeft;

	@Shadow private boolean inputRight;

	@Shadow private boolean inputUp;

	@Shadow private boolean inputDown;

	@Shadow
	protected abstract int getMaxPassengers();

	@Inject(method = "controlBoat", at = @At("HEAD"), cancellable = true)
	private void boatUtils$useViewRelativeMovement(CallbackInfo callbackInfo) {
		if (!BoatUtilsConfig.viewDirectionLockEnabled()) {
			return;
		}
		if (!boatUtils$isControlledByLocalPlayer()) {
			return;
		}

		AbstractBoat self = (AbstractBoat) (Object) this;
		if (!self.isVehicle()) {
			callbackInfo.cancel();
			return;
		}

		float forwardInput = 0.0F;
		float strafeInput = 0.0F;
		if (this.inputUp) {
			forwardInput += 1.0F;
		}
		if (this.inputDown) {
			forwardInput -= 1.0F;
		}
		if (this.inputLeft) {
			strafeInput -= 1.0F;
		}
		if (this.inputRight) {
			strafeInput += 1.0F;
		}

		float inputLength = Mth.sqrt(forwardInput * forwardInput + strafeInput * strafeInput);
		if (inputLength > 1.0F) {
			forwardInput /= inputLength;
			strafeInput /= inputLength;
		}

		float yawRadians = self.getYRot() * Mth.DEG_TO_RAD;
		float sinYaw = Mth.sin(yawRadians);
		float cosYaw = Mth.cos(yawRadians);
		double acceleration = FlySpeedModifierIntegration.applyOtherMovementMultiplier(0.04D);
		double accelerationX = (-sinYaw * forwardInput - cosYaw * strafeInput) * acceleration;
		double accelerationZ = (cosYaw * forwardInput - sinYaw * strafeInput) * acceleration;
		Vec3 movement = self.getDeltaMovement();
		self.setDeltaMovement(movement.add(accelerationX, 0.0D, accelerationZ));
		BoatUtilsMod.applyHandbrakeAfterThrust(self);
		((AbstractBoatAccessor) self).boatUtils$setDeltaRotation(0.0F);
		self.setPaddleState(
				this.inputUp || this.inputDown || this.inputLeft || this.inputRight,
				this.inputUp || this.inputDown || this.inputLeft || this.inputRight);
		callbackInfo.cancel();
	}

	@Inject(method = "controlBoat", at = @At("TAIL"))
	private void boatUtils$applyHandbrakeAfterVanillaThrust(CallbackInfo callbackInfo) {
		if (BoatUtilsConfig.viewDirectionLockEnabled()) {
			return;
		}
		BoatUtilsMod.applyHandbrakeAfterThrust((AbstractBoat) (Object) this);
	}

	@Inject(method = "clampRotation", at = @At("HEAD"), cancellable = true)
	private void boatUtils$disableViewRotationLimit(Entity passenger, CallbackInfo callbackInfo) {
		Minecraft client = Minecraft.getInstance();
		if (passenger != client.player) {
			return;
		}
		if (!BoatUtilsConfig.unrestrictedViewRotation()) {
			return;
		}

		AbstractBoat self = (AbstractBoat) (Object) this;
		passenger.setYBodyRot(self.getYRot());
		passenger.setYHeadRot(passenger.getYRot());
		callbackInfo.cancel();
	}

	@ModifyVariable(method = "floatBoat", at = @At("STORE"), index = 5)
	private float boatUtils$useBlueIceFrictionInEveryStatus(float friction) {
		if (!BoatUtilsConfig.blueIceSpeedEverywhere()) {
			return friction;
		}
		if (!boatUtils$isControlledByLocalPlayer()) {
			return friction;
		}
		return BOAT_UTILS_BLUE_ICE_FRICTION;
	}

	@Redirect(
			method = "floatBoat",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
	private void boatUtils$applyBlueIceFrictionDuringWaterEntry(AbstractBoat boat, Vec3 movement) {
		if (!BoatUtilsConfig.blueIceSpeedEverywhere()) {
			boat.setDeltaMovement(movement);
			return;
		}
		if (!boatUtils$isControlledByLocalPlayer()) {
			boat.setDeltaMovement(movement);
			return;
		}

		boat.setDeltaMovement(
				movement.x * BOAT_UTILS_BLUE_ICE_FRICTION,
				movement.y,
				movement.z * BOAT_UTILS_BLUE_ICE_FRICTION);
	}

	@Inject(method = "floatBoat", at = @At("TAIL"))
	private void boatUtils$applyGentleUnderwaterBuoyancy(CallbackInfo callbackInfo) {
		if (!BoatUtilsConfig.preventSinking()) {
			return;
		}
		if (!boatUtils$isControlledByLocalPlayer()) {
			return;
		}
		if (this.status != AbstractBoat.Status.UNDER_WATER
				&& this.status != AbstractBoat.Status.UNDER_FLOWING_WATER) {
			return;
		}

		AbstractBoat self = (AbstractBoat) (Object) this;
		Vec3 movement = self.getDeltaMovement();
		self.setDeltaMovement(movement.x, BOAT_UTILS_UNDERWATER_UPWARD_SPEED, movement.z);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void boatUtils$keepUnderwaterPassengersMounted(CallbackInfo callbackInfo) {
		if (!BoatUtilsConfig.preventSinking()) {
			return;
		}
		if (this.status != AbstractBoat.Status.UNDER_WATER
				&& this.status != AbstractBoat.Status.UNDER_FLOWING_WATER) {
			return;
		}

		this.outOfControlTicks = 0.0F;
	}

	@Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
	private void boatUtils$allowUnderwaterBoarding(
			Entity passenger, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (!BoatUtilsConfig.preventSinking()) {
			return;
		}

		AbstractBoat self = (AbstractBoat) (Object) this;
		if (!self.isUnderWater()) {
			return;
		}

		callbackInfo.setReturnValue(self.getPassengers().size() < this.getMaxPassengers());
	}

	private boolean boatUtils$isControlledByLocalPlayer() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return false;
		}

		AbstractBoat self = (AbstractBoat) (Object) this;
		return self.getControllingPassenger() == client.player;
	}
}

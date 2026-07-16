package com.example.betterstep.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityAirStepUpMixin {

	@Unique private static final double BETTERSTEP_EPSILON = 1.0E-6D;

	@Unique private static final double BETTERSTEP_MIN_HORIZONTAL_IMPROVEMENT = 1.0E-7D;

	@Shadow public boolean horizontalCollision;

	@Shadow
	public abstract boolean onGround();

	@Shadow
	public abstract float maxUpStep();

	@Inject(
			method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
			at = @At("TAIL"))
	private void betterstep$airStepUpAfterVanillaMove(
			MoverType type, Vec3 movement, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;

		if (!this.betterstep$shouldTryAirStepUp(self, movement)) {
			return;
		}

		double savedX = self.getX();
		double savedY = self.getY();
		double savedZ = self.getZ();
		double maxStepUp = this.maxUpStep();

		Vec3 upAttempt = new Vec3(0.0D, maxStepUp, 0.0D);
		Vec3 upResult = ((EntityAccessor) self).betterstep$invokeCollide(upAttempt);

		if (upResult.y <= BETTERSTEP_EPSILON) {
			return;
		}

		self.setPos(savedX, savedY + upResult.y, savedZ);

		Vec3 horizontalAttempt = new Vec3(movement.x, 0.0D, movement.z);
		Vec3 horizontalResult = ((EntityAccessor) self).betterstep$invokeCollide(horizontalAttempt);
		double horizontalResultLengthSquared =
				horizontalResult.x * horizontalResult.x + horizontalResult.z * horizontalResult.z;

		if (horizontalResultLengthSquared <= BETTERSTEP_MIN_HORIZONTAL_IMPROVEMENT) {
			self.setPos(savedX, savedY, savedZ);
			return;
		}

		self.setPos(savedX + horizontalResult.x, savedY + upResult.y, savedZ + horizontalResult.z);

		double downProbe = upResult.y + BETTERSTEP_EPSILON;
		Vec3 downAttempt = new Vec3(0.0D, -downProbe, 0.0D);
		Vec3 downResult = ((EntityAccessor) self).betterstep$invokeCollide(downAttempt);

		if (downResult.y >= -BETTERSTEP_EPSILON) {
			self.setPos(savedX, savedY, savedZ);
			return;
		}

		self.setPos(
				savedX + horizontalResult.x,
				savedY + upResult.y + downResult.y,
				savedZ + horizontalResult.z);
	}

	@Unique
	private boolean betterstep$shouldTryAirStepUp(Entity self, Vec3 movement) {
		if (!(self instanceof Player player)) {
			return false;
		}

		if (player.getAbilities().flying) {
			return false;
		}

		if (this.onGround()) {
			return false;
		}

		if (!this.horizontalCollision) {
			return false;
		}

		if (this.maxUpStep() <= 0.0F) {
			return false;
		}

		double horizontalLengthSquared = movement.x * movement.x + movement.z * movement.z;

		if (horizontalLengthSquared <= BETTERSTEP_MIN_HORIZONTAL_IMPROVEMENT) {
			return false;
		}

		if (movement.y > 0.0D) {
			return false;
		}

		return true;
	}
}

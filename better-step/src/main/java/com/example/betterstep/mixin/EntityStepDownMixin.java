package com.example.betterstep.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityStepDownMixin {

	@Unique private static final double BETTERSTEP_EPSILON = 1.0E-6D;

	@Unique private static final double BETTERSTEP_PROBE_EXTRA = 1.0E-3D;

	@Unique private static final double BETTERSTEP_SAFE_FALL_MARGIN = 0.25D;

	@Shadow public boolean horizontalCollision;

	@Shadow
	public abstract boolean onGround();

	@Shadow
	public abstract float maxUpStep();

	@Inject(
			method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
			at = @At("TAIL"))
	private void betterstep$stepDownAfterVanillaMove(MoverType type, Vec3 movement, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;

		if (!this.betterstep$shouldTryStepDown(self, movement)) {
			return;
		}

		double maxStepDown = this.betterstep$getSafeStepDownDistance((Player) self);

		if (maxStepDown <= BETTERSTEP_EPSILON) {
			return;
		}

		double probeDistance = maxStepDown + BETTERSTEP_PROBE_EXTRA;
		Vec3 downAttempt = new Vec3(0.0D, -probeDistance, 0.0D);
		Vec3 downResult = ((EntityAccessor) self).betterstep$invokeCollide(downAttempt);

		if (downResult.y >= -BETTERSTEP_EPSILON) {
			return;
		}

		if (downResult.y <= downAttempt.y + BETTERSTEP_EPSILON) {
			return;
		}

		if (downResult.y < -maxStepDown - BETTERSTEP_EPSILON) {
			return;
		}

		self.setPos(self.getX(), self.getY() + downResult.y, self.getZ());
	}

	@Unique
	private boolean betterstep$shouldTryStepDown(Entity self, Vec3 movement) {
		if (!(self instanceof Player)) {
			return false;
		}

		if (!this.onGround()) {
			return false;
		}

		if (this.horizontalCollision) {
			return false;
		}

		if (this.maxUpStep() <= 0.0F) {
			return false;
		}

		double horizontalLengthSquared = movement.x * movement.x + movement.z * movement.z;

		if (horizontalLengthSquared <= 1.0E-7D) {
			return false;
		}

		if (movement.y > 0.0D) {
			return false;
		}

		return true;
	}

	@Unique
	private double betterstep$getSafeStepDownDistance(Player player) {
		if (player.isCreative()) {
			return this.maxUpStep();
		}

		double safeFallDistance = player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
		double safeStepDown = Math.max(0.0D, safeFallDistance - BETTERSTEP_SAFE_FALL_MARGIN);

		return Math.min(this.maxUpStep(), safeStepDown);
	}
}

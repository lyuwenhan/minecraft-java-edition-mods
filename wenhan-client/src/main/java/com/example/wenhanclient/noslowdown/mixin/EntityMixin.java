package com.example.wenhanclient.noslowdown.mixin;

import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow protected Vec3 stuckSpeedMultiplier;

	@Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
	private void noSlowdown$keepNormalBlockSpeed(CallbackInfoReturnable<Float> cir) {
		if (NoSlowdownSubMod.shouldCancelBlockSlowdown((Entity) (Object) this)
				&& cir.getReturnValueF() < 1.0F) {
			cir.setReturnValue(1.0F);
		}
	}

	@Inject(method = "getBlockJumpFactor", at = @At("RETURN"), cancellable = true)
	private void noSlowdown$keepNormalBlockJump(CallbackInfoReturnable<Float> cir) {
		if (NoSlowdownSubMod.shouldCancelBlockSlowdown((Entity) (Object) this)
				&& cir.getReturnValueF() < 1.0F) {
			cir.setReturnValue(1.0F);
		}
	}

	@Redirect(
			method = "move",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/phys/Vec3;multiply(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 noSlowdown$applyOnlySlowFallFromStuckBlocks(Vec3 movement, Vec3 speedMultiplier) {
		if (NoSlowdownSubMod.shouldCancelBlockSlowdown((Entity) (Object) this)) {
			double verticalMultiplier =
					shouldKeepSlowFall(movement, speedMultiplier) ? speedMultiplier.y : 1.0D;
			return movement.multiply(new Vec3(1.0D, verticalMultiplier, 1.0D));
		}
		return movement.multiply(speedMultiplier);
	}

	@Redirect(
			method = "move",
			at =
					@At(
							value = "FIELD",
							target =
									"Lnet/minecraft/world/entity/Entity;stuckSpeedMultiplier:Lnet/minecraft/world/phys/Vec3;",
							opcode = Opcodes.PUTFIELD))
	private void noSlowdown$deferStuckBlockMultiplierReset(Entity entity, Vec3 value) {
		if (!NoSlowdownSubMod.shouldCancelBlockSlowdown(entity)) {
			this.stuckSpeedMultiplier = value;
		}
	}

	@Redirect(
			method = "move",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
							ordinal = 0))
	private void noSlowdown$replaceStuckBlockVelocityReset(Entity entity, Vec3 movement) {
		if (!NoSlowdownSubMod.shouldCancelBlockSlowdown(entity)) {
			entity.setDeltaMovement(movement);
			return;
		}

		Vec3 velocity = entity.getDeltaMovement();
		Vec3 speedMultiplier = this.stuckSpeedMultiplier;
		this.stuckSpeedMultiplier = Vec3.ZERO;

		if (this.noSlowdown$shouldPreserveOriginalSlowFall(velocity, speedMultiplier)) {
			entity.setDeltaMovement(new Vec3(velocity.x, 0.0D, velocity.z));
		}
	}

	private boolean noSlowdown$shouldPreserveOriginalSlowFall(Vec3 velocity, Vec3 speedMultiplier) {
		return velocity.y < 0.0D && speedMultiplier.y > 0.0D && speedMultiplier.y < 1.0D;
	}

	private boolean shouldKeepSlowFall(Vec3 movement, Vec3 speedMultiplier) {
		return movement.y < 0.0D && speedMultiplier.y > 0.0D && speedMultiplier.y < 1.0D;
	}
}

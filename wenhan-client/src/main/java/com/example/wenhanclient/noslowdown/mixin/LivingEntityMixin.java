package com.example.wenhanclient.noslowdown.mixin;

import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow
	private static float computeModifiedFriction(float friction, float frictionModifier) {
		throw new AssertionError();
	}

	@Shadow
	private float getFrictionInfluencedSpeed(float friction) {
		throw new AssertionError();
	}

	@Redirect(
			method = "travelInAir",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
	private float noSlowdown$useNormalFrictionOnSlimeBlock(Block block) {
		if (this.noSlowdown$enabledForLocalPlayer() && block == Blocks.SLIME_BLOCK) {
			return 0.6F;
		}
		return block.getFriction();
	}

	@ModifyArg(
			method = "travelInWater",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
			index = 0)
	private float noSlowdown$useLandAccelerationInWater(float originalSpeed) {
		return this.noSlowdown$landAccelerationOr(originalSpeed);
	}

	@ModifyArg(
			method = "travelInLava",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
			index = 0)
	private float noSlowdown$useLandAccelerationInLava(float originalSpeed) {
		return this.noSlowdown$landAccelerationOr(originalSpeed);
	}

	@ModifyArg(
			method = "travelInWater",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"),
			index = 0)
	private double noSlowdown$useLandWaterXDrag(double originalDrag) {
		return this.noSlowdown$landHorizontalDragOr(originalDrag);
	}

	@ModifyArg(
			method = "travelInWater",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"),
			index = 2)
	private double noSlowdown$useLandWaterZDrag(double originalDrag) {
		return this.noSlowdown$landHorizontalDragOr(originalDrag);
	}

	@ModifyArg(
			method = "travelInLava",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"),
			index = 0)
	private double noSlowdown$useLandShallowLavaXDrag(double originalDrag) {
		return this.noSlowdown$landHorizontalDragOr(originalDrag);
	}

	@ModifyArg(
			method = "travelInLava",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"),
			index = 2)
	private double noSlowdown$useLandShallowLavaZDrag(double originalDrag) {
		return this.noSlowdown$landHorizontalDragOr(originalDrag);
	}

	@Redirect(
			method = "travelInLava",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 noSlowdown$useLandDeepLavaHorizontalDrag(Vec3 movement, double originalDrag) {
		if (!this.noSlowdown$shouldUseLandFluidMovement()) {
			return movement.scale(originalDrag);
		}
		double horizontalDrag = this.noSlowdown$landHorizontalDrag();
		return movement.multiply(horizontalDrag, originalDrag, horizontalDrag);
	}

	private boolean noSlowdown$enabledForLocalPlayer() {
		return NoSlowdownSubMod.shouldCancelBlockSlowdown((Entity) (Object) this);
	}

	private float noSlowdown$landAccelerationOr(float originalSpeed) {
		if (!this.noSlowdown$shouldUseLandFluidMovement()) {
			return originalSpeed;
		}
		return this.getFrictionInfluencedSpeed(this.noSlowdown$landFriction());
	}

	private double noSlowdown$landHorizontalDragOr(double originalDrag) {
		if (!this.noSlowdown$shouldUseLandFluidMovement()) {
			return originalDrag;
		}
		return this.noSlowdown$landHorizontalDrag();
	}

	private boolean noSlowdown$shouldUseLandFluidMovement() {
		return this.noSlowdown$enabledForLocalPlayer() && ((LivingEntity) (Object) this).onGround();
	}

	private double noSlowdown$landHorizontalDrag() {
		return this.noSlowdown$landFriction() * 0.91F;
	}

	private float noSlowdown$landFriction() {
		LivingEntity entity = (LivingEntity) (Object) this;
		Block block =
				entity.level()
						.getBlockState(entity.getBlockPosBelowThatAffectsMyMovement())
						.getBlock();
		float friction = block == Blocks.SLIME_BLOCK ? 0.6F : block.getFriction();
		return computeModifiedFriction(
				friction, (float) entity.getAttributeValue(Attributes.FRICTION_MODIFIER));
	}
}

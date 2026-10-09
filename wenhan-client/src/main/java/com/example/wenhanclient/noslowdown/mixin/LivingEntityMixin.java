package com.example.wenhanclient.noslowdown.mixin;

import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
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

	@Inject(method = "getWaterSlowDown", at = @At("HEAD"), cancellable = true)
	private void noSlowdown$keepNormalWaterWalkingDrag(CallbackInfoReturnable<Float> cir) {
		if (this.noSlowdown$enabledForLocalPlayer() && ((LivingEntity) (Object) this).onGround()) {
			cir.setReturnValue(0.91F);
		}
	}

	@ModifyConstant(method = "travelInWater", constant = @Constant(floatValue = 0.9F))
	private float noSlowdown$keepNormalSprintingWaterWalkingDrag(float value) {
		if (this.noSlowdown$enabledForLocalPlayer() && ((LivingEntity) (Object) this).onGround()) {
			return 0.91F;
		}
		return value;
	}

	@ModifyConstant(method = "travelInLava", constant = @Constant(doubleValue = 0.5D))
	private double noSlowdown$keepNormalLavaWalkingDrag(double value) {
		if (this.noSlowdown$enabledForLocalPlayer() && ((LivingEntity) (Object) this).onGround()) {
			return 0.91D;
		}
		return value;
	}

	private boolean noSlowdown$enabledForLocalPlayer() {
		return NoSlowdownSubMod.shouldCancelBlockSlowdown((Entity) (Object) this);
	}
}

package com.example.doublejump.mixin;

import com.example.doublejump.DoubleJumpConfig;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Unique private int doubleJump$airJumpsPerformed;
	@Unique private boolean doubleJump$pendingAirJump;
	@Unique private boolean doubleJump$pendingGroundJump;
	@Unique private boolean doubleJump$pendingCoyoteJump;
	@Unique private int doubleJump$cooldownTicksRemaining;
	@Unique private int doubleJump$coyoteTicksRemaining;
	@Unique private boolean doubleJump$wasOnGround;

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void doubleJump$beginAiStep(CallbackInfo callbackInfo) {
		LivingEntity entity = (LivingEntity) (Object) this;
		doubleJump$pendingAirJump = false;
		doubleJump$pendingGroundJump = false;
		doubleJump$pendingCoyoteJump = false;

		if (!(entity instanceof LocalPlayer)) {
			return;
		}

		if (entity.onGround()) {
			doubleJump$airJumpsPerformed = 0;
			doubleJump$cooldownTicksRemaining = 0;
			doubleJump$coyoteTicksRemaining = DoubleJumpConfig.coyoteTimeTicks();
			doubleJump$wasOnGround = true;
			return;
		}

		if (doubleJump$wasOnGround) {
			doubleJump$wasOnGround = false;
		} else if (doubleJump$coyoteTicksRemaining > 0) {
			doubleJump$coyoteTicksRemaining--;
		}

		if (doubleJump$cooldownTicksRemaining > 0) {
			doubleJump$cooldownTicksRemaining--;
		}
	}

	@Redirect(
			method = "aiStep",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z",
							ordinal = 2))
	private boolean doubleJump$allowConfiguredAirJump(LivingEntity entity) {
		boolean onGround = entity.onGround();
		if (onGround) {
			if (entity instanceof LocalPlayer && DoubleJumpConfig.enabled()) {
				doubleJump$pendingGroundJump = true;
			}

			return true;
		}

		if (!(entity instanceof LocalPlayer player)) {
			return false;
		}

		if (!DoubleJumpConfig.enabled()) {
			return false;
		}

		if (doubleJump$canCoyoteJump(player)) {
			doubleJump$pendingCoyoteJump = true;
			return true;
		}

		if (!doubleJump$canAirJump(player)) {
			return false;
		}

		doubleJump$pendingAirJump = true;
		return true;
	}

	@Inject(
			method = "aiStep",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V",
							shift = At.Shift.AFTER))
	private void doubleJump$recordJump(CallbackInfo callbackInfo) {
		if (doubleJump$pendingGroundJump) {
			doubleJump$coyoteTicksRemaining = 0;
			doubleJump$wasOnGround = false;
		}

		if (doubleJump$pendingCoyoteJump) {
			doubleJump$coyoteTicksRemaining = 0;
		}

		if (doubleJump$pendingAirJump) {
			doubleJump$airJumpsPerformed++;
		}

		if ((doubleJump$pendingGroundJump
						|| doubleJump$pendingCoyoteJump
						|| doubleJump$pendingAirJump)
				&& DoubleJumpConfig.cooldownEnabled()) {
			doubleJump$cooldownTicksRemaining = DoubleJumpConfig.cooldownTicks();
		}

		doubleJump$pendingGroundJump = false;
		doubleJump$pendingCoyoteJump = false;
		doubleJump$pendingAirJump = false;
	}

	@Unique
	private boolean doubleJump$canCoyoteJump(LocalPlayer player) {
		if (doubleJump$coyoteTicksRemaining <= 0) {
			return false;
		}

		return doubleJump$canPerformConfiguredJump(player);
	}

	@Unique
	private boolean doubleJump$canAirJump(LocalPlayer player) {
		if (!doubleJump$canPerformConfiguredJump(player)) {
			return false;
		}

		if (DoubleJumpConfig.infiniteJumps()) {
			return true;
		}

		int maximumAirJumps = DoubleJumpConfig.jumpCount() - 1;
		return doubleJump$airJumpsPerformed < maximumAirJumps;
	}

	@Unique
	private boolean doubleJump$canPerformConfiguredJump(LocalPlayer player) {
		if (player.isPassenger()) {
			return false;
		}

		if (player.isFallFlying()) {
			return false;
		}

		if (player.isInWater() || player.isInLava()) {
			return false;
		}

		if (player.getAbilities().flying) {
			return false;
		}

		return !DoubleJumpConfig.cooldownEnabled() || doubleJump$cooldownTicksRemaining <= 0;
	}
}

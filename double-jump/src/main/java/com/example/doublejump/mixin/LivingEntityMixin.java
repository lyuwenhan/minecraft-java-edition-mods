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

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void doubleJump$beginAiStep(CallbackInfo callbackInfo) {
		LivingEntity entity = (LivingEntity) (Object) this;
		doubleJump$pendingAirJump = false;

		if (!(entity instanceof LocalPlayer)) {
			return;
		}

		if (entity.onGround()) {
			doubleJump$airJumpsPerformed = 0;
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
			return true;
		}

		if (!(entity instanceof LocalPlayer player)) {
			return false;
		}

		if (!DoubleJumpConfig.enabled()) {
			return false;
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
	private void doubleJump$recordAirJump(CallbackInfo callbackInfo) {
		if (!doubleJump$pendingAirJump) {
			return;
		}

		doubleJump$airJumpsPerformed++;
		doubleJump$pendingAirJump = false;
	}

	@Unique
	private boolean doubleJump$canAirJump(LocalPlayer player) {
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

		if (DoubleJumpConfig.infiniteJumps()) {
			return true;
		}

		int maximumAirJumps = DoubleJumpConfig.jumpCount() - 1;
		return doubleJump$airJumpsPerformed < maximumAirJumps;
	}
}

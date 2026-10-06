package com.example.wenhanclient.interactiondelay.mixin;

import com.example.wenhanclient.interactiondelay.InteractionDelaySubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow public Options options;

	@Shadow public HitResult hitResult;

	@Shadow private int rightClickDelay;

	@Inject(method = "tick", at = @At("HEAD"))
	private void interactionDelay$resetReleasedKeys(CallbackInfo ci) {
		if (!InteractionDelaySubMod.isMiningDelayEnabled() || !this.options.keyAttack.isDown()) {
			InteractionDelaySubMod.resetMiningDelay();
		}
		if (!InteractionDelaySubMod.isPlacingDelayEnabled() || !this.options.keyUse.isDown()) {
			InteractionDelaySubMod.resetPlacingDelay();
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"))
	private void interactionDelay$resetStoppedMining(boolean attackDown, CallbackInfo ci) {
		if (!attackDown) {
			InteractionDelaySubMod.resetMiningDelay();
		}
	}

	@Inject(method = "startUseItem", at = @At("RETURN"))
	private void interactionDelay$applyPlacingDelay(CallbackInfo ci) {
		if (!InteractionDelaySubMod.isPlacingDelayEnabled()
				|| !this.options.keyUse.isDown()
				|| !(this.hitResult instanceof BlockHitResult)
				|| this.rightClickDelay <= 1) {
			return;
		}

		this.rightClickDelay =
				Math.min(this.rightClickDelay, InteractionDelaySubMod.nextPlacingDelay());
	}
}

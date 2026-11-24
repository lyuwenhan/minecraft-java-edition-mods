package com.example.wenhanclient.autoaim.mixin;

import com.example.wenhanclient.autoaim.AutoAimSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow private double accumulatedDX;

	@Shadow private double accumulatedDY;

	@Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
	private void autoAim$cancelMouseMovementWhileAiming(
			double frameTime, CallbackInfo callbackInfo) {
		AutoAimSubMod.recordMouseMovement(accumulatedDX, accumulatedDY);
		if (AutoAimSubMod.shouldCancelMouseMovement(Minecraft.getInstance())) {
			callbackInfo.cancel();
		}
	}
}

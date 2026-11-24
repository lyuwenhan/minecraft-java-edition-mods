package com.example.wenhanclient.autoaim.mixin;

import com.example.wenhanclient.autoaim.AutoAimSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow public HitResult hitResult;

	@Shadow public Entity crosshairPickEntity;

	@Inject(method = "pick", at = @At("TAIL"))
	private void autoAim$replaceCrosshairTarget(float tickDelta, CallbackInfo callbackInfo) {
		Minecraft client = (Minecraft) (Object) this;
		LivingEntity target = AutoAimSubMod.getCrosshairTarget(client);
		if (target == null) {
			return;
		}

		this.hitResult = new EntityHitResult(target, target.getEyePosition(tickDelta));
		this.crosshairPickEntity = target;
	}

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void autoAim$attackLockedTargetThroughWalls(
			CallbackInfoReturnable<Boolean> callbackInfo) {
		if (AutoAimSubMod.tryAttackLockedTarget((Minecraft) (Object) this)) {
			callbackInfo.setReturnValue(false);
		}
	}
}

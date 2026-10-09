package com.example.wenhanclient.whoiam.mixin;

import com.example.wenhanclient.whoiam.WhoIAmSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	@Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
	private void whoIAm$showLocalPlayerName(
			LivingEntity entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
		if (!WhoIAmSubMod.shouldShowLocalPlayerName()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.player == null || entity != client.player) {
			return;
		}

		boolean thirdPerson = !client.options.getCameraType().isFirstPerson();
		boolean detachedCamera =
				client.getCameraEntity() != null && client.getCameraEntity() != client.player;
		if (thirdPerson || detachedCamera) {
			cir.setReturnValue(true);
		}
	}
}

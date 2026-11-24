package com.example.wenhanclient.elytracancel.mixin;

import com.example.wenhanclient.elytracancel.ElytraCancelSubMod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@WrapOperation(
			method = "aiStep",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"))
	private boolean elytraCancel$stopFallFlyingOnJump(
			LocalPlayer player, Operation<Boolean> original) {
		if (ElytraCancelSubMod.tryCancelFallFlying(player)) {
			return false;
		}
		return original.call(player);
	}
}

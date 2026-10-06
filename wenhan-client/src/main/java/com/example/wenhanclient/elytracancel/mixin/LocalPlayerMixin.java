package com.example.wenhanclient.elytracancel.mixin;

import com.example.wenhanclient.elytracancel.ElytraCancelSubMod;

import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Redirect(
			method = "aiStep",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"))
	private boolean elytraCancel$stopFallFlyingOnJump(LocalPlayer player) {
		return ElytraCancelSubMod.handleFallFlyingJump(player);
	}
}

package com.example.wenhanclient.instantrespawn.mixin;

import com.example.wenhanclient.instantrespawn.InstantRespawnSubMod;

import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
	@Shadow @Final private LocalPlayer player;

	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private void instantRespawn$respawnImmediately(CallbackInfo ci) {
		if (!InstantRespawnSubMod.isEnabled()) {
			return;
		}
		player.respawn();
		ci.cancel();
	}
}

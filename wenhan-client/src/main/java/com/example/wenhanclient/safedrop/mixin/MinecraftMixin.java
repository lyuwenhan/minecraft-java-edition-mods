package com.example.wenhanclient.safedrop.mixin;

import com.example.wenhanclient.safedrop.SafeDropSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Redirect(
			method = "handleKeybinds",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;dropItem(Lnet/minecraft/client/player/LocalPlayer;Z)V"))
	private void safeDrop$confirmDrop(
			MultiPlayerGameMode gameMode, LocalPlayer player, boolean dropAll) {
		if (!SafeDropSubMod.shouldBlockDrop((Minecraft) (Object) this, player, dropAll)) {
			gameMode.dropItem(player, dropAll);
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void safeDrop$validateConfirmation(CallbackInfo ci) {
		SafeDropSubMod.validateConfirmation((Minecraft) (Object) this);
	}
}

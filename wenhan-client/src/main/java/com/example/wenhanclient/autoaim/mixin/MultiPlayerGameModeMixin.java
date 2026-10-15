package com.example.wenhanclient.autoaim.mixin;

import com.example.wenhanclient.autoaim.AutoAimSubMod;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Inject(method = "attack", at = @At("TAIL"))
	private void autoAim$recordAttackedTarget(
			Player player, Entity target, CallbackInfo callbackInfo) {
		AutoAimSubMod.recordAttack(target);
	}
}

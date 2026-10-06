package com.example.wenhanclient.forwardlock.mixin;

import com.example.wenhanclient.forwardlock.ForwardLockSubMod;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
abstract class KeyMappingMixin {
	@Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
	private void forwardLock$forceLockedKeys(CallbackInfoReturnable<Boolean> cir) {
		if (!ForwardLockSubMod.isLocked()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}

		KeyMapping self = (KeyMapping) (Object) this;
		if (self == client.options.keyUp) {
			cir.setReturnValue(true);
		} else if (self == client.options.keySprint
				&& ForwardLockSubMod.shouldKeepSprinting(player)) {
			cir.setReturnValue(true);
		}
	}
}

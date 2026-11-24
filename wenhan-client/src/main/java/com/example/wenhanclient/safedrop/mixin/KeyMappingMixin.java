package com.example.wenhanclient.safedrop.mixin;

import com.example.wenhanclient.safedrop.SafeDropSubMod;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
	@Inject(method = "setDown", at = @At("HEAD"))
	private void safeDrop$trackRelease(boolean down, CallbackInfo ci) {
		if (!down) {
			Minecraft client = Minecraft.getInstance();
			if (client != null
					&& client.options != null
					&& (Object) this == client.options.keyDrop) {
				SafeDropSubMod.onDropKeyReleased();
			}
		}
	}
}

package com.example.wenhanclient.whoiam.mixin;

import com.example.wenhanclient.whoiam.WhoIAmSubMod;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftTitleMixin {
	@Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
	private void whoIAm$appendPlayerName(CallbackInfoReturnable<String> cir) {
		if (!WhoIAmSubMod.shouldAppendPlayerNameToTitle()) {
			return;
		}

		Minecraft client = (Minecraft) (Object) this;
		String playerName = client.getUser().getName();
		cir.setReturnValue(cir.getReturnValue() + " - " + playerName);
	}
}

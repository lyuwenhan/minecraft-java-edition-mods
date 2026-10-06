package com.example.wenhanclient.cancelslowdown.mixin;

import com.example.wenhanclient.cancelslowdown.CancelSlowdownSubMod;

import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Redirect(
			method = "modifyInput",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/player/LocalPlayer;itemUseSpeedMultiplier()F"))
	private float cancelSlowdown$keepUseItemInputSpeed(LocalPlayer player) {
		if (CancelSlowdownSubMod.shouldCancel(player.getUseItem())) {
			return 1.0F;
		}
		return this.itemUseSpeedMultiplier();
	}

	@Inject(method = "isSlowDueToUsingItem", at = @At("HEAD"), cancellable = true)
	private void cancelSlowdown$allowSprintingWhileUsingItem(CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (CancelSlowdownSubMod.shouldCancel(player.getUseItem())) {
			cir.setReturnValue(false);
		}
	}

	@Shadow
	private float itemUseSpeedMultiplier() {
		throw new AssertionError();
	}
}

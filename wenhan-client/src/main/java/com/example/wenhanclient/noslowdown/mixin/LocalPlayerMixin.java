package com.example.wenhanclient.noslowdown.mixin;

import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;

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
	private float noSlowdown$keepUseItemInputSpeed(LocalPlayer player) {
		if (NoSlowdownSubMod.shouldCancel(player.getUseItem())) {
			return 1.0F;
		}
		return this.itemUseSpeedMultiplier();
	}

	@Inject(method = "isSlowDueToUsingItem", at = @At("HEAD"), cancellable = true)
	private void noSlowdown$allowSprintingWhileUsingItem(CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (NoSlowdownSubMod.shouldCancel(player.getUseItem())) {
			cir.setReturnValue(false);
		}
	}

	@Redirect(
			method = "isSprintingPossible",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/player/LocalPlayer;isMobilityRestricted()Z"))
	private boolean noSlowdown$allowSprintingWhileBlind(LocalPlayer player) {
		if (NoSlowdownSubMod.isEnabled()) {
			return false;
		}
		return player.isMobilityRestricted();
	}

	@Redirect(
			method = "isSprintingPossible",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/player/LocalPlayer;hasEnoughFoodToDoExhaustiveManoeuvres()Z"))
	private boolean noSlowdown$allowSprintingWhileHungry(LocalPlayer player) {
		if (NoSlowdownSubMod.isEnabled()) {
			return true;
		}
		return player.getFoodData().hasEnoughFood() || player.getAbilities().mayfly;
	}

	@Shadow
	private float itemUseSpeedMultiplier() {
		throw new AssertionError();
	}
}

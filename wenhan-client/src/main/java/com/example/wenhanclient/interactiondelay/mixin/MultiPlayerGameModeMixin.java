package com.example.wenhanclient.interactiondelay.mixin;

import com.example.wenhanclient.interactiondelay.InteractionDelaySubMod;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Shadow private int destroyDelay;

	@Inject(method = "continueDestroyBlock", at = @At("RETURN"))
	private void interactionDelay$applyMiningDelay(
			BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()
				|| !InteractionDelaySubMod.isMiningDelayEnabled()
				|| this.destroyDelay <= 1) {
			return;
		}

		this.destroyDelay = Math.min(this.destroyDelay, InteractionDelaySubMod.nextMiningDelay());
	}
}

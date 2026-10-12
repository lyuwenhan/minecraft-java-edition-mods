package com.example.wenhanclient.stablemining.mixin;

import com.example.wenhanclient.stablemining.StableMiningSubMod;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Inject(method = "startDestroyBlock", at = @At("HEAD"))
	private void stableMining$recordStartedMining(
			BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		StableMiningSubMod.recordMiningBlock(pos);
	}

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"))
	private void stableMining$recordContinuedMining(
			BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		StableMiningSubMod.recordMiningBlock(pos);
	}
}

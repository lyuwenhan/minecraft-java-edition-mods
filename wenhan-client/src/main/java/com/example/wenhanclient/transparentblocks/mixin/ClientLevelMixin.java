package com.example.wenhanclient.transparentblocks.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Inject(method = "getMarkerParticleTarget", at = @At("HEAD"), cancellable = true)
	private void transparentBlocks$disableBarrierAndLightMarkerParticles(
			CallbackInfoReturnable<Block> cir) {
		cir.setReturnValue(null);
	}
}

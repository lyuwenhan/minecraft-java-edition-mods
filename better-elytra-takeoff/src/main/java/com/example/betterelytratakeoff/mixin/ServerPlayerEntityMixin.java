package com.example.betterelytratakeoff.mixin;

import com.example.betterelytratakeoff.BetterElytraTakeoffState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Inject(method = "playerTick", at = @At("TAIL"))
	private void betterElytraTakeoff$tickTakeoff(CallbackInfo ci) {
		BetterElytraTakeoffState.tick((ServerPlayerEntity)(Object)this);
	}
}

package com.example.servermanager.mixin;

import com.example.servermanager.web.BackendService;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerGameRuleMixin {
	@Inject(
			method =
					"onGameRuleChanged(Lnet/minecraft/world/level/gamerules/GameRule;Ljava/lang/Object;)V",
			at = @At("TAIL"))
	private <T> void serverManager$onGameRuleChanged(GameRule<T> rule, T value, CallbackInfo ci) {
		BackendService.onGameRuleChanged((MinecraftServer) (Object) this, rule, value);
	}
}

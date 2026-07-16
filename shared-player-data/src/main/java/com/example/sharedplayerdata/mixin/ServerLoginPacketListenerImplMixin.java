package com.example.sharedplayerdata.mixin;

import com.example.sharedplayerdata.SharedPlayerDataMod;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
	@Inject(method = "verifyLoginAndFinishConnectionSetup", at = @At("HEAD"))
	private void sharedplayerdata$enterLoginScope(GameProfile gameProfile, CallbackInfo ci) {
		SharedPlayerDataMod.MANAGER.enterLogin((ServerLoginPacketListenerImpl) (Object) this);
	}

	@Inject(method = "verifyLoginAndFinishConnectionSetup", at = @At("RETURN"))
	private void sharedplayerdata$exitLoginScope(GameProfile gameProfile, CallbackInfo ci) {
		SharedPlayerDataMod.MANAGER.exitLogin((ServerLoginPacketListenerImpl) (Object) this);
	}
}

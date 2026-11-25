package com.example.seedgen.mixin;

import com.example.seedgen.ClientWorldSeeds;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
	@Inject(method = "handleLogin", at = @At("HEAD"))
	private void seedgen$recordLoginWorld(
			ClientboundLoginPacket packet, CallbackInfo callback) {
		ClientWorldSeeds.onSpawnInfo(packet.commonPlayerSpawnInfo().seed());
	}

	@Inject(method = "handleRespawn", at = @At("HEAD"))
	private void seedgen$recordRespawnWorld(
			ClientboundRespawnPacket packet, CallbackInfo callback) {
		ClientWorldSeeds.onSpawnInfo(packet.commonPlayerSpawnInfo().seed());
	}
}

package com.example.clientflying.mixin;

import com.example.clientflying.ClientFlyingMod;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
	private void clientflying$onSend(Packet<?> packet, CallbackInfo ci) {
		if (ClientFlyingMod.isSendingInternalStartFallFlyingPacket()) {
			return;
		}

		if (!(packet instanceof ServerboundPlayerCommandPacket commandPacket)) {
			return;
		}

		if (commandPacket.getAction() != ServerboundPlayerCommandPacket.Action.START_FALL_FLYING) {
			return;
		}

		ClientFlyingMod.onClientStartFallFlyingPacket();
	}
}

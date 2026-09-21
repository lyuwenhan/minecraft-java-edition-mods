package com.example.clientflying.mixin;

import com.example.clientflying.ClientFlyingMod;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

	@Redirect(
			method = "aiStep",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private void clientflying$onSendFromAiStep(ClientPacketListener connection, Packet<?> packet) {

		if (packet instanceof ServerboundPlayerCommandPacket commandPacket
				&& commandPacket.getAction()
						== ServerboundPlayerCommandPacket.Action.START_FALL_FLYING) {
			ClientFlyingMod.startFallFlying();
		}

		connection.send(packet);
	}
}

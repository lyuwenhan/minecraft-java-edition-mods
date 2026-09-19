package com.example.clientflying.mixin;

import com.example.clientflying.ClientFlyingMod;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

	@ModifyVariable(
			method =
					"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
			at = @At("HEAD"),
			argsOnly = true,
			index = 1)
	private Packet<?> clientflying$replaceOutgoingPacket(Packet<?> packet) {
		if (!ClientFlyingMod.isEnabled()) {
			return packet;
		}

		if (ClientFlyingMod.isFallFlying()) {
			return packet;
		}

		if (!(packet instanceof ServerboundMovePlayerPacket movePacket)) {
			return packet;
		}

		return clientflying$copyMovePacketWithOnGroundTrue(movePacket);
	}

	private static ServerboundMovePlayerPacket clientflying$copyMovePacketWithOnGroundTrue(
			ServerboundMovePlayerPacket packet) {

		boolean horizontalCollision = packet.horizontalCollision();

		if (packet instanceof ServerboundMovePlayerPacket.Pos) {
			return new ServerboundMovePlayerPacket.Pos(
					packet.getX(0.0D),
					packet.getY(0.0D),
					packet.getZ(0.0D),
					true,
					horizontalCollision);
		}

		if (packet instanceof ServerboundMovePlayerPacket.PosRot) {
			return new ServerboundMovePlayerPacket.PosRot(
					packet.getX(0.0D),
					packet.getY(0.0D),
					packet.getZ(0.0D),
					packet.getYRot(0.0F),
					packet.getXRot(0.0F),
					true,
					horizontalCollision);
		}

		if (packet instanceof ServerboundMovePlayerPacket.Rot) {
			return new ServerboundMovePlayerPacket.Rot(
					packet.getYRot(0.0F), packet.getXRot(0.0F), true, horizontalCollision);
		}

		if (packet instanceof ServerboundMovePlayerPacket.StatusOnly) {
			return new ServerboundMovePlayerPacket.StatusOnly(true, horizontalCollision);
		}

		return packet;
	}
}

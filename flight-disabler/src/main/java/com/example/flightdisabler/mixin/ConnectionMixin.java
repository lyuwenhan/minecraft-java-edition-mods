package com.example.flightdisabler.mixin;

import com.example.flightdisabler.FlightDisablerMod;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public final class ConnectionMixin {
	@Inject(
			method = "send(Lnet/minecraft/network/protocol/Packet;)V",
			at = @At("HEAD"),
			cancellable = true)
	private void flightDisabler$cancelStartFallFlying(Packet<?> packet, CallbackInfo ci) {
		if (!FlightDisablerMod.isEnabled()) {
			return;
		}
		if (!(packet instanceof ServerboundPlayerCommandPacket commandPacket)) {
			return;
		}
		if (commandPacket.getAction() != ServerboundPlayerCommandPacket.Action.START_FALL_FLYING) {
			return;
		}
		ci.cancel();
		FlightDisablerMod.suppressFallFlyingAttempt();
	}
}

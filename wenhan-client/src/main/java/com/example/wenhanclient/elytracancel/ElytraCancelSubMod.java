package com.example.wenhanclient.elytracancel;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public final class ElytraCancelSubMod {
	private static ElytraCancelConfig config = new ElytraCancelConfig();

	private ElytraCancelSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.elytraCancel.copy();
	}

	public static ElytraCancelConfig config() {
		return config.copy();
	}

	public static void setConfig(ElytraCancelConfig newConfig) {
		config = newConfig == null ? new ElytraCancelConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.elytraCancel = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean handleFallFlyingJump(LocalPlayer player) {
		if (config.enabled && player.isFallFlying()) {
			player.stopFallFlying();
			player.connection.send(
					new ServerboundPlayerCommandPacket(
							player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
			return false;
		}
		return player.tryToStartFallFlying();
	}
}

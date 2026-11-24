package com.example.wenhanclient.clientpseudounrestrict;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class ClientPseudoUnrestrictSubMod {
	public static final double PSEUDO_INTERACTION_RANGE = 1024.0D;
	public static final float PSEUDO_DESTROY_SPEED = 1_000_000.0F;

	private static ClientPseudoUnrestrictConfig config = new ClientPseudoUnrestrictConfig();

	private ClientPseudoUnrestrictSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.clientPseudoUnrestrict.copy();
	}

	public static ClientPseudoUnrestrictConfig config() {
		return config.copy();
	}

	public static void setConfig(ClientPseudoUnrestrictConfig newConfig) {
		config = newConfig == null ? new ClientPseudoUnrestrictConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.clientPseudoUnrestrict = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isBlockInteractionRangeEnabled() {
		return config.blockInteractionRange;
	}

	public static boolean isEntityInteractionRangeEnabled() {
		return config.entityInteractionRange;
	}

	public static boolean isInstantBreakEnabled() {
		return config.instantBreak;
	}

	public static boolean isLocalPlayer(Player player) {
		if (player == null) {
			return false;
		}
		LocalPlayer localPlayer = Minecraft.getInstance().player;
		return player == localPlayer || player instanceof LocalPlayer;
	}
}

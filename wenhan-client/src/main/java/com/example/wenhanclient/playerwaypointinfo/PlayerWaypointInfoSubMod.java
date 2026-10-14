package com.example.wenhanclient.playerwaypointinfo;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterSubMod;
import com.example.wenhanclient.playerwaypointinfo.client.HudIconRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class PlayerWaypointInfoSubMod {
	private static PlayerWaypointInfoConfig config;

	private PlayerWaypointInfoSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.playerWaypointInfo.copy();
		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(
						PlayerHighlighterSubMod.MOD_ID, "player-highlighter/hud"),
				(graphics, tickCounter) -> PlayerWaypointInfoHud.render(graphics));
		HudIconRenderer.register();
	}

	public static PlayerWaypointInfoConfig config() {
		return config.copy();
	}

	public static boolean isInformationHudVisible() {
		return config != null && config.enabled;
	}

	public static void setConfig(PlayerWaypointInfoConfig newConfig) {
		config = newConfig == null ? new PlayerWaypointInfoConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.playerWaypointInfo = config.copy();
		WenhanClientMod.CONFIG.save();
	}
}

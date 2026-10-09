package com.example.wenhanclient.playerhighlighter;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.example.wenhanclient.playerhighlighter.client.HudIconRenderer;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class PlayerHighlighterSubMod {
	public static final String MOD_ID = "wenhan-client";
	private static final KeyMapping.Category CATEGORY = WenhanClientKeyCategories.GENERAL;
	public static KeyMapping TOGGLE_KEY;
	public static KeyMapping HOLD_KEY;
	public static PlayerHighlighterConfig config;

	private PlayerHighlighterSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.playerHighlighter.copy();
		TOGGLE_KEY =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.wenhan-client.player-highlighter.toggle",
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_I,
								CATEGORY,
								11));
		HOLD_KEY =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.wenhan-client.player-highlighter.hold",
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_TAB,
								CATEGORY,
								12));
		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					while (TOGGLE_KEY.consumeClick()) {
						if (!config.enabled) {
							continue;
						}
						config.keep = !config.keep;
						WenhanClientMod.CONFIG.playerHighlighter = config.copy();
						WenhanClientMod.CONFIG.save();
						if (client.player != null) {
							client.player.sendSystemMessage(
									Component.literal(
											"Keep Player Highlight: "
													+ (config.keep ? "ON" : "OFF")));
						}
					}
				});
		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MOD_ID, "player-highlighter/hud"),
				(graphics, tickCounter) -> PlayerHighlighterHud.render(graphics));
		HudIconRenderer.register();
		System.out.println("[PlayerHighlighter] Client initialized");
	}

	public static boolean isHighlightActive() {
		if (config == null || !config.enabled) {
			return false;
		}
		if (config.keep) {
			return true;
		}
		return HOLD_KEY != null && HOLD_KEY.isDown();
	}

	public static boolean isInformationHudVisible() {
		if (config == null || !config.enabled) {
			return false;
		}
		if (config.informationHud == null) {
			return true;
		}
		return config.informationHud;
	}

	public static PlayerHighlighterConfig config() {
		return config.copy();
	}

	public static void setConfig(PlayerHighlighterConfig newConfig) {
		config = newConfig == null ? new PlayerHighlighterConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.playerHighlighter = config.copy();
		WenhanClientMod.CONFIG.save();
	}
}

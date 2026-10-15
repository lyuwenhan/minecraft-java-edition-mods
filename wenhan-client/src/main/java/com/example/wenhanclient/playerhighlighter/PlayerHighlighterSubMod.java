package com.example.wenhanclient.playerhighlighter;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

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
						if (config.mode() == PlayerHighlightMode.DISABLED) {
							continue;
						}
						config.setMode(
								config.mode() == PlayerHighlightMode.ALWAYS
										? PlayerHighlightMode.HOLD
										: PlayerHighlightMode.ALWAYS);
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
	}

	public static boolean isHighlightActive() {
		if (config == null) {
			return false;
		}
		return switch (config.mode()) {
			case DISABLED -> false;
			case HOLD -> HOLD_KEY != null && HOLD_KEY.isDown();
			case ALWAYS -> true;
		};
	}

	public static PlayerHighlighterConfig config() {
		return config.copy();
	}

	public static void setConfig(PlayerHighlighterConfig newConfig) {
		config = newConfig == null ? new PlayerHighlighterConfig() : newConfig.copy();
		config.applyModeToLegacy();
		WenhanClientMod.CONFIG.playerHighlighter = config.copy();
		WenhanClientMod.CONFIG.save();
	}
}

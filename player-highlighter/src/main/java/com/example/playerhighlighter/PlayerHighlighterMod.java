package com.example.playerhighlighter;

import com.example.playerhighlighter.client.HudIconRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class PlayerHighlighterMod implements ClientModInitializer {
	public static final String MOD_ID = "player-highlighter";

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(MOD_ID, "general")
	);

	public static KeyMapping TOGGLE_KEY;
	public static KeyMapping HOLD_KEY;
	public static PlayerHighlighterConfig config;

	@Override
	public void onInitializeClient() {
		config = PlayerHighlighterConfig.load();

		TOGGLE_KEY = KeyMappingHelper.registerKeyMapping(
			new KeyMapping(
				"key.playerhighlighter.toggle",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_I,
				CATEGORY
			)
		);

		HOLD_KEY = KeyMappingHelper.registerKeyMapping(
			new KeyMapping(
				"key.playerhighlighter.hold",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_TAB,
				CATEGORY
			)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_KEY.consumeClick()) {
				config.keep = !config.keep;
				config.save();

				if (client.player != null) {
					client.player.sendSystemMessage(
						Component.literal("Keep Player Highlight: " + (config.keep ? "ON" : "OFF"))
					);
				}
			}
		});

		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CHAT,
			Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
			(graphics, tickCounter) -> PlayerHighlighterHud.render(graphics)
		);

		HudIconRenderer.register();
		System.out.println("[PlayerHighlighter] Client initialized");
	}

	public static boolean isHighlightActive() {
		if (config == null) {
			return false;
		}

		if (config.keep) {
			return true;
		}

		return HOLD_KEY != null && HOLD_KEY.isDown();
	}
}

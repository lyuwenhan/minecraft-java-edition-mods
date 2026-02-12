package com.example.playerhighlighter;

import com.example.playerhighlighter.client.HudIconRenderer;
import com.example.playerhighlighter.PlayerHighlighterConfig;

import net.minecraft.util.Identifier;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;

public class PlayerHighlighterMod implements ClientModInitializer {
	public static KeyBinding TOGGLE_KEY;
	public static KeyBinding HOLD_KEY;
	public static PlayerHighlighterConfig config;

	@Override
	public void onInitializeClient() {
		TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
				"key.playerhighlighter.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_I,
				KeyBinding.Category.MISC
			)
		);
		HOLD_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
				"key.playerhighlighter.hold",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_TAB,
				KeyBinding.Category.MISC
			)
		);

		config = PlayerHighlighterConfig.load();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_KEY.wasPressed()) {
				config.keep = !config.keep;
				config.save();

				if (client.player != null) {
					client.player.sendMessage(
						net.minecraft.text.Text.literal(
							"Keep Player Highlight: " + (config.keep ? "ON" : "OFF")
						),
						true
					);
				}
			}
		});

		HudIconRenderer.register();

		System.out.println("[PlayerHighlighter] Client initialized");
	}
}

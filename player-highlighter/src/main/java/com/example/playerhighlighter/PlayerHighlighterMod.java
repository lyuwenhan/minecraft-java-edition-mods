package com.example.playerhighlighter;

import com.example.playerhighlighter.client.HudIconRenderer;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
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

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));

	public static KeyMapping TOGGLE_KEY;
	public static KeyMapping HOLD_KEY;
	public static PlayerHighlighterConfig config;

	@Override
	public void onInitializeClient() {
		config = PlayerHighlighterConfig.load();

		TOGGLE_KEY =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.playerhighlighter.toggle",
								InputConstants.Type.KEYSYM,
								GLFW.GLFW_KEY_I,
								CATEGORY));

		HOLD_KEY =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.playerhighlighter.hold",
								InputConstants.Type.KEYSYM,
								GLFW.GLFW_KEY_TAB,
								CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					while (TOGGLE_KEY.consumeClick()) {
						config.keep = !config.keep;
						config.save();

						if (client.player != null) {
							client.player.sendSystemMessage(
									Component.literal(
											"Keep Player Highlight: "
													+ (config.keep ? "ON" : "OFF")));
						}
					}
				});

		registerCommands();

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
				(graphics, tickCounter) -> PlayerHighlighterHud.render(graphics));

		HudIconRenderer.register();
		System.out.println("[PlayerHighlighter] Client initialized");
	}

	private static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) ->
						dispatcher.register(
								ClientCommands.literal("playerhighlighter")
										.then(
												ClientCommands.literal("hud")
														.then(
																ClientCommands.literal("on")
																		.executes(
																				context ->
																						setInformationHudVisible(
																								context
																										.getSource(),
																								true)))
														.then(
																ClientCommands.literal("off")
																		.executes(
																				context ->
																						setInformationHudVisible(
																								context
																										.getSource(),
																								false)))
														.then(
																ClientCommands.literal("toggle")
																		.executes(
																				context ->
																						toggleInformationHud(
																								context
																										.getSource())))
														.then(
																ClientCommands.literal("status")
																		.executes(
																				context ->
																						showInformationHudStatus(
																								context
																										.getSource()))))));
	}

	private static int setInformationHudVisible(
			net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source,
			boolean visible) {
		config.informationHud = visible;
		config.save();
		source.sendFeedback(
				Component.literal("Player Information HUD: " + (visible ? "ON" : "OFF")));
		return 1;
	}

	private static int showInformationHudStatus(
			net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
		source.sendFeedback(
				Component.literal(
						"Player Highlighter hud "
								+ (isInformationHudVisible() ? "enabled." : "disabled.")));
		return 1;
	}

	private static int toggleInformationHud(
			net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
		return setInformationHudVisible(source, !isInformationHudVisible());
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

	public static boolean isInformationHudVisible() {
		if (config == null) {
			return true;
		}

		if (config.informationHud == null) {
			return true;
		}

		return config.informationHud;
	}
}

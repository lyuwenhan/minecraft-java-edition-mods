package com.example.hidepassword;

import com.example.hidepassword.config.HidePasswordConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HidePasswordMod implements ClientModInitializer {
	public static final String MOD_ID = "hide-password";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(MOD_ID, "general")
	);

	public static HidePasswordConfig CONFIG;

	private static Path configPath;
	private static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		configPath = FabricLoader.getInstance().getConfigDir().resolve("hide-password.json");

		loadConfig();

		LOGGER.info("HidePassword loaded, enabled={}", CONFIG.enabled);

		registerCommands();
		registerKeyMapping();
	}

	private static void registerKeyMapping() {
		toggleKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping(
				"key.hidepassword.toggle",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_F8,
				CATEGORY
			)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				CONFIG.enabled = !CONFIG.enabled;
				saveConfig();

				LOGGER.info("HidePassword enabled={}", CONFIG.enabled);

				if (client.player != null) {
					client.player.sendSystemMessage(
						Component.literal("HidePassword " + (CONFIG.enabled ? "Enabled" : "Disabled"))
					);
				}
			}
		});
	}

	private static void loadConfig() {
		try {
			if (Files.exists(configPath)) {
				CONFIG = GSON.fromJson(Files.readString(configPath), HidePasswordConfig.class);

				if (CONFIG == null) {
					CONFIG = new HidePasswordConfig();
				}

				return;
			}

			CONFIG = new HidePasswordConfig();
			saveConfig();
		} catch (Exception e) {
			CONFIG = new HidePasswordConfig();
			LOGGER.error("Failed to load config", e);
		}
	}

	public static void saveConfig() {
		try {
			Files.createDirectories(configPath.getParent());
			Files.writeString(configPath, GSON.toJson(CONFIG));
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}

	private static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
			ClientCommands.literal("hidepassword")
				.executes(context -> sendStatus(context.getSource()))
				.then(ClientCommands.literal("status")
					.executes(context -> sendStatus(context.getSource())))
				.then(ClientCommands.literal("toggle")
					.executes(context -> setEnabled(context.getSource(), !CONFIG.enabled)))
				.then(ClientCommands.literal("on")
					.executes(context -> setEnabled(context.getSource(), true)))
				.then(ClientCommands.literal("off")
					.executes(context -> setEnabled(context.getSource(), false)))
				.then(ClientCommands.literal("hide-length")
					.executes(context -> sendHideLengthStatus(context.getSource()))
					.then(ClientCommands.literal("status")
						.executes(context -> sendHideLengthStatus(context.getSource())))
					.then(ClientCommands.literal("toggle")
						.executes(context -> setHideLength(context.getSource(), !CONFIG.hideLength)))
					.then(ClientCommands.literal("on")
						.executes(context -> setHideLength(context.getSource(), true)))
					.then(ClientCommands.literal("off")
						.executes(context -> setHideLength(context.getSource(), false))))
		));
	}

	private static int setEnabled(FabricClientCommandSource source, boolean enabled) {
		CONFIG.enabled = enabled;
		saveConfig();

		source.sendFeedback(Component.literal("HidePassword " + (CONFIG.enabled ? "Enabled" : "Disabled")));
		LOGGER.info("HidePassword enabled={}", CONFIG.enabled);

		return Command.SINGLE_SUCCESS;
	}

	private static int sendStatus(FabricClientCommandSource source) {
		source.sendFeedback(Component.literal("HidePassword is " + (CONFIG.enabled ? "Enabled" : "Disabled")));
		return Command.SINGLE_SUCCESS;
	}

	private static int setHideLength(FabricClientCommandSource source, boolean hideLength) {
		CONFIG.hideLength = hideLength;
		saveConfig();

		source.sendFeedback(Component.literal("HidePassword hide length " + (CONFIG.hideLength ? "Enabled" : "Disabled")));
		LOGGER.info("HidePassword hideLength={}", CONFIG.hideLength);

		return Command.SINGLE_SUCCESS;
	}

	private static int sendHideLengthStatus(FabricClientCommandSource source) {
		source.sendFeedback(Component.literal("HidePassword hide length is " + (CONFIG.hideLength ? "Enabled" : "Disabled")));
		return Command.SINGLE_SUCCESS;
	}
}

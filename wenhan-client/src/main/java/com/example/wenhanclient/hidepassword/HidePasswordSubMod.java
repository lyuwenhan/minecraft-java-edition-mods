package com.example.wenhanclient.hidepassword;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.example.wenhanclient.hidepassword.config.HidePasswordConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HidePasswordSubMod {
	public static final String MOD_ID = "hide-password";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final KeyMapping.Category CATEGORY = WenhanClientKeyCategories.GENERAL;
	public static HidePasswordConfig CONFIG;
	private static KeyMapping toggleKey;

	private HidePasswordSubMod() {}

	public static void init() {
		loadConfig();
		LOGGER.info("HidePassword loaded, enabled={}", CONFIG.enabled);
		registerCommands();
		registerKeyMapping();
	}

	private static void registerKeyMapping() {
		toggleKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.wenhan-client.hide-password.toggle",
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_F8,
								CATEGORY,
								4));
		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					while (toggleKey.consumeClick()) {
						CONFIG.enabled = !CONFIG.enabled;
						saveConfig();
						LOGGER.info("HidePassword enabled={}", CONFIG.enabled);
						if (client.player != null) {
							client.player.sendSystemMessage(
									Component.literal(
											"HidePassword "
													+ (CONFIG.enabled ? "Enabled" : "Disabled")));
						}
					}
				});
	}

	private static void loadConfig() {
		CONFIG = WenhanClientMod.CONFIG.hidePassword.copy();
	}

	public static void saveConfig() {
		WenhanClientMod.CONFIG.hidePassword = CONFIG.copy();
		WenhanClientMod.CONFIG.save();
	}

	private static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) ->
						dispatcher.register(ClientCommands.literal("wc").then(hidePasswordNode())));
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource>
			hidePasswordNode() {
		return ClientCommands.literal("hidepassword")
				.executes(context -> sendStatus(context.getSource()))
				.then(
						ClientCommands.literal("status")
								.executes(context -> sendStatus(context.getSource())))
				.then(
						ClientCommands.literal("toggle")
								.executes(
										context ->
												setEnabled(context.getSource(), !CONFIG.enabled)))
				.then(
						ClientCommands.literal("on")
								.executes(context -> setEnabled(context.getSource(), true)))
				.then(
						ClientCommands.literal("off")
								.executes(context -> setEnabled(context.getSource(), false)))
				.then(
						ClientCommands.literal("hide-length")
								.executes(context -> sendHideLengthStatus(context.getSource()))
								.then(
										ClientCommands.literal("status")
												.executes(
														context ->
																sendHideLengthStatus(
																		context.getSource())))
								.then(
										ClientCommands.literal("toggle")
												.executes(
														context ->
																setHideLength(
																		context.getSource(),
																		!CONFIG.hideLength)))
								.then(
										ClientCommands.literal("on")
												.executes(
														context ->
																setHideLength(
																		context.getSource(), true)))
								.then(
										ClientCommands.literal("off")
												.executes(
														context ->
																setHideLength(
																		context.getSource(),
																		false))));
	}

	public static HidePasswordConfig config() {
		return CONFIG.copy();
	}

	public static void setConfig(HidePasswordConfig config) {
		CONFIG = config == null ? new HidePasswordConfig() : config.copy();
		saveConfig();
	}

	private static int setEnabled(FabricClientCommandSource source, boolean enabled) {
		CONFIG.enabled = enabled;
		saveConfig();
		source.sendFeedback(
				Component.literal("HidePassword " + (CONFIG.enabled ? "Enabled" : "Disabled")));
		LOGGER.info("HidePassword enabled={}", CONFIG.enabled);
		return Command.SINGLE_SUCCESS;
	}

	private static int sendStatus(FabricClientCommandSource source) {
		source.sendFeedback(
				Component.literal("HidePassword is " + (CONFIG.enabled ? "Enabled" : "Disabled")));
		return Command.SINGLE_SUCCESS;
	}

	private static int setHideLength(FabricClientCommandSource source, boolean hideLength) {
		CONFIG.hideLength = hideLength;
		saveConfig();
		source.sendFeedback(
				Component.literal(
						"HidePassword hide length "
								+ (CONFIG.hideLength ? "Enabled" : "Disabled")));
		LOGGER.info("HidePassword hideLength={}", CONFIG.hideLength);
		return Command.SINGLE_SUCCESS;
	}

	private static int sendHideLengthStatus(FabricClientCommandSource source) {
		source.sendFeedback(
				Component.literal(
						"HidePassword hide length is "
								+ (CONFIG.hideLength ? "Enabled" : "Disabled")));
		return Command.SINGLE_SUCCESS;
	}
}

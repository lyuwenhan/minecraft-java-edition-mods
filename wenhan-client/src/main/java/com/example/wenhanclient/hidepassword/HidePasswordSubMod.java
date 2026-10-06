package com.example.wenhanclient.hidepassword;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.example.wenhanclient.hidepassword.config.HidePasswordConfig;
import com.mojang.blaze3d.platform.InputConstants;

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

	public static HidePasswordConfig config() {
		return CONFIG.copy();
	}

	public static void setConfig(HidePasswordConfig config) {
		CONFIG = config == null ? new HidePasswordConfig() : config.copy();
		saveConfig();
	}
}

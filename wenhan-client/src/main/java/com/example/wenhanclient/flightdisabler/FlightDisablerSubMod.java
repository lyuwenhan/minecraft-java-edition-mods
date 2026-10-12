package com.example.wenhanclient.flightdisabler;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class FlightDisablerSubMod {
	public static final String MOD_ID = "wenhan-client";
	private static final String KEY_TOGGLE = "key.wenhan-client.flight-disabler.toggle";
	private static final KeyMapping.Category CATEGORY = WenhanClientKeyCategories.GENERAL;
	private static KeyMapping toggleKey;
	private static FlightDisablerConfig config = new FlightDisablerConfig();

	private FlightDisablerSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.flightDisabler.copy();
		toggleKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								KEY_TOGGLE,
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_B,
								CATEGORY,
								2));
		ClientTickEvents.END_CLIENT_TICK.register(FlightDisablerSubMod::onEndClientTick);
	}

	public static FlightDisablerConfig config() {
		return config.copy();
	}

	public static void setConfig(FlightDisablerConfig newConfig) {
		config = newConfig == null ? new FlightDisablerConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.flightDisabler = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.anyEnabled();
	}

	public static boolean isCreativeFlightDisabled() {
		return config.creativeFlightEnabled();
	}

	public static boolean isElytraFlightDisabled() {
		return config.elytraFlightEnabled();
	}

	private static void onEndClientTick(Minecraft client) {
		handleToggleKey(client);
	}

	private static void handleToggleKey(Minecraft client) {
		if (toggleKey == null) {
			return;
		}
		while (toggleKey.consumeClick()) {
			boolean newValue = !config.anyEnabled();
			config.creativeFlightEnabled = newValue;
			config.elytraFlightEnabled = newValue;
			WenhanClientMod.CONFIG.flightDisabler = config.copy();
			WenhanClientMod.CONFIG.save();
			if (client.player != null) {
				client.player.sendOverlayMessage(
						Component.literal("Flight Disabler: " + (newValue ? "ON" : "OFF")));
			}
			if (config.anyEnabled()) {
				disableFlight(client);
			}
		}
	}

	public static void disableFlight(Minecraft client) {
		if (!config.anyEnabled() || client.player == null) {
			return;
		}

		if (config.creativeFlightEnabled()) {
			client.player.getAbilities().flying = false;
		}

		if (config.elytraFlightEnabled() && client.player.isFallFlying()) {
			client.player.stopFallFlying();
		}
	}
}

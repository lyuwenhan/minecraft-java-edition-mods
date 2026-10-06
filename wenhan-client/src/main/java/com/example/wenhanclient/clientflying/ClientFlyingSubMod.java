package com.example.wenhanclient.clientflying;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

public final class ClientFlyingSubMod {
	private static final KeyMapping.Category CATEGORY = WenhanClientKeyCategories.GENERAL;
	private static KeyMapping toggleKey;
	private static ClientFlyingConfig config = new ClientFlyingConfig();
	private static boolean lastGamemode = false;

	private ClientFlyingSubMod() {}

	private static void resetState() {
		lastGamemode = false;
	}

	private static void handleToggleKey(Minecraft client) {
		if (toggleKey == null || client.player == null) {
			return;
		}
		boolean changed = false;
		while (toggleKey.consumeClick()) {
			config.enabled = !config.enabled;
			changed = true;
		}
		if (changed) {
			WenhanClientMod.CONFIG.clientFlying = config.copy();
			WenhanClientMod.CONFIG.save();
			client.player.sendOverlayMessage(
					Component.literal("Client Flying: " + (config.enabled ? "ON" : "OFF")));
		}
	}

	public static ClientFlyingConfig config() {
		return config.copy();
	}

	public static void setConfig(ClientFlyingConfig newConfig) {
		config = newConfig == null ? new ClientFlyingConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.clientFlying = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled && lastGamemode;
	}

	public static void init() {
		System.out.println("[ClientFlying] Client initialized");
		config = WenhanClientMod.CONFIG.clientFlying.copy();
		toggleKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.wenhan-client.client-flying.toggle",
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_V,
								CATEGORY,
								1));
		ClientPlayConnectionEvents.JOIN.register(
				(handler, sender, client) -> {
					resetState();
					System.out.println("[ClientFlying] State reset on join");
				});
		ClientPlayConnectionEvents.DISCONNECT.register(
				(handler, client) -> {
					resetState();
					System.out.println("[ClientFlying] State reset on disconnect");
				});
		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					if (client.getConnection() == null) {
						return;
					}
					if (client.player == null || client.gameMode == null) {
						return;
					}
					handleToggleKey(client);
					GameType gameMode = client.gameMode.getPlayerMode();
					lastGamemode = gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE;
					if (lastGamemode) {
						if (config.enabled) {
							if (client.player.isFallFlying()) {
								client.player.stopFallFlying();
							}
							client.player.getAbilities().mayfly =
									client.player.getAbilities().flying = true;
						} else {
							client.player.getAbilities().mayfly =
									client.player.getAbilities().flying = false;
						}
					}
				});
	}
}

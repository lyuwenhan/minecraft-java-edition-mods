package com.example.wenhanclient.clientflying;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

public final class ClientFlyingSubMod {
	private static final KeyMapping.Category CATEGORY = WenhanClientKeyCategories.GENERAL;
	private static KeyMapping toggleKey;
	private static ClientFlyingConfig config = new ClientFlyingConfig();
	private static boolean lastGlider = false;
	private static boolean lastFlying = false;
	private static boolean lastFallFlying = false;
	private static boolean lastGamemode = false;
	private static boolean lastEnabled = false;
	private static int startFallFlyingResendTicks = 0;

	private ClientFlyingSubMod() {}

	private static void resetState() {
		lastGlider = false;
		lastFlying = false;
		lastFallFlying = false;
		lastGamemode = false;
		lastEnabled = false;
		startFallFlyingResendTicks = 0;
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

	public static boolean isFallFlying() {
		return startFallFlyingResendTicks > 0 || lastFallFlying;
	}

	public static void startFallFlying() {
		startFallFlyingResendTicks = 10;
	}

	private static void startFlying(Minecraft client, boolean wearingGlider, boolean fallFlying) {
		if (client.player == null) {
			return;
		}
		if (fallFlying) {
			return;
		} else if (wearingGlider) {
			startFallFlying();
		} else {
			client.player.getAbilities().flying = true;
		}
	}

	private static void sendInternalStartFallFlyingPacket(
			Minecraft client, ClientPacketListener connection) {
		if (client.player == null) {
			return;
		}
		connection.send(
				new ServerboundPlayerCommandPacket(
						client.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
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
					ClientPacketListener connection = client.getConnection();
					if (connection == null) {
						return;
					}
					if (client.player == null || client.gameMode == null) {
						return;
					}
					handleToggleKey(client);
					GameType gameMode = client.gameMode.getPlayerMode();
					boolean newGamemode =
							gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE;
					boolean enabled = config.enabled;
					boolean onground = client.player.onGround();
					ItemStack chestStack = client.player.getItemBySlot(EquipmentSlot.CHEST);
					boolean wearingGlider = chestStack.get(DataComponents.GLIDER) != null;
					boolean flying = client.player.getAbilities().flying;
					boolean fallFlying = client.player.isFallFlying();
					if (newGamemode) {
						if (enabled) {
							client.player.getAbilities().mayfly = true;
							if (!lastEnabled || !lastGamemode) {
								startFlying(client, wearingGlider, fallFlying);
								flying = client.player.getAbilities().flying;
							}
							if (lastGlider && !wearingGlider && lastFallFlying) {
								System.out.println("[ClientFlying] set flying");
								client.player.stopFallFlying();
								client.player.getAbilities().flying = flying = true;
								fallFlying = client.player.isFallFlying();
							}
							if (wearingGlider && !lastGlider) {
								System.out.println("[ClientFlying] set fall flying");
								client.player.getAbilities().flying = flying = false;
								startFallFlying();
							}
						} else {
							client.player.getAbilities().mayfly =
									client.player.getAbilities().flying = false;
							if (lastEnabled) {
								startFallFlying();
							}
						}
						if (startFallFlyingResendTicks > 0) {
							if (onground || !wearingGlider) {
								startFallFlyingResendTicks = 0;
							} else {
								System.out.println("[ClientFlying] run fall flying");
								if (!fallFlying) {
									sendInternalStartFallFlyingPacket(client, connection);
								}
								startFallFlyingResendTicks--;
							}
						} else if (flying && !lastFlying) {
							client.player.stopFallFlying();
							fallFlying = client.player.isFallFlying();
						} else if (fallFlying && flying) {
							client.player.getAbilities().flying = flying = false;
						}
					}
					lastGlider = wearingGlider;
					lastFlying = client.player.getAbilities().flying;
					lastFallFlying = client.player.isFallFlying();
					lastGamemode = newGamemode;
					lastEnabled = enabled;
				});
	}
}

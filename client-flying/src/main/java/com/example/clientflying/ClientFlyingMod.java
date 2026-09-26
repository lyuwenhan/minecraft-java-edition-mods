package com.example.clientflying;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

public class ClientFlyingMod implements ClientModInitializer {
	private static final String MOD_ID = "client-flying";
	private final KeyMapping.Category category =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));
	private static KeyMapping toggleKey;
	private static ClientFlyingConfig config = new ClientFlyingConfig();
	private static boolean lastGlider = false;
	private static boolean lastFlying = false;
	private static boolean lastFallFlying = false;
	private static boolean lastGamemode = false;
	private static boolean lastEnabled = false;
	private static int startFallFlyingResendTicks = 0;

	private void resetState() {
		lastGlider = false;
		lastFlying = false;
		lastFallFlying = false;
		lastGamemode = false;
		lastEnabled = false;
		startFallFlyingResendTicks = 0;
	}

	private void disableClientFlight(Minecraft client) {
		if (client.player == null || client.gameMode == null) {
			return;
		}
		GameType gameMode = client.gameMode.getPlayerMode();
		if (gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE) {
			client.player.getAbilities().flying = false;
			client.player.getAbilities().mayfly = false;
		}
	}

	private void handleToggleKey(Minecraft client) {
		if (toggleKey == null || client.player == null) {
			return;
		}
		while (toggleKey.consumeClick()) {
			config.enabled = !config.enabled;
			config.save();
			client.player.sendOverlayMessage(
					Component.literal("Client Flying: " + (config.enabled ? "ON" : "OFF")));
		}
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

	@Override
	public void onInitializeClient() {
		System.out.println("[ClientFlying] Client initialized");
		config = ClientFlyingConfig.load();
		toggleKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.client-flying.toggle",
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_V,
								category));
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

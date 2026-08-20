package com.example.flightdisabler;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public final class FlightDisablerMod implements ClientModInitializer {
	public static final String MOD_ID = "flight-disabler";
	private static final String KEY_TOGGLE = "key.flight-disabler.toggle";
	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));
	private static KeyMapping toggleKey;
	private static FlightDisablerConfig config = new FlightDisablerConfig();

	@Override
	public void onInitializeClient() {
		config = FlightDisablerConfig.load();
		toggleKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								KEY_TOGGLE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(FlightDisablerMod::onEndClientTick);
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	private static void onEndClientTick(Minecraft client) {
		handleToggleKey(client);
	}

	private static void handleToggleKey(Minecraft client) {
		if (toggleKey == null) {
			return;
		}
		while (toggleKey.consumeClick()) {
			config.enabled = !config.enabled;
			config.save();
			if (client.player != null) {
				client.player.sendOverlayMessage(
						Component.literal("Flight Disabler: " + (config.enabled ? "ON" : "OFF")));
			}
			if (config.enabled) {
				disableFlight(client);
			}
		}
	}

	public static void disableFlight(Minecraft client) {
		if (!config.enabled || client.player == null) {
			return;
		}

		client.player.getAbilities().flying = false;

		if (client.player.isFallFlying()) {
			sendMovementResetPacket(client);
			client.player.stopFallFlying();
		}
	}

	public static void suppressFallFlyingAttempt() {
		if (!config.enabled) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		disableFlight(client);
		sendMovementResetPacket(client);
	}

	private static void sendMovementResetPacket(Minecraft client) {
		if (client.player == null) {
			return;
		}
		ClientPacketListener connection = client.getConnection();
		if (connection == null) {
			return;
		}

		boolean inAir = !client.player.onGround();
		int notFlyingTicks = 0;
		connection.send(
				new ServerboundMovePlayerPacket.PosRot(
						client.player.getX(),
						client.player.getY(),
						client.player.getZ(),
						client.player.getYRot(),
						client.player.getXRot(),
						(notFlyingTicks == 0) || !inAir,
						client.player.horizontalCollision));
	}
}

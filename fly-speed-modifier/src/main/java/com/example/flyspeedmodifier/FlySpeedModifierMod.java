package com.example.flyspeedmodifier;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public final class FlySpeedModifierMod implements ClientModInitializer {
	public static final String MOD_ID = "fly-speed-modifier";

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));

	private static KeyMapping adjustSpeedKey;

	@Override
	public void onInitializeClient() {
		FlySpeedModifierConfig.load();

		adjustSpeedKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.fly-speed-modifier.adjust_speed",
								InputConstants.Type.KEYSYM,
								GLFW.GLFW_KEY_LEFT_ALT,
								CATEGORY));

		FreecamSpeedController.setAdjustSpeedKey(adjustSpeedKey);
		ClientTickEvents.END_CLIENT_TICK.register(FreecamSpeedController::onEndClientTick);
	}
}

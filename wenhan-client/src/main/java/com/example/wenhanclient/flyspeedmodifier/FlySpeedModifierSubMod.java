package com.example.wenhanclient.flyspeedmodifier;

import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;

public final class FlySpeedModifierSubMod {
	public static final String MOD_ID = "fly-speed-modifier";
	private static final KeyMapping.Category CATEGORY = WenhanClientKeyCategories.GENERAL;
	private static KeyMapping adjustSpeedKey;

	private FlySpeedModifierSubMod() {}

	public static void init() {
		FlySpeedModifierConfig.load();
		adjustSpeedKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								"key.wenhan-client.fly-speed-modifier.adjust_speed",
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_LALT,
								CATEGORY,
								3));
		FreecamSpeedController.setAdjustSpeedKey(adjustSpeedKey);
		ClientTickEvents.END_CLIENT_TICK.register(FreecamSpeedController::onEndClientTick);
		ClientPlayConnectionEvents.JOIN.register(
				(handler, sender, client) -> FreecamSpeedController.onJoinWorld());
	}
}

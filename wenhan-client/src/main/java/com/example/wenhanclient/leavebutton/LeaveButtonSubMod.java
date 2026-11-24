package com.example.wenhanclient.leavebutton;

import com.example.wenhanclient.client.WenhanClientKeyCategories;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class LeaveButtonSubMod {
	private static final String KEY_LEAVE = "key.wenhan-client.leave-button.leave";
	private static final KeyMapping.Category CATEGORY = WenhanClientKeyCategories.GENERAL;
	private static KeyMapping leaveKey;

	private LeaveButtonSubMod() {}

	public static void init() {
		leaveKey =
				KeyMappingHelper.registerKeyMapping(
						new KeyMapping(
								KEY_LEAVE,
								InputConstants.Type.KEYBOARD,
								InputConstants.KEY_GRAVE,
								CATEGORY,
								5));
		ClientTickEvents.END_CLIENT_TICK.register(LeaveButtonSubMod::onEndClientTick);
	}

	private static void onEndClientTick(Minecraft client) {
		if (leaveKey == null) {
			return;
		}

		while (leaveKey.consumeClick()) {
			if (client.level != null && client.getConnection() != null) {
				client.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
				return;
			}
		}
	}
}

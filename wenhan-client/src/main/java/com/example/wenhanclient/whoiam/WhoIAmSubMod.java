package com.example.wenhanclient.whoiam;

import com.example.wenhanclient.WenhanClientMod;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class WhoIAmSubMod {
	private static WhoIAmConfig config = new WhoIAmConfig();

	private WhoIAmSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.whoIAm.copy();
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, buildContext) ->
						dispatcher.register(
								ClientCommands.literal("i")
										.executes(
												context -> {
													String playerName =
															Minecraft.getInstance()
																	.getUser()
																	.getName();
													context.getSource()
															.sendFeedback(
																	Component.translatable(
																			"command.wenhan-client.who-i-am",
																			playerName));
													return 1;
												})));
	}

	public static boolean shouldShowLocalPlayerName() {
		return config.showLocalPlayerName;
	}

	public static boolean shouldAppendPlayerNameToTitle() {
		return config.appendPlayerNameToTitle;
	}

	public static WhoIAmConfig config() {
		return config.copy();
	}

	public static void setConfig(WhoIAmConfig newConfig) {
		config = newConfig == null ? new WhoIAmConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.whoIAm = config.copy();
		WenhanClientMod.CONFIG.save();
	}
}

package com.example.whoiam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class WhoIAmClientMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
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
																	Component.literal(
																			"You are: "
																					+ playerName));
													return 1;
												})));
	}
}

package com.example.wenhanclient.ghostblock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class GhostblockSubMod {
	private GhostblockSubMod() {}

	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> {
					RequiredArgumentBuilder<FabricClientCommandSource, ItemInput> itemArgument =
							ClientCommands.argument("item", ItemArgument.item(registryAccess))
									.executes(
											ctx ->
													giveGhostItem(
															ctx.getSource(),
															ItemArgument.getItem(ctx, "item"),
															1))
									.then(
											ClientCommands.argument(
															"count", IntegerArgumentType.integer(1))
													.executes(
															ctx ->
																	giveGhostItem(
																			ctx.getSource(),
																			ItemArgument.getItem(
																					ctx, "item"),
																			IntegerArgumentType
																					.getInteger(
																							ctx,
																							"count"))));
					dispatcher.register(ClientCommands.literal("get").then(itemArgument));
				});
	}

	private static int giveGhostItem(
			FabricClientCommandSource source, ItemInput itemInput, int count)
			throws CommandSyntaxException {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			source.sendError(Component.translatable("command.wenhan-client.ghostblock.no_player"));
			return 0;
		}

		int remaining = count;
		int given = 0;
		int maxStackSize = itemInput.createItemStack(1).getMaxStackSize();
		while (remaining > 0) {
			int stackCount = Math.min(remaining, maxStackSize);
			ItemStack stack = itemInput.createItemStack(stackCount);
			remaining -= stackCount;
			int before = stack.getCount();
			if (!client.player.getInventory().add(stack)) {
				given += before - stack.getCount();
				break;
			}
			given += before;
		}

		if (given <= 0) {
			source.sendError(
					Component.translatable("command.wenhan-client.ghostblock.inventory_full"));
			return 0;
		}

		source.sendFeedback(
				Component.translatable("command.wenhan-client.ghostblock.given", given));
		return given;
	}
}

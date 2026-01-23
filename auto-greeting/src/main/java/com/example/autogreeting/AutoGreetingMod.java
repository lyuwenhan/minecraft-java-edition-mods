package com.example.autogreeting;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class AutoGreetingMod implements ClientModInitializer {

	public static long joinWorldAt = 0L;
	public static final AutoGreetingConfig CONFIG = AutoGreetingConfig.load();

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			AutoGreetingMod.joinWorldAt = System.currentTimeMillis();
			if (!CONFIG.selfEnabled) return;

			client.execute(() -> {
				if (client.player == null) return;

				String playerName = client.player.getName().getString();

				for (String msg : CONFIG.selfGreetings) {
					if (msg == null || msg.isBlank()) continue;

					msg = msg.trim();

	  				String finalMsg = msg.replace("@player", playerName);
					if (msg.startsWith("/")) {
						client.player.networkHandler.sendChatCommand(finalMsg.substring(1));
					} else {
						client.player.networkHandler.sendChatMessage(finalMsg);
					}
				}
			});
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

			dispatcher.register(literal("autogreet").then(literal("self")

				.then(literal("status").executes(ctx -> {
					ctx.getSource().sendFeedback(
						Text.literal("Auto greeting is " + (CONFIG.selfEnabled ? "ON" : "OFF"))
					);
					return 1;
				}))

				.then(literal("on").executes(ctx -> {
					CONFIG.selfEnabled = true;
					CONFIG.save();
					ctx.getSource().sendFeedback(Text.literal("Auto greeting enabled."));
					return 1;
				}))
				.then(literal("off").executes(ctx -> {
					CONFIG.selfEnabled = false;
					CONFIG.save();
					ctx.getSource().sendFeedback(Text.literal("Auto greeting disabled."));
					return 1;
				}))
				.then(literal("toggle").executes(ctx -> {
					CONFIG.selfEnabled = !CONFIG.selfEnabled;
					CONFIG.save();
					ctx.getSource().sendFeedback(
						Text.literal("Auto greeting is now " + (CONFIG.selfEnabled ? "ON" : "OFF"))
					);
					return 1;
				}))

				.then(literal("add")

					.then(argument("message", StringArgumentType.greedyString())
						.executes(ctx -> {
							CONFIG.selfGreetings.add(
								StringArgumentType.getString(ctx, "message")
							);
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Greeting appended."));
							return 1;
						})

						.then(argument("index", IntegerArgumentType.integer(1))
							.executes(ctx -> {
								String msg = StringArgumentType.getString(ctx, "message");
								int index = IntegerArgumentType.getInteger(ctx, "index");

								int pos = Math.min(index - 1, CONFIG.selfGreetings.size());
								CONFIG.selfGreetings.add(pos, msg);
								CONFIG.save();

								ctx.getSource().sendFeedback(
									Text.literal("Greeting inserted at position " + index + ".")
								);
								return 1;
							})
						)
					)
				)

				.then(literal("list").executes(ctx -> {
					if (CONFIG.selfGreetings.isEmpty()) {
						ctx.getSource().sendFeedback(Text.literal("Greeting list is empty."));
						return 1;
					}

					int i = 1;
					for (String msg : CONFIG.selfGreetings) {
						ctx.getSource().sendFeedback(
							Text.literal(i++ + ". " + msg)
						);
					}
					return 1;
				}))

				.then(literal("remove")

					.executes(ctx -> {
						if (CONFIG.selfGreetings.isEmpty()) {
							ctx.getSource().sendFeedback(Text.literal("Nothing to remove."));
							return 1;
						}

						CONFIG.selfGreetings.remove(CONFIG.selfGreetings.size() - 1);
						CONFIG.save();
						ctx.getSource().sendFeedback(Text.literal("Last greeting removed."));
						return 1;
					})

					.then(argument("index", IntegerArgumentType.integer(1))
						.executes(ctx -> {
							int index = IntegerArgumentType.getInteger(ctx, "index");

							if (index > CONFIG.selfGreetings.size()) {
								ctx.getSource().sendFeedback(Text.literal("Index out of range."));
								return 1;
							}

							CONFIG.selfGreetings.remove(index - 1);
							CONFIG.save();
							ctx.getSource().sendFeedback(
								Text.literal("Greeting " + index + " removed.")
							);
							return 1;
						})
					)
				)

				.then(literal("removeAll").executes(ctx -> {
					CONFIG.selfGreetings.clear();
					CONFIG.save();
					ctx.getSource().sendFeedback(Text.literal("All greetings removed."));
					return 1;
				}))
			).then(literal("other")
				.then(literal("status").executes(ctx -> {
					ctx.getSource().sendFeedback(
						Text.literal("Auto greeting is " + (CONFIG.otherEnabled ? "ON" : "OFF"))
					);
					return 1;
				}))

				.then(literal("on").executes(ctx -> {
					CONFIG.otherEnabled = true;
					CONFIG.save();
					ctx.getSource().sendFeedback(Text.literal("Auto greeting enabled."));
					return 1;
				}))
				.then(literal("off").executes(ctx -> {
					CONFIG.otherEnabled = false;
					CONFIG.save();
					ctx.getSource().sendFeedback(Text.literal("Auto greeting disabled."));
					return 1;
				}))
				.then(literal("toggle").executes(ctx -> {
					CONFIG.otherEnabled = !CONFIG.otherEnabled;
					CONFIG.save();
					ctx.getSource().sendFeedback(
						Text.literal("Auto greeting is now " + (CONFIG.otherEnabled ? "ON" : "OFF"))
					);
					return 1;
				}))

				.then(literal("add")

					.then(argument("message", StringArgumentType.greedyString())
						.executes(ctx -> {
							CONFIG.otherGreetings.add(
								StringArgumentType.getString(ctx, "message")
							);
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Greeting appended."));
							return 1;
						})

						.then(argument("index", IntegerArgumentType.integer(1))
							.executes(ctx -> {
								String msg = StringArgumentType.getString(ctx, "message");
								int index = IntegerArgumentType.getInteger(ctx, "index");

								int pos = Math.min(index - 1, CONFIG.otherGreetings.size());
								CONFIG.otherGreetings.add(pos, msg);
								CONFIG.save();

								ctx.getSource().sendFeedback(
									Text.literal("Greeting inserted at position " + index + ".")
								);
								return 1;
							})
						)
					)
				)

				.then(literal("list").executes(ctx -> {
					if (CONFIG.otherGreetings.isEmpty()) {
						ctx.getSource().sendFeedback(Text.literal("Greeting list is empty."));
						return 1;
					}

					int i = 1;
					for (String msg : CONFIG.otherGreetings) {
						ctx.getSource().sendFeedback(
							Text.literal(i++ + ". " + msg)
						);
					}
					return 1;
				}))

				.then(literal("remove")

					.executes(ctx -> {
						if (CONFIG.otherGreetings.isEmpty()) {
							ctx.getSource().sendFeedback(Text.literal("Nothing to remove."));
							return 1;
						}

						CONFIG.otherGreetings.remove(CONFIG.otherGreetings.size() - 1);
						CONFIG.save();
						ctx.getSource().sendFeedback(Text.literal("Last greeting removed."));
						return 1;
					})

					.then(argument("index", IntegerArgumentType.integer(1))
						.executes(ctx -> {
							int index = IntegerArgumentType.getInteger(ctx, "index");

							if (index > CONFIG.otherGreetings.size()) {
								ctx.getSource().sendFeedback(Text.literal("Index out of range."));
								return 1;
							}

							CONFIG.otherGreetings.remove(index - 1);
							CONFIG.save();
							ctx.getSource().sendFeedback(
								Text.literal("Greeting " + index + " removed.")
							);
							return 1;
						})
					)
				)

				.then(literal("removeAll").executes(ctx -> {
					CONFIG.otherGreetings.clear();
					CONFIG.save();
					ctx.getSource().sendFeedback(Text.literal("All greetings removed."));
					return 1;
				}))
			));
		});
	}
}

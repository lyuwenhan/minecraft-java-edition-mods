package com.example.autogreeting;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class AutoGreetingMod implements ClientModInitializer {

	public static long joinWorldAt = 0L;
	public static final AutoGreetingConfig CONFIG = AutoGreetingConfig.load();

	private static void sendList(
		FabricClientCommandSource src,
		String title,
		List<String> list
	) {
		if (list.isEmpty()) {
			src.sendFeedback(Text.literal(title + ": <empty>"));
			return;
		}

		src.sendFeedback(Text.literal(title + ":"));

		int i = 1;
		for (String s : list) {
			src.sendFeedback(Text.literal(i++ + ". " + s));
		}
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> buildStringListNode(
		String name,
		String title,
		List<String> list,
		boolean allowDupe,
		boolean allowAddIndex
	) {
		RequiredArgumentBuilder<FabricClientCommandSource, String> addArg = argument("message", StringArgumentType.greedyString())
		.executes(ctx -> {
			String msg = StringArgumentType.getString(ctx, "message");
			if (!allowDupe && list.contains(msg)) {
				ctx.getSource().sendFeedback(Text.literal(title + ": \"" + msg + "\" already exists."));
				return 1;
			}
			list.add(msg);
			CONFIG.save();
			ctx.getSource().sendFeedback(Text.literal(title + ": appended \"" + msg + "\"."));
			return 1;
		});
		if(allowAddIndex){
			addArg = addArg.then(argument("index", IntegerArgumentType.integer(1))
				.executes(ctx -> {
					String msg = StringArgumentType.getString(ctx, "message");
					int index = IntegerArgumentType.getInteger(ctx, "index");
					if (!allowDupe && list.contains(msg)) {
						ctx.getSource().sendFeedback(Text.literal(title + ": \"" + msg + "\" already exists."));
						return 1;
					}
					boolean isAppend = index > list.size();
					int pos = Math.max(1, Math.min(index - 1, list.size()));
					list.add(pos, msg);
					CONFIG.save();
					if (isAppend) {
						ctx.getSource().sendFeedback(Text.literal(title + ": appended \"" + msg + "\"."));
					} else {
						ctx.getSource().sendFeedback(Text.literal(title + ": inserted \"" + msg + "\" at position " + index + "."));
					}
					return 1;
				})
			);
		}

		return literal(name)
			.then(literal("add").then(addArg))

			.then(literal("remove")
				.executes(ctx -> {
					if (list.isEmpty()) {
						ctx.getSource().sendFeedback(Text.literal(title + " is empty."));
						return 1;
					}
					list.remove(list.size() - 1);
					CONFIG.save();
					ctx.getSource().sendFeedback(Text.literal(title + ": removed last item."));
					return 1;
				})

				.then(argument("index", IntegerArgumentType.integer(1))
					.executes(ctx -> {
						int index = IntegerArgumentType.getInteger(ctx, "index");
						if (index < 1 || index > list.size()) {
							ctx.getSource().sendFeedback(Text.literal(title + ": index out of range."));
							return 1;
						}

						list.remove(index - 1);
						CONFIG.save();
						ctx.getSource().sendFeedback(Text.literal(title + ": removed #" + index + "."));
						return 1;
					})
				)

				.then(literal("all")
					.executes(ctx -> {
						if (list.isEmpty()) {
							ctx.getSource().sendFeedback(Text.literal(title + " is already empty."));
							return 1;
						}
						list.clear();
						CONFIG.save();
						ctx.getSource().sendFeedback(Text.literal(title + ": all entries cleared."));
						return 1;
					})
				)
			)

			.then(literal("list")
				.executes(ctx -> {
					sendList(ctx.getSource(), title, list);
					return 1;
				})
			);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> buildStringListNode(
		String name,
		String title,
		List<String> list
	) {
		return buildStringListNode(name, title, list, false, false);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> buildStringListNode(
		String name,
		String title,
		List<String> list,
		boolean operation
	) {
		return buildStringListNode(name, title, list, operation, operation);
	}

	private static String fmt(double v) {
		return BigDecimal.valueOf(v)
				.setScale(3, RoundingMode.HALF_UP)
				.stripTrailingZeros()
				.toPlainString();
	}


	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			AutoGreetingMod.joinWorldAt = System.currentTimeMillis();
			if (!CONFIG.selfEnabled) {
				return;
			}

			client.execute(() -> {
				if (client.player == null) {
					return;
				}

				String playerName = client.player.getName().getString();
				String playerUUID = client.player.getUuid().toString();

				String playerX = fmt(client.player.getX());
				String playerY = fmt(client.player.getY());
				String playerZ = fmt(client.player.getZ());

				String health = fmt(client.player.getHealth());
				String level = Integer.toString(client.player.experienceLevel);

				for (String msg : CONFIG.selfGreetings) {
					if (msg == null || msg.isBlank()) {
						continue;
					}

					msg = msg.trim();

					String finalMsg = msg
						.replace("@player", playerName)
						.replace("@UUID", playerUUID)
						.replace("@X", playerX)
						.replace("@Y", playerY)
						.replace("@Z", playerZ)
						.replace("@health", health)
						.replace("@level", level);
					if (msg.startsWith("/")) {
						client.player.networkHandler.sendChatCommand(finalMsg.substring(1));
					} else {
						client.player.networkHandler.sendChatMessage(finalMsg);
					}
				}
			});
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("autogreet")
				.then(literal("self")
					.then(literal("status")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Text.literal("Auto greeting is " + (CONFIG.selfEnabled ? "enabled" : "disabled")));
							return 1;
						})

						.then(literal("enable").executes(ctx -> {
							CONFIG.selfEnabled = true;
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Auto greeting enabled."));
							return 1;
						}))

						.then(literal("disable").executes(ctx -> {
							CONFIG.selfEnabled = false;
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Auto greeting disabled."));
							return 1;
						}))

						.then(literal("toggle").executes(ctx -> {
							CONFIG.selfEnabled = !CONFIG.selfEnabled;
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Auto greeting is now " + (CONFIG.selfEnabled ? "enabled" : "disabled")));
							return 1;
						}))
					)

					.then(buildStringListNode("message", "Auto greeting", CONFIG.selfGreetings, true))
				)
				.then(literal("other")
					.then(literal("status")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Text.literal("Auto greeting " + (CONFIG.otherEnabled ? "enabled" : "disabled")));
							return 1;
						})

						.then(literal("enable").executes(ctx -> {
							CONFIG.otherEnabled = true;
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Auto greeting enabled."));
							return 1;
						}))

						.then(literal("disable").executes(ctx -> {
							CONFIG.otherEnabled = false;
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Auto greeting disabled."));
							return 1;
						}))

						.then(literal("toggle").executes(ctx -> {
							CONFIG.otherEnabled = !CONFIG.otherEnabled;
							CONFIG.save();
							ctx.getSource().sendFeedback(Text.literal("Auto greeting " + (CONFIG.otherEnabled ? "enabled" : "disabled") + "."));
							return 1;
						}))
					)
					.then(buildStringListNode("message", "Auto greeting", CONFIG.otherGreetings, true))

					.then(literal("blacklist")
						.then(literal("match")
							.then(buildStringListNode(
								"equal",
								"Blacklist (Name Equal)",
								CONFIG.otherBlacklist.equal
							))

							.then(buildStringListNode(
								"contain",
								"Blacklist (Name Contain)",
								CONFIG.otherBlacklist.contain
							))

							.then(buildStringListNode(
								"startWith",
								"Blacklist (Name Starts with)",
								CONFIG.otherBlacklist.startWith
							))

							.then(buildStringListNode(
								"endWith",
								"Blacklist (Name Ends with)",
								CONFIG.otherBlacklist.endWith
							))

							.then(literal("list")
								.executes(ctx -> {
									sendList(ctx.getSource(), "Match (Name Equal)", CONFIG.otherBlacklist.equal);
									sendList(ctx.getSource(), "Match (Name Contain)", CONFIG.otherBlacklist.contain);
									sendList(ctx.getSource(), "Match (Name Starts with)", CONFIG.otherBlacklist.startWith);
									sendList(ctx.getSource(), "Match (Name Ends with)", CONFIG.otherBlacklist.endWith);
									return 1;
								})
							)
						)

						.then(literal("except")
							.then(buildStringListNode(
								"equal",
								"Except (Name Equal)",
								CONFIG.otherBlacklistExcept.equal
							))

							.then(buildStringListNode(
								"contain",
								"Except (Name Contain)",
								CONFIG.otherBlacklistExcept.contain
							))

							.then(buildStringListNode(
								"startWith",
								"Except (Name Starts with)",
								CONFIG.otherBlacklistExcept.startWith
							))

							.then(buildStringListNode(
								"endWith",
								"Except (Name Ends with)",
								CONFIG.otherBlacklistExcept.endWith
							))

							.then(literal("list")
								.executes(ctx -> {
									sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.otherBlacklistExcept.equal);
									sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.otherBlacklistExcept.contain);
									sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.otherBlacklistExcept.startWith);
									sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.otherBlacklistExcept.endWith);
									return 1;
								})
							)
						)

						.then(literal("list")
							.executes(ctx -> {
								sendList(ctx.getSource(), "Match (Name Equal)", CONFIG.otherBlacklist.equal);
								sendList(ctx.getSource(), "Match (Name Contain)", CONFIG.otherBlacklist.contain);
								sendList(ctx.getSource(), "Match (Name Starts with)", CONFIG.otherBlacklist.startWith);
								sendList(ctx.getSource(), "Match (Name Ends with)", CONFIG.otherBlacklist.endWith);

								sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.otherBlacklistExcept.equal);
								sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.otherBlacklistExcept.contain);
								sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.otherBlacklistExcept.startWith);
								sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.otherBlacklistExcept.endWith);
								return 1;
							})
						)

						.then(literal("clear")
							.then(literal("confirm")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(Text.literal("Blacklist cleared."));
									CONFIG.otherBlacklist.equal.clear();
									CONFIG.otherBlacklist.contain.clear();
									CONFIG.otherBlacklist.startWith.clear();
									CONFIG.otherBlacklist.endWith.clear();

									CONFIG.otherBlacklistExcept.equal.clear();
									CONFIG.otherBlacklistExcept.contain.clear();
									CONFIG.otherBlacklistExcept.startWith.clear();
									CONFIG.otherBlacklistExcept.endWith.clear();
									CONFIG.save();
									return 1;
								})
							)
						)
					)

					.then(literal("whitelist")
						.then(literal("match")
							.then(buildStringListNode(
								"equal",
								"Whitelist (Name Equal)",
								CONFIG.otherWhitelist.equal
							))

							.then(buildStringListNode(
								"contain",
								"Whitelist (Name Contain)",
								CONFIG.otherWhitelist.contain
							))

							.then(buildStringListNode(
								"startWith",
								"Whitelist (Name Starts with)",
								CONFIG.otherWhitelist.startWith
							))

							.then(buildStringListNode(
								"endWith",
								"Whitelist (Name Ends with)",
								CONFIG.otherWhitelist.endWith
							))

							.then(literal("list")
								.executes(ctx -> {
									sendList(ctx.getSource(), "Whitelist (Name Equal)", CONFIG.otherWhitelist.equal);
									sendList(ctx.getSource(), "Whitelist (Name Contain)", CONFIG.otherWhitelist.contain);
									sendList(ctx.getSource(), "Whitelist (Name Starts with)", CONFIG.otherWhitelist.startWith);
									sendList(ctx.getSource(), "Whitelist (Name Ends with)", CONFIG.otherWhitelist.endWith);
									return 1;
								})
							)
						)

						.then(literal("except")
							.then(buildStringListNode(
								"equal",
								"Except (Name Equal)",
								CONFIG.otherWhitelistExcept.equal
							))

							.then(buildStringListNode(
								"contain",
								"Except (Name Contain)",
								CONFIG.otherWhitelistExcept.contain
							))

							.then(buildStringListNode(
								"startWith",
								"Except (Name Starts with)",
								CONFIG.otherWhitelistExcept.startWith
							))

							.then(buildStringListNode(
								"endWith",
								"Except (Name Ends with)",
								CONFIG.otherWhitelistExcept.endWith
							))

							.then(literal("list")
								.executes(ctx -> {
									sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.otherWhitelistExcept.equal);
									sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.otherWhitelistExcept.contain);
									sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.otherWhitelistExcept.startWith);
									sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.otherWhitelistExcept.endWith);
									return 1;
								})
							)
						)

						.then(literal("list")
							.executes(ctx -> {
								sendList(ctx.getSource(), "Whitelist (Name Equal)", CONFIG.otherWhitelist.equal);
								sendList(ctx.getSource(), "Whitelist (Name Contain)", CONFIG.otherWhitelist.contain);
								sendList(ctx.getSource(), "Whitelist (Name Starts with)", CONFIG.otherWhitelist.startWith);
								sendList(ctx.getSource(), "Whitelist (Name Ends with)", CONFIG.otherWhitelist.endWith);

								sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.otherWhitelistExcept.equal);
								sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.otherWhitelistExcept.contain);
								sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.otherWhitelistExcept.startWith);
								sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.otherWhitelistExcept.endWith);
								return 1;
							})
						)

						.then(literal("clear")
							.then(literal("confirm")
								.executes(ctx -> {
									ctx.getSource().sendFeedback(Text.literal("Whitelist cleared."));
									CONFIG.otherWhitelist.equal.clear();
									CONFIG.otherWhitelist.contain.clear();
									CONFIG.otherWhitelist.startWith.clear();
									CONFIG.otherWhitelist.endWith.clear();

									CONFIG.otherWhitelistExcept.equal.clear();
									CONFIG.otherWhitelistExcept.contain.clear();
									CONFIG.otherWhitelistExcept.startWith.clear();
									CONFIG.otherWhitelistExcept.endWith.clear();
									CONFIG.save();
									return 1;
								})
							)
						)
					)
				)
			);
		});
	}
}

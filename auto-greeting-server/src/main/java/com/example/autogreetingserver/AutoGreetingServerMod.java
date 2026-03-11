package com.example.autogreetingserver;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AutoGreetingServerMod implements DedicatedServerModInitializer {
	public static final AutoGreetingServerConfig CONFIG = AutoGreetingServerConfig.load();

	private static void sendList(
		ServerCommandSource src,
		String title,
		List<String> list
	) {
		if (list.isEmpty()) {
			src.sendFeedback(() -> Text.literal(title + ": <empty>"), false);
			return;
		}

		src.sendFeedback(() -> Text.literal(title + ":"), false);

		int i = 1;
		for (String s : list) {
			final int index = i++;
			src.sendFeedback(() -> Text.literal(index + ". " + s), false);
		}
	}

	private static LiteralArgumentBuilder<ServerCommandSource> buildStringListNode(
		String name,
		String title,
		List<String> list,
		boolean allowDupe,
		boolean allowAddIndex
	) {
		RequiredArgumentBuilder<ServerCommandSource, String> addArg = argument("message", StringArgumentType.greedyString())
		.executes(ctx -> {
			String msg = StringArgumentType.getString(ctx, "message");
			if (!allowDupe && list.contains(msg)) {
				ctx.getSource().sendFeedback(() -> Text.literal(title + ": \"" + msg + "\" already exists."), false);
				return 1;
			}
			list.add(msg);
			CONFIG.save();
			ctx.getSource().sendFeedback(() -> Text.literal(title + ": appended \"" + msg + "\"."), false);
			return 1;
		});
		if (allowAddIndex) {
			addArg = addArg.then(argument("index", IntegerArgumentType.integer(1))
				.executes(ctx -> {
					String msg = StringArgumentType.getString(ctx, "message");
					int index = IntegerArgumentType.getInteger(ctx, "index");
					if (!allowDupe && list.contains(msg)) {
						ctx.getSource().sendFeedback(() -> Text.literal(title + ": \"" + msg + "\" already exists."), false);
						return 1;
					}
					boolean isAppend = index > list.size();
					int pos = Math.max(1, Math.min(index - 1, list.size()));
					list.add(pos, msg);
					CONFIG.save();
					if (isAppend) {
						ctx.getSource().sendFeedback(() -> Text.literal(title + ": appended \"" + msg + "\"."), false);
					} else {
						ctx.getSource().sendFeedback(() -> Text.literal(title + ": inserted \"" + msg + "\" at position " + index + "."), false);
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
						ctx.getSource().sendFeedback(() -> Text.literal(title + " is empty."), false);
						return 1;
					}
					list.remove(list.size() - 1);
					CONFIG.save();
					ctx.getSource().sendFeedback(() -> Text.literal(title + ": removed last item."), false);
					return 1;
				})

				.then(argument("index", IntegerArgumentType.integer(1))
					.executes(ctx -> {
						int index = IntegerArgumentType.getInteger(ctx, "index");
						if (index < 1 || index > list.size()) {
							ctx.getSource().sendFeedback(() -> Text.literal(title + ": index out of range."), false);
							return 1;
						}

						list.remove(index - 1);
						CONFIG.save();
						ctx.getSource().sendFeedback(() -> Text.literal(title + ": removed #" + index + "."), false);
						return 1;
					})
				)

				.then(literal("all")
					.executes(ctx -> {
						if (list.isEmpty()) {
							ctx.getSource().sendFeedback(() -> Text.literal(title + " is already empty."), false);
							return 1;
						}
						list.clear();
						CONFIG.save();
						ctx.getSource().sendFeedback(() -> Text.literal(title + ": all entries cleared."), false);
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

	private static LiteralArgumentBuilder<ServerCommandSource> buildStringListNode(
		String name,
		String title,
		List<String> list
	) {
		return buildStringListNode(name, title, list, false, false);
	}

	private static LiteralArgumentBuilder<ServerCommandSource> buildStringListNode(
		String name,
		String title,
		List<String> list,
		boolean operation
	) {
		return buildStringListNode(name, title, list, operation, operation);
	}

	private static boolean shouldGreet(ServerPlayerEntity player) {
		String name = player.getName().getString();

		if (CONFIG.serverBlacklist.match(name) && !CONFIG.serverBlacklistExcept.match(name)) {
			return false;
		}

		if (!CONFIG.serverWhitelist.isEmpty() && (!CONFIG.serverWhitelist.match(name) || CONFIG.serverWhitelistExcept.match(name))) {
			return false;
		}

		return true;
	}

	@Override
	public void onInitializeServer() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!CONFIG.serverEnabled) {
				return;
			}

			ServerPlayerEntity player = handler.player;
			if (!shouldGreet(player)) {
				return;
			}

			AutoGreetingServerDelay.greetAfter1Second(player);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("servergreet")
				.requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))
				.then(literal("status")
					.executes(ctx -> {
						ctx.getSource().sendFeedback(() -> Text.literal("Auto greeting " + (CONFIG.serverEnabled ? "enabled" : "disabled") + "."), false);
						return 1;
					})

					.then(literal("enable").executes(ctx -> {
						CONFIG.serverEnabled = true;
						CONFIG.save();
						ctx.getSource().sendFeedback(() -> Text.literal("Auto greeting enabled."), false);
						return 1;
					}))

					.then(literal("disable").executes(ctx -> {
						CONFIG.serverEnabled = false;
						CONFIG.save();
						ctx.getSource().sendFeedback(() -> Text.literal("Auto greeting disabled."), false);
						return 1;
					}))

					.then(literal("toggle").executes(ctx -> {
						CONFIG.serverEnabled = !CONFIG.serverEnabled;
						CONFIG.save();
						ctx.getSource().sendFeedback(() -> Text.literal("Auto greeting is " + (CONFIG.serverEnabled ? "enabled" : "disabled") + "."), false);
						return 1;
					}))
				)

				.then(buildStringListNode("message", "Auto greeting", CONFIG.serverGreetings, true))

				.then(literal("blacklist")
					.then(literal("match")
						.then(buildStringListNode(
							"equal",
							"Blacklist (Name Equal)",
							CONFIG.serverBlacklist.equal
						))

						.then(buildStringListNode(
							"contain",
							"Blacklist (Name Contain)",
							CONFIG.serverBlacklist.contain
						))

						.then(buildStringListNode(
							"startWith",
							"Blacklist (Name Starts with)",
							CONFIG.serverBlacklist.startWith
						))

						.then(buildStringListNode(
							"endWith",
							"Blacklist (Name Ends with)",
							CONFIG.serverBlacklist.endWith
						))

						.then(literal("list")
							.executes(ctx -> {
								sendList(ctx.getSource(), "Match (Name Equal)", CONFIG.serverBlacklist.equal);
								sendList(ctx.getSource(), "Match (Name Contain)", CONFIG.serverBlacklist.contain);
								sendList(ctx.getSource(), "Match (Name Starts with)", CONFIG.serverBlacklist.startWith);
								sendList(ctx.getSource(), "Match (Name Ends with)", CONFIG.serverBlacklist.endWith);
								return 1;
							})
						)
					)

					.then(literal("except")
						.then(buildStringListNode(
							"equal",
							"Except (Name Equal)",
							CONFIG.serverBlacklistExcept.equal
						))

						.then(buildStringListNode(
							"contain",
							"Except (Name Contain)",
							CONFIG.serverBlacklistExcept.contain
						))

						.then(buildStringListNode(
							"startWith",
							"Except (Name Starts with)",
							CONFIG.serverBlacklistExcept.startWith
						))

						.then(buildStringListNode(
							"endWith",
							"Except (Name Ends with)",
							CONFIG.serverBlacklistExcept.endWith
						))

						.then(literal("list")
							.executes(ctx -> {
								sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.serverBlacklistExcept.equal);
								sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.serverBlacklistExcept.contain);
								sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.serverBlacklistExcept.startWith);
								sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.serverBlacklistExcept.endWith);
								return 1;
							})
						)
					)

					.then(literal("list")
						.executes(ctx -> {
							sendList(ctx.getSource(), "Match (Name Equal)", CONFIG.serverBlacklist.equal);
							sendList(ctx.getSource(), "Match (Name Contain)", CONFIG.serverBlacklist.contain);
							sendList(ctx.getSource(), "Match (Name Starts with)", CONFIG.serverBlacklist.startWith);
							sendList(ctx.getSource(), "Match (Name Ends with)", CONFIG.serverBlacklist.endWith);

							sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.serverBlacklistExcept.equal);
							sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.serverBlacklistExcept.contain);
							sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.serverBlacklistExcept.startWith);
							sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.serverBlacklistExcept.endWith);
							return 1;
						})
					)

					.then(literal("clear")
						.then(literal("confirm")
							.executes(ctx -> {
								CONFIG.serverBlacklist.clear();
								CONFIG.serverBlacklistExcept.clear();
								CONFIG.save();
								ctx.getSource().sendFeedback(() -> Text.literal("Blacklist cleared."), false);
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
							CONFIG.serverWhitelist.equal
						))

						.then(buildStringListNode(
							"contain",
							"Whitelist (Name Contain)",
							CONFIG.serverWhitelist.contain
						))

						.then(buildStringListNode(
							"startWith",
							"Whitelist (Name Starts with)",
							CONFIG.serverWhitelist.startWith
						))

						.then(buildStringListNode(
							"endWith",
							"Whitelist (Name Ends with)",
							CONFIG.serverWhitelist.endWith
						))

						.then(literal("list")
							.executes(ctx -> {
								sendList(ctx.getSource(), "Whitelist (Name Equal)", CONFIG.serverWhitelist.equal);
								sendList(ctx.getSource(), "Whitelist (Name Contain)", CONFIG.serverWhitelist.contain);
								sendList(ctx.getSource(), "Whitelist (Name Starts with)", CONFIG.serverWhitelist.startWith);
								sendList(ctx.getSource(), "Whitelist (Name Ends with)", CONFIG.serverWhitelist.endWith);
								return 1;
							})
						)
					)

					.then(literal("except")
						.then(buildStringListNode(
							"equal",
							"Except (Name Equal)",
							CONFIG.serverWhitelistExcept.equal
						))

						.then(buildStringListNode(
							"contain",
							"Except (Name Contain)",
							CONFIG.serverWhitelistExcept.contain
						))

						.then(buildStringListNode(
							"startWith",
							"Except (Name Starts with)",
							CONFIG.serverWhitelistExcept.startWith
						))

						.then(buildStringListNode(
							"endWith",
							"Except (Name Ends with)",
							CONFIG.serverWhitelistExcept.endWith
						))

						.then(literal("list")
							.executes(ctx -> {
								sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.serverWhitelistExcept.equal);
								sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.serverWhitelistExcept.contain);
								sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.serverWhitelistExcept.startWith);
								sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.serverWhitelistExcept.endWith);
								return 1;
							})
						)
					)

					.then(literal("list")
						.executes(ctx -> {
							sendList(ctx.getSource(), "Whitelist (Name Equal)", CONFIG.serverWhitelist.equal);
							sendList(ctx.getSource(), "Whitelist (Name Contain)", CONFIG.serverWhitelist.contain);
							sendList(ctx.getSource(), "Whitelist (Name Starts with)", CONFIG.serverWhitelist.startWith);
							sendList(ctx.getSource(), "Whitelist (Name Ends with)", CONFIG.serverWhitelist.endWith);

							sendList(ctx.getSource(), "Except (Name Equal)", CONFIG.serverWhitelistExcept.equal);
							sendList(ctx.getSource(), "Except (Name Contain)", CONFIG.serverWhitelistExcept.contain);
							sendList(ctx.getSource(), "Except (Name Starts with)", CONFIG.serverWhitelistExcept.startWith);
							sendList(ctx.getSource(), "Except (Name Ends with)", CONFIG.serverWhitelistExcept.endWith);
							return 1;
						})
					)

					.then(literal("clear")
						.then(literal("confirm")
							.executes(ctx -> {
								CONFIG.serverWhitelist.clear();
								CONFIG.serverWhitelistExcept.clear();
								CONFIG.save();
								ctx.getSource().sendFeedback(() -> Text.literal("Whitelist cleared."), false);
								return 1;
							})
						)
					)
				)
			);
		});
	}
}

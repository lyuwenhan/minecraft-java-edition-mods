package com.example.autogreetingserver;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.List;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AutoGreetingServerMod implements DedicatedServerModInitializer {
	public static final AutoGreetingServerConfig CONFIG = AutoGreetingServerConfig.load();

	private static void sendList(CommandSourceStack src, String title, List<String> list) {
		if (list.isEmpty()) {
			src.sendSuccess(() -> Component.literal(title + ": <empty>"), false);
			return;
		}

		src.sendSuccess(() -> Component.literal(title + ":"), false);

		int i = 1;
		for (String s : list) {
			final int index = i++;
			src.sendSuccess(() -> Component.literal(index + ". " + s), false);
		}
	}

	private static LiteralArgumentBuilder<CommandSourceStack> buildStringListNode(
			String name, String title, List<String> list, boolean isMessage) {
		String pattern = isMessage ? "message" : "pattern";
		RequiredArgumentBuilder<CommandSourceStack, String> addArg =
				argument(pattern, StringArgumentType.greedyString())
						.executes(
								ctx -> {
									String msg = StringArgumentType.getString(ctx, pattern);
									if (!isMessage && list.contains(msg)) {
										ctx.getSource()
												.sendSuccess(
														() ->
																Component.literal(title + ": \"" + msg + "\" already" + " exists."),
														false);
										return 1;
									}
									list.add(msg);
									CONFIG.save();
									ctx.getSource()
											.sendSuccess(
													() -> Component.literal(title + ": appended \"" + msg + "\"."), false);
									return 1;
								});
		if (isMessage) {
			addArg =
					addArg.then(
							argument("index", IntegerArgumentType.integer(1))
									.executes(
											ctx -> {
												String msg = StringArgumentType.getString(ctx, pattern);
												int index = IntegerArgumentType.getInteger(ctx, "index");
												if (!isMessage && list.contains(msg)) {
													ctx.getSource()
															.sendSuccess(
																	() ->
																			Component.literal(
																					title + ": \"" + msg + "\" already" + " exists."),
																	false);
													return 1;
												}
												boolean isAppend = index > list.size();
												int pos = Math.max(1, Math.min(index - 1, list.size()));
												list.add(pos, msg);
												CONFIG.save();
												if (isAppend) {
													ctx.getSource()
															.sendSuccess(
																	() ->
																			Component.literal(title + ": appended" + " \"" + msg + "\"."),
																	false);
												} else {
													ctx.getSource()
															.sendSuccess(
																	() ->
																			Component.literal(
																					title
																							+ ": inserted"
																							+ " \""
																							+ msg
																							+ "\" at"
																							+ " position"
																							+ " "
																							+ index
																							+ "."),
																	false);
												}
												return 1;
											}));
		}

		return literal(name)
				.then(literal("add").then(addArg))
				.then(
						literal("remove")
								.executes(
										ctx -> {
											if (list.isEmpty()) {
												ctx.getSource()
														.sendSuccess(() -> Component.literal(title + " is empty."), false);
												return 1;
											}
											list.remove(list.size() - 1);
											CONFIG.save();
											ctx.getSource()
													.sendSuccess(
															() -> Component.literal(title + ": removed" + " last" + " item."),
															false);
											return 1;
										})
								.then(
										argument("index", IntegerArgumentType.integer(1))
												.executes(
														ctx -> {
															int index = IntegerArgumentType.getInteger(ctx, "index");
															if (index < 1 || index > list.size()) {
																ctx.getSource()
																		.sendSuccess(
																				() ->
																						Component.literal(
																								title + ": index" + " out of" + " range."),
																				false);
																return 1;
															}

															list.remove(index - 1);
															CONFIG.save();
															ctx.getSource()
																	.sendSuccess(
																			() ->
																					Component.literal(
																							title + ": removed" + " #" + index + "."),
																			false);
															return 1;
														}))
								.then(
										literal("all")
												.executes(
														ctx -> {
															if (list.isEmpty()) {
																ctx.getSource()
																		.sendSuccess(
																				() -> Component.literal(title + " is already" + " empty."),
																				false);
																return 1;
															}
															list.clear();
															CONFIG.save();
															ctx.getSource()
																	.sendSuccess(
																			() ->
																					Component.literal(
																							title + ": all" + " entries" + " cleared."),
																			false);
															return 1;
														})))
				.then(
						literal("list")
								.executes(
										ctx -> {
											sendList(ctx.getSource(), title, list);
											return 1;
										}));
	}

	private static boolean shouldGreet(ServerPlayer player) {
		String name = player.getName().getString();

		if (CONFIG.serverBlacklist.match(name) && !CONFIG.serverBlacklistExcept.match(name)) {
			return false;
		}

		if (!CONFIG.serverWhitelist.isEmpty()
				&& (!CONFIG.serverWhitelist.match(name) || CONFIG.serverWhitelistExcept.match(name))) {
			return false;
		}

		return true;
	}

	@Override
	public void onInitializeServer() {
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> {
					if (!CONFIG.serverEnabled) {
						return;
					}

					ServerPlayer player = handler.player;
					if (!shouldGreet(player)) {
						return;
					}

					AutoGreetingServerDelay.greetAfter1Second(player);
				});

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> {
					dispatcher.register(
							literal("servergreet")
									.requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
									.then(
											literal("status")
													.executes(
															ctx -> {
																ctx.getSource()
																		.sendSuccess(
																				() ->
																						Component.literal(
																								"Auto greeting"
																										+ " "
																										+ (CONFIG.serverEnabled
																												? "enabled"
																												: "disabled")
																										+ "."),
																				false);
																return 1;
															})
													.then(
															literal("enable")
																	.executes(
																			ctx -> {
																				CONFIG.serverEnabled = true;
																				CONFIG.save();
																				ctx.getSource()
																						.sendSuccess(
																								() ->
																										Component.literal(
																												"Auto greeting" + " enabled."),
																								false);
																				return 1;
																			}))
													.then(
															literal("disable")
																	.executes(
																			ctx -> {
																				CONFIG.serverEnabled = false;
																				CONFIG.save();
																				ctx.getSource()
																						.sendSuccess(
																								() ->
																										Component.literal(
																												"Auto greeting" + " disabled."),
																								false);
																				return 1;
																			}))
													.then(
															literal("toggle")
																	.executes(
																			ctx -> {
																				CONFIG.serverEnabled = !CONFIG.serverEnabled;
																				CONFIG.save();
																				ctx.getSource()
																						.sendSuccess(
																								() ->
																										Component.literal(
																												"Auto greeting"
																														+ " is "
																														+ (CONFIG.serverEnabled
																																? "enabled"
																																: "disabled")
																														+ "."),
																								false);
																				return 1;
																			})))
									.then(
											buildStringListNode("message", "Auto greeting", CONFIG.serverGreetings, true))
									.then(
											literal("blacklist")
													.then(
															literal("match")
																	.then(
																			buildStringListNode(
																					"equal",
																					"Blacklist" + " (Name" + " Equal)",
																					CONFIG.serverBlacklist.equal,
																					false))
																	.then(
																			buildStringListNode(
																					"contain",
																					"Blacklist" + " (Name" + " Contain)",
																					CONFIG.serverBlacklist.contain,
																					false))
																	.then(
																			buildStringListNode(
																					"startWith",
																					"Blacklist" + " (Name" + " Starts" + " with)",
																					CONFIG.serverBlacklist.startWith,
																					false))
																	.then(
																			buildStringListNode(
																					"endWith",
																					"Blacklist" + " (Name" + " Ends" + " with)",
																					CONFIG.serverBlacklist.endWith,
																					false))
																	.then(
																			literal("list")
																					.executes(
																							ctx -> {
																								sendList(
																										ctx.getSource(),
																										"Match" + " (Name" + " Equal)",
																										CONFIG.serverBlacklist.equal);
																								sendList(
																										ctx.getSource(),
																										"Match" + " (Name" + " Contain)",
																										CONFIG.serverBlacklist.contain);
																								sendList(
																										ctx.getSource(),
																										"Match" + " (Name" + " Starts" + " with)",
																										CONFIG.serverBlacklist.startWith);
																								sendList(
																										ctx.getSource(),
																										"Match" + " (Name" + " Ends" + " with)",
																										CONFIG.serverBlacklist.endWith);
																								return 1;
																							})))
													.then(
															literal("except")
																	.then(
																			buildStringListNode(
																					"equal",
																					"Except (Name" + " Equal)",
																					CONFIG.serverBlacklistExcept.equal,
																					false))
																	.then(
																			buildStringListNode(
																					"contain",
																					"Except (Name" + " Contain)",
																					CONFIG.serverBlacklistExcept.contain,
																					false))
																	.then(
																			buildStringListNode(
																					"startWith",
																					"Except (Name" + " Starts" + " with)",
																					CONFIG.serverBlacklistExcept.startWith,
																					false))
																	.then(
																			buildStringListNode(
																					"endWith",
																					"Except (Name" + " Ends" + " with)",
																					CONFIG.serverBlacklistExcept.endWith,
																					false))
																	.then(
																			literal("list")
																					.executes(
																							ctx -> {
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Equal)",
																										CONFIG.serverBlacklistExcept.equal);
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Contain)",
																										CONFIG.serverBlacklistExcept.contain);
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Starts" + " with)",
																										CONFIG.serverBlacklistExcept.startWith);
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Ends" + " with)",
																										CONFIG.serverBlacklistExcept.endWith);
																								return 1;
																							})))
													.then(
															literal("list")
																	.executes(
																			ctx -> {
																				sendList(
																						ctx.getSource(),
																						"Match" + " (Name" + " Equal)",
																						CONFIG.serverBlacklist.equal);
																				sendList(
																						ctx.getSource(),
																						"Match" + " (Name" + " Contain)",
																						CONFIG.serverBlacklist.contain);
																				sendList(
																						ctx.getSource(),
																						"Match" + " (Name" + " Starts" + " with)",
																						CONFIG.serverBlacklist.startWith);
																				sendList(
																						ctx.getSource(),
																						"Match" + " (Name" + " Ends" + " with)",
																						CONFIG.serverBlacklist.endWith);

																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Equal)",
																						CONFIG.serverBlacklistExcept.equal);
																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Contain)",
																						CONFIG.serverBlacklistExcept.contain);
																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Starts" + " with)",
																						CONFIG.serverBlacklistExcept.startWith);
																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Ends" + " with)",
																						CONFIG.serverBlacklistExcept.endWith);
																				return 1;
																			}))
													.then(
															literal("clear")
																	.then(
																			literal("confirm")
																					.executes(
																							ctx -> {
																								CONFIG.serverBlacklist.clear();
																								CONFIG.serverBlacklistExcept.clear();
																								CONFIG.save();
																								ctx.getSource()
																										.sendSuccess(
																												() ->
																														Component.literal(
																																"Blacklist" + " cleared."),
																												false);
																								return 1;
																							}))))
									.then(
											literal("whitelist")
													.then(
															literal("match")
																	.then(
																			buildStringListNode(
																					"equal",
																					"Whitelist" + " (Name" + " Equal)",
																					CONFIG.serverWhitelist.equal,
																					false))
																	.then(
																			buildStringListNode(
																					"contain",
																					"Whitelist" + " (Name" + " Contain)",
																					CONFIG.serverWhitelist.contain,
																					false))
																	.then(
																			buildStringListNode(
																					"startWith",
																					"Whitelist" + " (Name" + " Starts" + " with)",
																					CONFIG.serverWhitelist.startWith,
																					false))
																	.then(
																			buildStringListNode(
																					"endWith",
																					"Whitelist" + " (Name" + " Ends" + " with)",
																					CONFIG.serverWhitelist.endWith,
																					false))
																	.then(
																			literal("list")
																					.executes(
																							ctx -> {
																								sendList(
																										ctx.getSource(),
																										"Whitelist" + " (Name" + " Equal)",
																										CONFIG.serverWhitelist.equal);
																								sendList(
																										ctx.getSource(),
																										"Whitelist" + " (Name" + " Contain)",
																										CONFIG.serverWhitelist.contain);
																								sendList(
																										ctx.getSource(),
																										"Whitelist" + " (Name" + " Starts" + " with)",
																										CONFIG.serverWhitelist.startWith);
																								sendList(
																										ctx.getSource(),
																										"Whitelist" + " (Name" + " Ends" + " with)",
																										CONFIG.serverWhitelist.endWith);
																								return 1;
																							})))
													.then(
															literal("except")
																	.then(
																			buildStringListNode(
																					"equal",
																					"Except (Name" + " Equal)",
																					CONFIG.serverWhitelistExcept.equal,
																					false))
																	.then(
																			buildStringListNode(
																					"contain",
																					"Except (Name" + " Contain)",
																					CONFIG.serverWhitelistExcept.contain,
																					false))
																	.then(
																			buildStringListNode(
																					"startWith",
																					"Except (Name" + " Starts" + " with)",
																					CONFIG.serverWhitelistExcept.startWith,
																					false))
																	.then(
																			buildStringListNode(
																					"endWith",
																					"Except (Name" + " Ends" + " with)",
																					CONFIG.serverWhitelistExcept.endWith,
																					false))
																	.then(
																			literal("list")
																					.executes(
																							ctx -> {
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Equal)",
																										CONFIG.serverWhitelistExcept.equal);
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Contain)",
																										CONFIG.serverWhitelistExcept.contain);
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Starts" + " with)",
																										CONFIG.serverWhitelistExcept.startWith);
																								sendList(
																										ctx.getSource(),
																										"Except" + " (Name" + " Ends" + " with)",
																										CONFIG.serverWhitelistExcept.endWith);
																								return 1;
																							})))
													.then(
															literal("list")
																	.executes(
																			ctx -> {
																				sendList(
																						ctx.getSource(),
																						"Whitelist" + " (Name" + " Equal)",
																						CONFIG.serverWhitelist.equal);
																				sendList(
																						ctx.getSource(),
																						"Whitelist" + " (Name" + " Contain)",
																						CONFIG.serverWhitelist.contain);
																				sendList(
																						ctx.getSource(),
																						"Whitelist" + " (Name" + " Starts" + " with)",
																						CONFIG.serverWhitelist.startWith);
																				sendList(
																						ctx.getSource(),
																						"Whitelist" + " (Name" + " Ends" + " with)",
																						CONFIG.serverWhitelist.endWith);

																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Equal)",
																						CONFIG.serverWhitelistExcept.equal);
																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Contain)",
																						CONFIG.serverWhitelistExcept.contain);
																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Starts" + " with)",
																						CONFIG.serverWhitelistExcept.startWith);
																				sendList(
																						ctx.getSource(),
																						"Except" + " (Name" + " Ends" + " with)",
																						CONFIG.serverWhitelistExcept.endWith);
																				return 1;
																			}))
													.then(
															literal("clear")
																	.then(
																			literal("confirm")
																					.executes(
																							ctx -> {
																								CONFIG.serverWhitelist.clear();
																								CONFIG.serverWhitelistExcept.clear();
																								CONFIG.save();
																								ctx.getSource()
																										.sendSuccess(
																												() ->
																														Component.literal(
																																"Whitelist" + " cleared."),
																												false);
																								return 1;
																							})))));
				});
	}
}

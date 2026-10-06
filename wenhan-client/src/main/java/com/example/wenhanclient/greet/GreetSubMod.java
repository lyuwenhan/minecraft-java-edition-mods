package com.example.wenhanclient.greet;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.greet.rules.StringMatchRules;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class GreetSubMod {
	public static long joinWorldAt = 0L;
	private static GreetConfig config;

	private GreetSubMod() {}

	public static void init(GreetConfig greetConfig) {
		config = greetConfig;
		config.fillMissing();
		GreetDelay.init();
		ClientPlayConnectionEvents.JOIN.register(
				(handler, sender, client) -> {
					joinWorldAt = System.currentTimeMillis();
					GreetDelay.resetPlayerTracking();
					if (config.selfEnabled) {
						GreetDelay.greetSelfAfter1Second();
					}
				});
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) ->
						dispatcher.register(
								literal("wc")
										.then(
												literal("greet")
														.then(selfNode())
														.then(otherNode()))));
	}

	public static GreetConfig config() {
		return config;
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> selfNode() {
		return literal("self")
				.then(statusNode(() -> config.selfEnabled, enabled -> config.selfEnabled = enabled))
				.then(buildStringListNode("message", "Auto greeting", config.selfGreetings, true));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> otherNode() {
		return literal("other")
				.then(
						statusNode(
								() -> config.otherEnabled,
								enabled -> config.otherEnabled = enabled))
				.then(buildStringListNode("message", "Auto greeting", config.otherGreetings, true))
				.then(
						ruleGroupNode(
								"blacklist", config.otherBlacklist, config.otherBlacklistExcept))
				.then(
						ruleGroupNode(
								"whitelist", config.otherWhitelist, config.otherWhitelistExcept));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> statusNode(
			BooleanGetter getter, BooleanSetter setter) {
		return literal("status")
				.executes(
						ctx -> {
							ctx.getSource()
									.sendFeedback(
											Component.literal(
													"Auto greeting "
															+ (getter.get()
																	? "enabled"
																	: "disabled")
															+ "."));
							return 1;
						})
				.then(
						literal("enable")
								.executes(
										ctx -> {
											setter.set(true);
											save();
											ctx.getSource()
													.sendFeedback(
															Component.literal(
																	"Auto greeting enabled."));
											return 1;
										}))
				.then(
						literal("disable")
								.executes(
										ctx -> {
											setter.set(false);
											save();
											ctx.getSource()
													.sendFeedback(
															Component.literal(
																	"Auto greeting disabled."));
											return 1;
										}))
				.then(
						literal("toggle")
								.executes(
										ctx -> {
											setter.set(!getter.get());
											save();
											ctx.getSource()
													.sendFeedback(
															Component.literal(
																	"Auto greeting is "
																			+ (getter.get()
																					? "enabled"
																					: "disabled")
																			+ "."));
											return 1;
										}));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> ruleGroupNode(
			String name, StringMatchRules match, StringMatchRules except) {
		return literal(name)
				.then(
						literal("match")
								.then(
										buildRuleNode(
												"equal",
												nameTitle(name, "Name Equal"),
												match.equal))
								.then(
										buildRuleNode(
												"contain",
												nameTitle(name, "Name Contain"),
												match.contain))
								.then(
										buildRuleNode(
												"startWith",
												nameTitle(name, "Name Starts with"),
												match.startWith))
								.then(
										buildRuleNode(
												"endWith",
												nameTitle(name, "Name Ends with"),
												match.endWith))
								.then(listRulesNode("list", nameTitle(name, "Name"), match)))
				.then(
						literal("except")
								.then(buildRuleNode("equal", "Except (Name Equal)", except.equal))
								.then(
										buildRuleNode(
												"contain", "Except (Name Contain)", except.contain))
								.then(
										buildRuleNode(
												"startWith",
												"Except (Name Starts with)",
												except.startWith))
								.then(
										buildRuleNode(
												"endWith",
												"Except (Name Ends with)",
												except.endWith))
								.then(listRulesNode("list", "Except", except)))
				.then(
						literal("list")
								.executes(
										ctx -> {
											sendRules(
													ctx.getSource(),
													nameTitle(name, "Name"),
													match);
											sendRules(ctx.getSource(), "Except", except);
											return 1;
										}))
				.then(
						literal("clear")
								.then(
										literal("confirm")
												.executes(
														ctx -> {
															match.clear();
															except.clear();
															save();
															ctx.getSource()
																	.sendFeedback(
																			Component.literal(
																					titleCase(name)
																							+ " cleared."));
															return 1;
														})));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> buildRuleNode(
			String name, String title, List<String> list) {
		return buildStringListNode(name, title, list, false);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> listRulesNode(
			String name, String title, StringMatchRules rules) {
		return literal(name)
				.executes(
						ctx -> {
							sendRules(ctx.getSource(), title, rules);
							return 1;
						});
	}

	private static void sendRules(
			FabricClientCommandSource source, String title, StringMatchRules rules) {
		sendList(source, title + " (Name Equal)", rules.equal);
		sendList(source, title + " (Name Contain)", rules.contain);
		sendList(source, title + " (Name Starts with)", rules.startWith);
		sendList(source, title + " (Name Ends with)", rules.endWith);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> buildStringListNode(
			String name, String title, List<String> list, boolean allowInsertIndex) {
		String argumentName = allowInsertIndex ? "message" : "pattern";
		RequiredArgumentBuilder<FabricClientCommandSource, String> addArg =
				argument(argumentName, StringArgumentType.greedyString());
		addArg =
				addArg.executes(
						ctx -> {
							String value = StringArgumentType.getString(ctx, argumentName);
							if (!allowInsertIndex && list.contains(value)) {
								ctx.getSource()
										.sendFeedback(
												Component.literal(
														title
																+ ": \""
																+ value
																+ "\" already exists."));
								return 1;
							}
							list.add(value);
							save();
							ctx.getSource()
									.sendFeedback(
											Component.literal(
													title + ": appended \"" + value + "\"."));
							return 1;
						});
		if (allowInsertIndex) {
			addArg =
					addArg.then(
							argument("index", IntegerArgumentType.integer(1))
									.executes(
											ctx -> {
												String value =
														StringArgumentType.getString(
																ctx, argumentName);
												int index =
														IntegerArgumentType.getInteger(
																ctx, "index");
												boolean isAppend = index > list.size();
												int pos =
														Math.max(
																0,
																Math.min(index - 1, list.size()));
												list.add(pos, value);
												save();
												ctx.getSource()
														.sendFeedback(
																Component.literal(
																		isAppend
																				? title
																						+ ": appended"
																						+ " \""
																						+ value
																						+ "\"."
																				: title
																						+ ": inserted"
																						+ " \""
																						+ value
																						+ "\" at"
																						+ " position"
																						+ " "
																						+ index
																						+ "."));
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
														.sendFeedback(
																Component.literal(
																		title + " is empty."));
												return 1;
											}
											list.remove(list.size() - 1);
											save();
											ctx.getSource()
													.sendFeedback(
															Component.literal(
																	title
																			+ ": removed last"
																			+ " item."));
											return 1;
										})
								.then(
										argument("index", IntegerArgumentType.integer(1))
												.executes(
														ctx -> {
															int index =
																	IntegerArgumentType.getInteger(
																			ctx, "index");
															if (index < 1 || index > list.size()) {
																ctx.getSource()
																		.sendFeedback(
																				Component.literal(
																						title
																								+ ": index"
																								+ " out of"
																								+ " range."));
																return 1;
															}
															list.remove(index - 1);
															save();
															ctx.getSource()
																	.sendFeedback(
																			Component.literal(
																					title
																							+ ": removed"
																							+ " #"
																							+ index
																							+ "."));
															return 1;
														}))
								.then(
										literal("all")
												.executes(
														ctx -> {
															if (list.isEmpty()) {
																ctx.getSource()
																		.sendFeedback(
																				Component.literal(
																						title
																								+ " is already"
																								+ " empty."));
																return 1;
															}
															list.clear();
															save();
															ctx.getSource()
																	.sendFeedback(
																			Component.literal(
																					title
																							+ ": all"
																							+ " entries"
																							+ " cleared."));
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

	private static void sendList(
			FabricClientCommandSource source, String title, List<String> list) {
		if (list.isEmpty()) {
			source.sendFeedback(Component.literal(title + ": <empty>"));
			return;
		}
		source.sendFeedback(Component.literal(title + ":"));
		int i = 1;
		for (String s : list) {
			source.sendFeedback(Component.literal(i++ + ". " + s));
		}
	}

	private static String nameTitle(String groupName, String suffix) {
		return titleCase(groupName) + " (" + suffix + ")";
	}

	private static String titleCase(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

	private static void save() {
		WenhanClientMod.CONFIG.save();
	}

	private interface BooleanGetter {
		boolean get();
	}

	private interface BooleanSetter {
		void set(boolean value);
	}
}

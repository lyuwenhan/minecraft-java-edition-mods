package com.example.entityhighlighter;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class EntityHighlighterMod implements ClientModInitializer {
	public static final String MOD_ID = "entity-highlighter";
	private static final int DEFAULT_COLOR = 0xFFFFFF;
	private static final List<String> GROUPS = List.of("animal", "monster", "everything");

	public static EntityHighlighterConfig config;
	private static final ThreadLocal<Entity> GLOW_OVERRIDE_BYPASS = new ThreadLocal<>();

	@Override
	public void onInitializeClient() {
		config = EntityHighlighterConfig.load();
		registerCommands();
		System.out.println("[EntityHighlighter] Client initialized");
	}

	private static void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) ->
						dispatcher.register(
								ClientCommands.literal("entityhighlighter")
										.then(
												ClientCommands.literal("add")
														.then(
																ClientCommands.literal("type")
																		.then(
																				ClientCommands
																						.argument(
																								"entityid",
																								IdentifierArgument
																										.id())
																						.suggests(
																								(context,
																										builder) ->
																										suggestEntityIds(
																												builder,
																												false))
																						.executes(
																								context ->
																										addType(
																												context
																														.getSource(),
																												context
																														.getArgument(
																																"entityid",
																																Identifier
																																		.class),
																												DEFAULT_COLOR))
																						.then(
																								ClientCommands
																										.argument(
																												"color",
																												StringArgumentType
																														.greedyString())
																										.executes(
																												context ->
																														addType(
																																context
																																		.getSource(),
																																context
																																		.getArgument(
																																				"entityid",
																																				Identifier
																																						.class),
																																parseColorOrReport(
																																		context
																																				.getSource(),
																																		StringArgumentType
																																				.getString(
																																						context,
																																						"color")))))))
														.then(
																ClientCommands.literal("group")
																		.then(
																				ClientCommands
																						.argument(
																								"groupname",
																								StringArgumentType
																										.word())
																						.suggests(
																								(context,
																										builder) ->
																										suggestGroupNames(
																												builder,
																												false))
																						.executes(
																								context ->
																										addGroup(
																												context
																														.getSource(),
																												StringArgumentType
																														.getString(
																																context,
																																"groupname"),
																												DEFAULT_COLOR))
																						.then(
																								ClientCommands
																										.argument(
																												"color",
																												StringArgumentType
																														.greedyString())
																										.executes(
																												context ->
																														addGroup(
																																context
																																		.getSource(),
																																StringArgumentType
																																		.getString(
																																				context,
																																				"groupname"),
																																parseColorOrReport(
																																		context
																																				.getSource(),
																																		StringArgumentType
																																				.getString(
																																						context,
																																						"color"))))))))
										.then(
												ClientCommands.literal("remove")
														.then(
																ClientCommands.literal("type")
																		.then(
																				ClientCommands
																						.argument(
																								"entityid",
																								IdentifierArgument
																										.id())
																						.suggests(
																								(context,
																										builder) ->
																										suggestEntityIds(
																												builder,
																												true))
																						.executes(
																								context ->
																										removeType(
																												context
																														.getSource(),
																												context
																														.getArgument(
																																"entityid",
																																Identifier
																																		.class)))
																						.then(
																								ClientCommands
																										.argument(
																												"color",
																												StringArgumentType
																														.greedyString())
																										.executes(
																												context ->
																														removeTypeWithColorValidation(
																																context
																																		.getSource(),
																																context
																																		.getArgument(
																																				"entityid",
																																				Identifier
																																						.class),
																																StringArgumentType
																																		.getString(
																																				context,
																																				"color"))))))
														.then(
																ClientCommands.literal("group")
																		.then(
																				ClientCommands
																						.argument(
																								"groupname",
																								StringArgumentType
																										.word())
																						.suggests(
																								(context,
																										builder) ->
																										suggestGroupNames(
																												builder,
																												true))
																						.executes(
																								context ->
																										removeGroup(
																												context
																														.getSource(),
																												StringArgumentType
																														.getString(
																																context,
																																"groupname")))
																						.then(
																								ClientCommands
																										.argument(
																												"color",
																												StringArgumentType
																														.greedyString())
																										.executes(
																												context ->
																														removeGroupWithColorValidation(
																																context
																																		.getSource(),
																																StringArgumentType
																																		.getString(
																																				context,
																																				"groupname"),
																																StringArgumentType
																																		.getString(
																																				context,
																																				"color")))))))
										.then(
												ClientCommands.literal("purge")
														.then(
																ClientCommands.literal("confirm")
																		.executes(
																				context ->
																						purge(
																								context
																										.getSource()))))
										.then(
												ClientCommands.literal("status")
														.executes(
																context ->
																		status(
																				context
																						.getSource())))));
	}

	private static CompletableFuture<Suggestions> suggestEntityIds(
			SuggestionsBuilder builder, boolean existingOnly) {
		if (!existingOnly) {
			return SharedSuggestionProvider.suggestResource(
					BuiltInRegistries.ENTITY_TYPE.keySet(), builder);
		}

		Set<String> configuredTypes = new LinkedHashSet<>();
		EntityHighlighterConfig currentConfig = config;
		if (currentConfig != null && currentConfig.typeRules != null) {
			for (TypeHighlightRule rule : currentConfig.typeRules) {
				if (rule != null && rule.entityId != null) {
					configuredTypes.add(rule.entityId);
				}
			}
		}
		return suggestStrings(configuredTypes, builder);
	}

	private static CompletableFuture<Suggestions> suggestGroupNames(
			SuggestionsBuilder builder, boolean existingOnly) {
		if (!existingOnly) {
			return suggestStrings(GROUPS, builder);
		}

		Set<String> configuredGroups = new LinkedHashSet<>();
		EntityHighlighterConfig currentConfig = config;
		if (currentConfig != null && currentConfig.groupRules != null) {
			for (GroupHighlightRule rule : currentConfig.groupRules) {
				if (rule != null && rule.groupName != null && isValidGroup(rule.groupName)) {
					configuredGroups.add(rule.groupName);
				}
			}
		}
		return suggestStrings(configuredGroups, builder);
	}

	private static CompletableFuture<Suggestions> suggestStrings(
			Iterable<String> values, SuggestionsBuilder builder) {
		String remaining = builder.getRemainingLowerCase();
		for (String value : values) {
			if (value != null && value.toLowerCase(java.util.Locale.ROOT).startsWith(remaining)) {
				builder.suggest(value);
			}
		}
		return builder.buildFuture();
	}

	private static int addType(FabricClientCommandSource source, Identifier entityId, int color) {
		if (color < 0) {
			return 0;
		}

		if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
			source.sendError(Component.literal("Unknown entity type: " + entityId));
			return 0;
		}

		upsertTypeRule(entityId.toString(), color);
		source.sendFeedback(
				Component.literal(
						"Entity Highlighter: added type "
								+ entityId
								+ " with color #"
								+ formatColor(color)));
		return 1;
	}

	private static int addGroup(FabricClientCommandSource source, String groupName, int color) {
		if (color < 0) {
			return 0;
		}
		if (!isValidGroup(groupName)) {
			source.sendError(
					Component.literal(
							"Unknown group: "
									+ groupName
									+ ". Valid groups: animal, monster, everything"));
			return 0;
		}

		upsertGroupRule(groupName, color);
		source.sendFeedback(
				Component.literal(
						"Entity Highlighter: added group "
								+ groupName
								+ " with color #"
								+ formatColor(color)));
		return 1;
	}

	private static int removeType(FabricClientCommandSource source, Identifier entityId) {
		return removeTypeRule(source, entityId.toString());
	}

	private static int removeTypeWithColorValidation(
			FabricClientCommandSource source, Identifier entityId, String color) {
		if (parseColorOrReport(source, color) < 0) {
			return 0;
		}
		return removeType(source, entityId);
	}

	private static int removeGroupWithColorValidation(
			FabricClientCommandSource source, String groupName, String color) {
		if (parseColorOrReport(source, color) < 0) {
			return 0;
		}
		return removeGroup(source, groupName);
	}

	private static int removeGroup(FabricClientCommandSource source, String groupName) {
		if (!isValidGroup(groupName)) {
			source.sendError(
					Component.literal(
							"Unknown group: "
									+ groupName
									+ ". Valid groups: animal, monster, everything"));
			return 0;
		}
		return removeGroupRule(source, groupName);
	}

	private static int removeTypeRule(FabricClientCommandSource source, String entityId) {
		boolean removed =
				config.typeRules.removeIf(rule -> rule != null && entityId.equals(rule.entityId));
		if (!removed) {
			source.sendError(
					Component.literal("No matching Entity Highlighter type rule: " + entityId));
			return 0;
		}

		config.save();
		source.sendFeedback(Component.literal("Entity Highlighter: removed type " + entityId));
		return 1;
	}

	private static int removeGroupRule(FabricClientCommandSource source, String groupName) {
		boolean removed =
				config.groupRules.removeIf(
						rule -> rule != null && groupName.equals(rule.groupName));
		if (!removed) {
			source.sendError(
					Component.literal("No matching Entity Highlighter group rule: " + groupName));
			return 0;
		}

		config.save();
		source.sendFeedback(Component.literal("Entity Highlighter: removed group " + groupName));
		return 1;
	}

	private static int purge(FabricClientCommandSource source) {
		int count = config.typeRules.size() + config.groupRules.size();
		config.typeRules.clear();
		config.groupRules.clear();
		config.save();
		source.sendFeedback(Component.literal("Entity Highlighter: purged " + count + " rule(s)."));
		return 1;
	}

	private static int status(FabricClientCommandSource source) {
		List<TypeHighlightRule> typeRules = config.typeRules;
		List<GroupHighlightRule> groupRules =
				config.groupRules.stream()
						.filter(
								rule ->
										rule != null
												&& rule.groupName != null
												&& isValidGroup(rule.groupName))
						.toList();

		source.sendFeedback(
				Component.literal(
						"Entity Highlighter status: "
								+ typeRules.size()
								+ " type rule(s), "
								+ groupRules.size()
								+ " group rule(s)"));

		source.sendFeedback(Component.literal("Type rules:"));
		if (typeRules.isEmpty()) {
			source.sendFeedback(Component.literal("  (none)"));
		} else {
			for (TypeHighlightRule rule : typeRules) {
				if (rule != null && rule.entityId != null) {
					source.sendFeedback(
							Component.literal(
									"  " + rule.entityId + " " + formatColor(rule.color)));
				}
			}
		}

		source.sendFeedback(Component.literal("Group rules:"));
		if (groupRules.isEmpty()) {
			source.sendFeedback(Component.literal("  (none)"));
		} else {
			for (GroupHighlightRule rule : groupRules) {
				if (rule != null && rule.groupName != null) {
					source.sendFeedback(
							Component.literal(
									"  " + rule.groupName + " " + formatColor(rule.color)));
				}
			}
		}

		return 1;
	}

	private static void upsertTypeRule(String entityId, int color) {
		config.typeRules.removeIf(rule -> rule != null && entityId.equals(rule.entityId));
		config.typeRules.add(new TypeHighlightRule(entityId, color));
		config.save();
	}

	private static void upsertGroupRule(String groupName, int color) {
		config.groupRules.removeIf(rule -> rule != null && groupName.equals(rule.groupName));
		config.groupRules.add(new GroupHighlightRule(groupName, color));
		config.save();
	}

	private static int parseColorOrReport(FabricClientCommandSource source, String raw) {
		if (raw.length() != 6) {
			source.sendError(
					Component.literal(
							"Invalid color: "
									+ raw
									+ ". Use exactly 6 hexadecimal digits (RRGGBB)."));
			return -1;
		}

		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			boolean digit = c >= '0' && c <= '9';
			boolean lower = c >= 'a' && c <= 'f';
			boolean upper = c >= 'A' && c <= 'F';
			if (!digit && !lower && !upper) {
				source.sendError(
						Component.literal(
								"Invalid color: "
										+ raw
										+ ". Use exactly 6 hexadecimal digits (RRGGBB)."));
				return -1;
			}
		}

		return Integer.parseInt(raw, 16);
	}

	private static String formatColor(int color) {
		return String.format("%06X", color & 0xFFFFFF);
	}

	private static boolean isValidGroup(String group) {
		return "animal".equals(group) || "monster".equals(group) || "everything".equals(group);
	}

	public static boolean isGlowOverrideBypassed(Entity entity) {
		return GLOW_OVERRIDE_BYPASS.get() == entity;
	}

	public static boolean isGlowingWithoutEntityHighlighter(Entity entity) {
		Entity previous = GLOW_OVERRIDE_BYPASS.get();
		GLOW_OVERRIDE_BYPASS.set(entity);
		try {
			return entity.isCurrentlyGlowing();
		} finally {
			if (previous == null) {
				GLOW_OVERRIDE_BYPASS.remove();
			} else {
				GLOW_OVERRIDE_BYPASS.set(previous);
			}
		}
	}

	public static Integer getHighlightColor(Entity entity) {
		EntityHighlighterConfig currentConfig = config;
		if (currentConfig == null) {
			return null;
		}

		boolean hasTypeRules =
				currentConfig.typeRules != null && !currentConfig.typeRules.isEmpty();
		boolean hasGroupRules =
				currentConfig.groupRules != null && !currentConfig.groupRules.isEmpty();
		if (!hasTypeRules && !hasGroupRules) {
			return null;
		}

		Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		if (entityId != null && currentConfig.typeRules != null) {
			String entityIdString = entityId.toString();
			for (int i = currentConfig.typeRules.size() - 1; i >= 0; i--) {
				TypeHighlightRule rule = currentConfig.typeRules.get(i);
				if (rule != null && entityIdString.equals(rule.entityId)) {
					return rule.color & 0xFFFFFF;
				}
			}
		}

		if (currentConfig.groupRules != null) {
			for (int i = currentConfig.groupRules.size() - 1; i >= 0; i--) {
				GroupHighlightRule rule = currentConfig.groupRules.get(i);
				if (rule != null
						&& rule.groupName != null
						&& matchesGroupRule(entity, rule.groupName)) {
					return rule.color & 0xFFFFFF;
				}
			}
		}

		return null;
	}

	private static boolean matchesGroupRule(Entity entity, String groupName) {
		MobCategory category = entity.getType().getCategory();
		return switch (groupName) {
			case "animal" -> isAnimalCategory(category);
			case "monster" -> category == MobCategory.MONSTER;
			case "everything" -> true;
			default -> false;
		};
	}

	private static boolean isAnimalCategory(MobCategory category) {
		return category == MobCategory.CREATURE
				|| category == MobCategory.AMBIENT
				|| category == MobCategory.AXOLOTLS
				|| category == MobCategory.UNDERGROUND_WATER_CREATURE
				|| category == MobCategory.WATER_AMBIENT
				|| category == MobCategory.WATER_CREATURE;
	}
}

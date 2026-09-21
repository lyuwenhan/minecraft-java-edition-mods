package com.example.sharedplayerdata;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class SharedPlayerDataCommands {
	private static final String ROOT_LITERAL = "playerbind";
	private static final String NAMES_ARGUMENT = "names";
	private static final String NAME_ARGUMENT = "name";
	private static CommandDispatcher<CommandSourceStack> dispatcher;

	private static final Field CHILDREN_FIELD = commandNodeField("children");
	private static final Field LITERALS_FIELD = commandNodeField("literals");
	private static final Field ARGUMENTS_FIELD = commandNodeField("arguments");

	private SharedPlayerDataCommands() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register(
				(currentDispatcher, registryAccess, environment) -> {
					if (!environment.includeDedicated) {
						return;
					}
					dispatcher = currentDispatcher;
					registerCurrentTree(currentDispatcher);
				});
	}

	private static void registerCurrentTree(
			CommandDispatcher<CommandSourceStack> targetDispatcher) {
		var root =
				Commands.literal(ROOT_LITERAL)
						.requires(
								source ->
										source.permissions()
												.hasPermission(Permissions.COMMANDS_OWNER));
		var groupRoot = Commands.literal("group");

		groupRoot.then(
				Commands.literal("add")
						.then(
								Commands.argument(NAMES_ARGUMENT, StringArgumentType.greedyString())
										.suggests(
												(context, builder) ->
														suggestAddPlayerNames(context, builder))
										.executes(
												SharedPlayerDataCommands
														::executeGroupCreateWithPlayers)));

		int groupCount = SharedPlayerDataMod.MANAGER.groupCount();
		for (int groupNumber = 1; groupNumber <= groupCount; groupNumber++) {
			groupRoot.then(buildExistingGroupNode(groupNumber));
		}

		root.then(groupRoot);
		root.then(
				Commands.literal("find")
						.then(
								Commands.argument(NAME_ARGUMENT, StringArgumentType.word())
										.suggests(
												SharedPlayerDataCommands
														::suggestKnownAndOnlinePlayerNames)
										.executes(SharedPlayerDataCommands::executeFind)));
		root.then(Commands.literal("list").executes(SharedPlayerDataCommands::executeList));
		targetDispatcher.register(root);
	}

	private static LiteralCommandNode<CommandSourceStack> buildExistingGroupNode(int groupNumber) {
		var groupNode = Commands.literal(Integer.toString(groupNumber));
		groupNode.then(
				Commands.literal("add")
						.then(
								Commands.argument(NAMES_ARGUMENT, StringArgumentType.greedyString())
										.suggests(
												(context, builder) ->
														suggestAddPlayerNames(context, builder))
										.executes(
												context ->
														executeGroupAddPlayers(
																context, groupNumber))));
		groupNode.then(
				Commands.literal("list")
						.executes(context -> executeGroupList(context, groupNumber)));
		groupNode.then(
				Commands.literal("purge")
						.then(
								Commands.literal("confirm")
										.executes(
												context ->
														executeGroupPurgeConfirm(
																context, groupNumber))));
		groupNode.then(
				Commands.literal("remove")
						.then(
								Commands.argument(NAMES_ARGUMENT, StringArgumentType.greedyString())
										.suggests(
												(context, builder) ->
														suggestGroupMemberNames(
																context, builder, groupNumber))
										.executes(
												context ->
														executeGroupRemovePlayers(
																context, groupNumber))));
		return groupNode.build();
	}

	private static int executeGroupCreateWithPlayers(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();
		List<ServerPlayer> players = resolveAllowedAddPlayers(server, getRequestedNames(context));

		SharedProfileManager.CreateGroupResult createResult;
		try {
			createResult = SharedPlayerDataMod.MANAGER.createGroup();
			for (ServerPlayer player : players) {
				SharedPlayerDataMod.MANAGER.addOnlinePlayerToGroup(
						server, createResult.groupNumber(), player, source.getPlayer());
			}
		} catch (IOException | RuntimeException exception) {
			SharedPlayerDataMod.LOGGER.error(
					"Failed to execute /playerbind group add with players.", exception);
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
					.dispatcherParseException()
					.create("/playerbind group add failed. Check the server log.");
		}

		rebuildAndSync(server);
		source.sendSuccess(
				() ->
						Component.literal(
								"Created playerbind group "
										+ createResult.groupNumber()
										+ " with "
										+ players.size()
										+ " member(s): "
										+ joinPlayerNames(players)
										+ "."),
				true);
		return Command.SINGLE_SUCCESS;
	}

	private static int executeGroupAddPlayers(
			CommandContext<CommandSourceStack> context, int groupNumber)
			throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();
		List<ServerPlayer> players = resolveAllowedAddPlayers(server, getRequestedNames(context));

		try {
			for (ServerPlayer player : players) {
				SharedPlayerDataMod.MANAGER.addOnlinePlayerToGroup(
						server, groupNumber, player, source.getPlayer());
			}
		} catch (IOException | RuntimeException exception) {
			SharedPlayerDataMod.LOGGER.error(
					"Failed to execute /playerbind group {} add {}.",
					groupNumber,
					joinPlayerNames(players),
					exception);
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
					.dispatcherParseException()
					.create(
							"/playerbind group "
									+ groupNumber
									+ " add failed. Check the server log.");
		}

		source.sendSuccess(
				() ->
						Component.literal(
								"Added "
										+ players.size()
										+ " player(s) to playerbind group "
										+ groupNumber
										+ ": "
										+ joinPlayerNames(players)
										+ "."),
				true);
		return Command.SINGLE_SUCCESS;
	}

	private static int executeGroupList(CommandContext<CommandSourceStack> context, int groupNumber)
			throws CommandSyntaxException {
		SharedProfileManager.GroupDetails details =
				SharedPlayerDataMod.MANAGER
						.groupDetails(groupNumber)
						.orElseThrow(
								() ->
										CommandSyntaxException.BUILT_IN_EXCEPTIONS
												.dispatcherUnknownArgument()
												.create());
		context.getSource()
				.sendSuccess(() -> Component.literal(formatGroupDetails(details)), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int executeGroupPurgeConfirm(
			CommandContext<CommandSourceStack> context, int groupNumber)
			throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();
		SharedProfileManager.RemoveGroupResult result;
		try {
			result = SharedPlayerDataMod.MANAGER.removeGroup(server, groupNumber);
		} catch (IOException | RuntimeException exception) {
			SharedPlayerDataMod.LOGGER.error(
					"Failed to execute /playerbind group {} purge confirm.",
					groupNumber,
					exception);
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
					.dispatcherParseException()
					.create(
							"/playerbind group "
									+ groupNumber
									+ " purge confirm failed. Check the server log.");
		}
		rebuildAndSync(server);
		source.sendSuccess(
				() ->
						Component.literal(
								"Purged playerbind group "
										+ result.removedGroupNumber()
										+ ". Former members: "
										+ result.removedMemberCount()
										+ ". Player data, advancements, stats, OP status, and"
										+ " shared group files were left unchanged. Groups"
										+ " remaining: "
										+ result.remainingGroupCount()
										+ "."),
				true);
		return Command.SINGLE_SUCCESS;
	}

	private static int executeGroupRemovePlayers(
			CommandContext<CommandSourceStack> context, int groupNumber)
			throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();
		List<String> names = resolveAllowedRemoveNames(groupNumber, getRequestedNames(context));

		int immediateResetCount = 0;
		int pendingResetCount = 0;
		try {
			for (String name : names) {
				SharedProfileManager.RemovePlayerFromGroupResult result =
						SharedPlayerDataMod.MANAGER.removePlayerFromGroup(
								server, groupNumber, name);
				immediateResetCount += result.immediateResetCount();
				pendingResetCount += result.pendingResetCount();
			}
		} catch (IOException | RuntimeException exception) {
			SharedPlayerDataMod.LOGGER.error(
					"Failed to execute /playerbind group {} remove {}.",
					groupNumber,
					String.join(" ", names),
					exception);
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
					.dispatcherParseException()
					.create(
							"/playerbind group "
									+ groupNumber
									+ " remove failed. Check the server log.");
		}

		int finalImmediateResetCount = immediateResetCount;
		int finalPendingResetCount = pendingResetCount;
		source.sendSuccess(
				() ->
						Component.literal(
								"Removed "
										+ names.size()
										+ " player(s) from playerbind group "
										+ groupNumber
										+ ": "
										+ String.join(", ", names)
										+ ". Reset offline members: "
										+ finalImmediateResetCount
										+ ". Online members disconnected for reset: "
										+ finalPendingResetCount
										+ "."),
				true);
		return Command.SINGLE_SUCCESS;
	}

	private static int executeFind(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		MinecraftServer server = source.getServer();
		String playerName = StringArgumentType.getString(context, NAME_ARGUMENT);
		SharedProfileManager.FindPlayerResult result =
				SharedPlayerDataMod.MANAGER
						.findPlayer(server, playerName)
						.orElseThrow(
								() ->
										CommandSyntaxException.BUILT_IN_EXCEPTIONS
												.dispatcherUnknownArgument()
												.create());
		if (result.groupNumber().isPresent()) {
			int groupNumber = result.groupNumber().getAsInt();
			source.sendSuccess(
					() ->
							Component.literal(
									result.name() + " is in playerbind group " + groupNumber + "."),
					false);
		} else {
			source.sendSuccess(
					() -> Component.literal(result.name() + " is not in any playerbind group."),
					false);
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int executeList(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		SharedProfileManager.GroupList groups = SharedPlayerDataMod.MANAGER.listGroups();
		source.sendSuccess(() -> Component.literal(formatGroupList(groups)), false);
		return Command.SINGLE_SUCCESS;
	}

	private static List<String> getRequestedNames(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		String raw = StringArgumentType.getString(context, NAMES_ARGUMENT).trim();
		if (raw.isEmpty()) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
		}
		String[] tokens = raw.split("\\s+");
		List<String> names = new ArrayList<>(tokens.length);
		Set<String> seen = new LinkedHashSet<>();
		for (String token : tokens) {
			String key = token.toLowerCase(Locale.ROOT);
			if (!seen.add(key)) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
						.dispatcherUnknownArgument()
						.create();
			}
			names.add(token);
		}
		return List.copyOf(names);
	}

	private static List<ServerPlayer> resolveAllowedAddPlayers(
			MinecraftServer server, List<String> requestedNames) throws CommandSyntaxException {
		Map<String, ServerPlayer> allowedByLowerName = new LinkedHashMap<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!SharedPlayerDataMod.MANAGER.isBound(player.getUUID())) {
				allowedByLowerName.put(player.nameAndId().name().toLowerCase(Locale.ROOT), player);
			}
		}
		List<ServerPlayer> players = new ArrayList<>(requestedNames.size());
		Set<String> selected = new LinkedHashSet<>();
		for (String requestedName : requestedNames) {
			String key = requestedName.toLowerCase(Locale.ROOT);
			ServerPlayer player = allowedByLowerName.get(key);
			if (player == null || !selected.add(key)) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
						.dispatcherUnknownArgument()
						.create();
			}
			players.add(player);
		}
		return List.copyOf(players);
	}

	private static List<String> resolveAllowedRemoveNames(
			int groupNumber, List<String> requestedNames) throws CommandSyntaxException {
		SharedProfileManager.GroupDetails details =
				SharedPlayerDataMod.MANAGER
						.groupDetails(groupNumber)
						.orElseThrow(
								() ->
										CommandSyntaxException.BUILT_IN_EXCEPTIONS
												.dispatcherUnknownArgument()
												.create());
		Map<String, String> allowedByLowerName = new LinkedHashMap<>();
		for (SharedProfileManager.MemberDetails member : details.members()) {
			if (!isUuidText(member.name())) {
				allowedByLowerName.put(member.name().toLowerCase(Locale.ROOT), member.name());
			}
		}
		List<String> names = new ArrayList<>(requestedNames.size());
		Set<String> selected = new LinkedHashSet<>();
		for (String requestedName : requestedNames) {
			String key = requestedName.toLowerCase(Locale.ROOT);
			String canonicalName = allowedByLowerName.get(key);
			if (canonicalName == null || !selected.add(key)) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
						.dispatcherUnknownArgument()
						.create();
			}
			names.add(canonicalName);
		}
		return List.copyOf(names);
	}

	private static CompletableFuture<Suggestions> suggestAddPlayerNames(
			CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		List<String> allowed = new ArrayList<>();
		for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
			if (!SharedPlayerDataMod.MANAGER.isBound(player.getUUID())) {
				allowed.add(player.nameAndId().name());
			}
		}
		return suggestNextName(builder, allowed);
	}

	private static CompletableFuture<Suggestions> suggestGroupMemberNames(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder,
			int groupNumber) {
		List<String> allowed = new ArrayList<>();
		SharedPlayerDataMod.MANAGER
				.groupDetails(groupNumber)
				.ifPresent(
						details -> {
							for (SharedProfileManager.MemberDetails member : details.members()) {
								if (!isUuidText(member.name())) {
									allowed.add(member.name());
								}
							}
						});
		return suggestNextName(builder, allowed);
	}

	private static CompletableFuture<Suggestions> suggestNextName(
			SuggestionsBuilder builder, Collection<String> allowedNames) {
		String remaining = builder.getRemaining();
		int lastSpace = remaining.lastIndexOf(' ');
		String completedPart = lastSpace < 0 ? "" : remaining.substring(0, lastSpace).trim();
		String currentPart = lastSpace < 0 ? remaining : remaining.substring(lastSpace + 1);
		Set<String> selected = new LinkedHashSet<>();
		if (!completedPart.isEmpty()) {
			for (String token : completedPart.split("\\s+")) {
				selected.add(token.toLowerCase(Locale.ROOT));
			}
		}
		SuggestionsBuilder currentBuilder =
				lastSpace < 0 ? builder : builder.createOffset(builder.getStart() + lastSpace + 1);
		String lowerCurrent = currentPart.toLowerCase(Locale.ROOT);
		for (String allowedName : allowedNames) {
			String lowerAllowed = allowedName.toLowerCase(Locale.ROOT);
			if (!selected.contains(lowerAllowed) && lowerAllowed.startsWith(lowerCurrent)) {
				currentBuilder.suggest(allowedName);
			}
		}
		return currentBuilder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestKnownAndOnlinePlayerNames(
			CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		CommandSourceStack source = context.getSource();
		for (String playerName : source.getOnlinePlayerNames()) {
			suggestIfMatches(builder, playerName);
		}
		for (String playerName : SharedPlayerDataMod.MANAGER.knownPlayerNames()) {
			suggestIfMatches(builder, playerName);
		}
		return builder.buildFuture();
	}

	public static void rebuildAndSync(MinecraftServer server) {
		CommandDispatcher<CommandSourceStack> currentDispatcher = dispatcher;
		if (currentDispatcher == null) {
			return;
		}
		try {
			removeChild(currentDispatcher.getRoot(), ROOT_LITERAL);
			registerCurrentTree(currentDispatcher);
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				server.getCommands().sendCommands(player);
			}
		} catch (ReflectiveOperationException | RuntimeException exception) {
			SharedPlayerDataMod.LOGGER.error(
					"Failed to rebuild /playerbind command tree.", exception);
		}
	}

	@SuppressWarnings("unchecked")
	private static void removeChild(CommandNode<?> parent, String name)
			throws IllegalAccessException {
		CommandNode<?> child = parent.getChild(name);
		if (child == null) {
			return;
		}
		((Map<String, CommandNode<?>>) CHILDREN_FIELD.get(parent)).remove(name);
		if (child instanceof LiteralCommandNode<?>) {
			((Map<String, LiteralCommandNode<?>>) LITERALS_FIELD.get(parent)).remove(name);
		} else {
			((Map<String, CommandNode<?>>) ARGUMENTS_FIELD.get(parent)).remove(name);
		}
	}

	private static Field commandNodeField(String name) {
		try {
			Field field = CommandNode.class.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException exception) {
			throw new ExceptionInInitializerError(exception);
		}
	}

	private static String formatGroupList(SharedProfileManager.GroupList groups) {
		if (groups.groups().isEmpty()) {
			return "No playerbind groups.";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Playerbind groups:");
		for (SharedProfileManager.GroupSummary group : groups.groups()) {
			builder.append('\n');
			builder.append(group.groupNumber());
			builder.append(": ");
			builder.append(group.memberCount());
			builder.append(" member(s)");
			if (!group.memberNames().isEmpty()) {
				builder.append(" - ");
				builder.append(String.join(", ", group.memberNames()));
			}
		}
		return builder.toString();
	}

	private static String formatGroupDetails(SharedProfileManager.GroupDetails details) {
		StringBuilder builder = new StringBuilder();
		builder.append("Playerbind group ");
		builder.append(details.groupNumber());
		builder.append(" members:");
		if (details.members().isEmpty()) {
			builder.append(" none");
			return builder.toString();
		}
		for (SharedProfileManager.MemberDetails member : details.members()) {
			builder.append('\n');
			builder.append("- ");
			builder.append(member.name());
			builder.append(" (");
			builder.append(member.uuid());
			builder.append(")");
		}
		return builder.toString();
	}

	private static void suggestIfMatches(SuggestionsBuilder builder, String value) {
		String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
		String lowerValue = value.toLowerCase(Locale.ROOT);
		if (lowerValue.startsWith(remaining)) {
			builder.suggest(value);
		}
	}

	private static boolean isUuidText(String text) {
		try {
			java.util.UUID.fromString(text);
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	private static String joinPlayerNames(List<ServerPlayer> players) {
		List<String> names = new ArrayList<>(players.size());
		for (ServerPlayer player : players) {
			names.add(player.nameAndId().name());
		}
		return String.join(", ", names);
	}
}

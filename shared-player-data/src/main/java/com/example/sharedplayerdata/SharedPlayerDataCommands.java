package com.example.sharedplayerdata;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class SharedPlayerDataCommands {
    private static final String GROUP_ARGUMENT = "group";
    private static final String NAME_ARGUMENT = "name";

    private SharedPlayerDataCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    if (!environment.includeDedicated) {
                        return;
                    }

                    var root =
                            Commands.literal("playerbind")
                                    .requires(
                                            source ->
                                                    source.permissions()
                                                            .hasPermission(
                                                                    Permissions.COMMANDS_OWNER));

                    var groupRoot = Commands.literal("group");
                    groupRoot.then(
                            Commands.literal("add")
                                    .executes(SharedPlayerDataCommands::executeGroupCreate));

                    var groupNumber =
                            Commands.argument(GROUP_ARGUMENT, IntegerArgumentType.integer(1))
                                    .suggests(SharedPlayerDataCommands::suggestGroupNumbers);

                    groupNumber.then(
                            Commands.literal("add")
                                    .then(
                                            Commands.argument(
                                                            NAME_ARGUMENT,
                                                            StringArgumentType.word())
                                                    .suggests(
                                                            SharedPlayerDataCommands
                                                                    ::suggestOnlinePlayerNames)
                                                    .executes(
                                                            SharedPlayerDataCommands
                                                                    ::executeGroupAddPlayer)));

                    groupNumber.then(
                            Commands.literal("list")
                                    .executes(SharedPlayerDataCommands::executeGroupList));

                    groupNumber.then(
                            Commands.literal("remove")
                                    .then(
                                            Commands.literal("confirm")
                                                    .executes(
                                                            SharedPlayerDataCommands
                                                                    ::executeGroupRemoveConfirm))
                                    .then(
                                            Commands.argument(
                                                            NAME_ARGUMENT,
                                                            StringArgumentType.word())
                                                    .suggests(
                                                            SharedPlayerDataCommands
                                                                    ::suggestGroupMemberNames)
                                                    .then(
                                                            Commands.literal("confirm")
                                                                    .executes(
                                                                            SharedPlayerDataCommands
                                                                                    ::executeGroupRemovePlayerConfirm))));

                    groupRoot.then(groupNumber);
                    root.then(groupRoot);

                    root.then(
                            Commands.literal("find")
                                    .then(
                                            Commands.argument(
                                                            NAME_ARGUMENT,
                                                            StringArgumentType.word())
                                                    .suggests(
                                                            SharedPlayerDataCommands
                                                                    ::suggestKnownAndOnlinePlayerNames)
                                                    .executes(
                                                            SharedPlayerDataCommands
                                                                    ::executeFind)));

                    root.then(
                            Commands.literal("list")
                                    .executes(SharedPlayerDataCommands::executeList));

                    dispatcher.register(root);
                });
    }

    private static int executeGroupCreate(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        SharedProfileManager.CreateGroupResult result;

        try {
            result = SharedPlayerDataMod.MANAGER.createGroup();
        } catch (IOException | RuntimeException exception) {
            SharedPlayerDataMod.LOGGER.error("Failed to execute /playerbind group add.", exception);
            throw failure("/playerbind group add failed. Check the server log.");
        }

        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> Component.literal("Created playerbind group " + result.groupNumber() + "."),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeGroupAddPlayer(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        int groupNumber = IntegerArgumentType.getInteger(context, GROUP_ARGUMENT);
        String playerName = StringArgumentType.getString(context, NAME_ARGUMENT);
        ServerPlayer player = findOnlinePlayerByName(server, playerName);

        if (player == null) {
            throw failure("Player is not online: " + playerName);
        }

        SharedProfileManager.AddPlayerToGroupResult result;
        try {
            result =
                    SharedPlayerDataMod.MANAGER.addOnlinePlayerToGroup(
                            server, groupNumber, player, source.getPlayer());
        } catch (IOException | RuntimeException exception) {
            SharedPlayerDataMod.LOGGER.error(
                    "Failed to execute /playerbind group {} add {}.",
                    groupNumber,
                    playerName,
                    exception);
            throw failure(
                    "/playerbind group " + groupNumber + " add failed. Check the server log.");
        }

        if (result.changed()) {
            final String conflictMessage;

            if (!result.disconnectedPlayerNames().isEmpty()) {
                conflictMessage =
                        " Disconnected due to online group conflict: "
                                + String.join(", ", result.disconnectedPlayerNames())
                                + ".";
            } else {
                conflictMessage = "";
            }

            source.sendSuccess(
                    () ->
                            Component.literal(
                                    "Added "
                                            + result.playerName()
                                            + " to playerbind group "
                                            + result.groupNumber()
                                            + ". Group members: "
                                            + result.memberCount()
                                            + "."
                                            + conflictMessage),
                    true);
        } else {
            source.sendSuccess(
                    () ->
                            Component.literal(
                                    result.playerName()
                                            + " is already in playerbind group "
                                            + result.groupNumber()
                                            + "."),
                    false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int executeGroupList(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        int groupNumber = IntegerArgumentType.getInteger(context, GROUP_ARGUMENT);
        SharedProfileManager.GroupDetails details =
                SharedPlayerDataMod.MANAGER
                        .groupDetails(groupNumber)
                        .orElseThrow(
                                () -> failure("Playerbind group does not exist: " + groupNumber));

        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal(formatGroupDetails(details)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeGroupRemoveConfirm(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        int groupNumber = IntegerArgumentType.getInteger(context, GROUP_ARGUMENT);
        SharedProfileManager.RemoveGroupResult result;

        try {
            result = SharedPlayerDataMod.MANAGER.removeGroup(server, groupNumber);
        } catch (IOException | RuntimeException exception) {
            SharedPlayerDataMod.LOGGER.error(
                    "Failed to execute /playerbind group {} remove confirm.",
                    groupNumber,
                    exception);
            throw failure(
                    "/playerbind group "
                            + groupNumber
                            + " remove confirm failed. Check the server log.");
        }

        source.sendSuccess(
                () ->
                        Component.literal(
                                "Removed playerbind group "
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

    private static int executeGroupRemovePlayerConfirm(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        int groupNumber = IntegerArgumentType.getInteger(context, GROUP_ARGUMENT);
        String playerName = StringArgumentType.getString(context, NAME_ARGUMENT);
        SharedProfileManager.RemovePlayerFromGroupResult result;

        try {
            result =
                    SharedPlayerDataMod.MANAGER.removePlayerFromGroup(
                            server, groupNumber, playerName);
        } catch (IOException | RuntimeException exception) {
            SharedPlayerDataMod.LOGGER.error(
                    "Failed to execute /playerbind group {} remove {} confirm.",
                    groupNumber,
                    playerName,
                    exception);
            throw failure(
                    "/playerbind group "
                            + groupNumber
                            + " remove "
                            + playerName
                            + " confirm failed. Check the server log.");
        }

        source.sendSuccess(
                () ->
                        Component.literal(
                                "Removed "
                                        + result.playerName()
                                        + " from playerbind group "
                                        + result.groupNumber()
                                        + ". Remaining members: "
                                        + result.remainingMemberCount()
                                        + ". Reset offline members: "
                                        + result.immediateResetCount()
                                        + ". Online members disconnected for reset: "
                                        + result.pendingResetCount()
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
                        .orElseThrow(() -> failure("Unknown player name: " + playerName));

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

    private static CompletableFuture<Suggestions> suggestOnlinePlayerNames(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CommandSourceStack source = context.getSource();
        Collection<String> playerNames = source.getOnlinePlayerNames();

        for (String playerName : playerNames) {
            suggestIfMatches(builder, playerName);
        }

        return builder.buildFuture();
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

    private static CompletableFuture<Suggestions> suggestGroupNumbers(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        int groupCount = SharedPlayerDataMod.MANAGER.groupCount();

        for (int groupNumber = 1; groupNumber <= groupCount; groupNumber++) {
            suggestIfMatches(builder, Integer.toString(groupNumber));
        }

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestGroupMemberNames(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        int groupNumber;

        try {
            groupNumber = IntegerArgumentType.getInteger(context, GROUP_ARGUMENT);
        } catch (IllegalArgumentException exception) {
            return builder.buildFuture();
        }

        SharedPlayerDataMod.MANAGER
                .groupDetails(groupNumber)
                .ifPresent(
                        details -> {
                            for (SharedProfileManager.MemberDetails member : details.members()) {
                                if (!isUuidText(member.name())) {
                                    suggestIfMatches(builder, member.name());
                                }
                            }
                        });

        return builder.buildFuture();
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

    private static ServerPlayer findOnlinePlayerByName(MinecraftServer server, String name) {
        ServerPlayer exactNamePlayer = server.getPlayerList().getPlayer(name);

        if (exactNamePlayer != null) {
            return exactNamePlayer;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.nameAndId().name().equalsIgnoreCase(name)) {
                return player;
            }
        }

        return null;
    }

    private static CommandSyntaxException failure(String message) {
        return new SimpleCommandExceptionType(new LiteralMessage(message)).create();
    }
}

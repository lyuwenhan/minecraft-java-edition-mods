package com.example.servermanager;

import com.example.servermanager.web.AccountStore;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.io.IOException;
import java.util.List;

public final class ServerManagerCommands {
	private static volatile AccountStore accountStore;

	private ServerManagerCommands() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> {
					var root =
							Commands.literal("servermanager")
									.requires(
											source ->
													source.permissions()
															.hasPermission(
																	Permissions.COMMANDS_ADMIN));

					root.then(createCommand());
					root.then(
							Commands.literal("delete")
									.then(
											Commands.argument("name", StringArgumentType.word())
													.then(
															Commands.literal("confirm")
																	.executes(
																			context ->
																					run(
																							context
																									.getSource(),
																							store ->
																									store
																											.delete(
																													StringArgumentType
																															.getString(
																																	context,
																																	"name")))))));
					root.then(
							Commands.literal("purge")
									.then(
											Commands.literal("confirm")
													.executes(
															context ->
																	run(
																			context.getSource(),
																			AccountStore::purge))));
					root.then(
							Commands.literal("list")
									.executes(context -> list(context.getSource())));
					root.then(
							Commands.literal("reset-password")
									.then(
											Commands.argument("name", StringArgumentType.word())
													.then(
															Commands.argument(
																			"password",
																			StringArgumentType
																					.word())
																	.executes(
																			context ->
																					run(
																							context
																									.getSource(),
																							store ->
																									store
																											.resetPassword(
																													StringArgumentType
																															.getString(
																																	context,
																																	"name"),
																													StringArgumentType
																															.getString(
																																	context,
																																	"password")))))));

					var update =
							Commands.literal("update")
									.then(
											Commands.argument("name", StringArgumentType.word())
													.then(
															Commands.literal("uses")
																	.then(
																			Commands.argument(
																							"count",
																							IntegerArgumentType
																									.integer(
																											1))
																					.executes(
																							context ->
																									run(
																											context
																													.getSource(),
																											store ->
																													store
																															.updateUses(
																																	StringArgumentType
																																			.getString(
																																					context,
																																					"name"),
																																	(long)
																																			IntegerArgumentType
																																					.getInteger(
																																							context,
																																							"count")))))
																	.then(
																			Commands.literal(
																							"unlimited")
																					.executes(
																							context ->
																									run(
																											context
																													.getSource(),
																											store ->
																													store
																															.updateUses(
																																	StringArgumentType
																																			.getString(
																																					context,
																																					"name"),
																																	null)))))
													.then(
															Commands.literal("expires")
																	.then(
																			Commands.argument(
																							"time",
																							LongArgumentType
																									.longArg(
																											1))
																					.executes(
																							context ->
																									run(
																											context
																													.getSource(),
																											store ->
																													store
																															.updateExpiry(
																																	StringArgumentType
																																			.getString(
																																					context,
																																					"name"),
																																	LongArgumentType
																																			.getLong(
																																					context,
																																					"time")))))
																	.then(
																			Commands.literal(
																							"unlimited")
																					.executes(
																							context ->
																									run(
																											context
																													.getSource(),
																											store ->
																													store
																															.updateExpiry(
																																	StringArgumentType
																																			.getString(
																																					context,
																																					"name"),
																																	null))))));
					root.then(update);
					dispatcher.register(root);
				});
	}

	public static void setAccountStore(AccountStore store) {
		accountStore = store;
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
			createCommand() {
		var base =
				Commands.literal("create")
						.then(
								Commands.argument("name", StringArgumentType.word())
										.then(
												Commands.argument(
																"password",
																StringArgumentType.word())
														.then(
																Commands.argument(
																				"confirm",
																				StringArgumentType
																						.word())
																		.executes(
																				context ->
																						create(
																								context
																										.getSource(),
																								context,
																								null,
																								null))
																		.then(
																				Commands.literal(
																								"--uses")
																						.then(
																								Commands
																										.argument(
																												"uses",
																												LongArgumentType
																														.longArg(
																																1))
																										.executes(
																												context ->
																														create(
																																context
																																		.getSource(),
																																context,
																																LongArgumentType
																																		.getLong(
																																				context,
																																				"uses"),
																																null))
																										.then(
																												Commands
																														.literal(
																																"--expires")
																														.then(
																																Commands
																																		.argument(
																																				"expires",
																																				LongArgumentType
																																						.longArg(
																																								1))
																																		.executes(
																																				context ->
																																						create(
																																								context
																																										.getSource(),
																																								context,
																																								LongArgumentType
																																										.getLong(
																																												context,
																																												"uses"),
																																								LongArgumentType
																																										.getLong(
																																												context,
																																												"expires")))))))
																		.then(
																				Commands.literal(
																								"--expires")
																						.then(
																								Commands
																										.argument(
																												"expires",
																												LongArgumentType
																														.longArg(
																																1))
																										.executes(
																												context ->
																														create(
																																context
																																		.getSource(),
																																context,
																																null,
																																LongArgumentType
																																		.getLong(
																																				context,
																																				"expires")))
																										.then(
																												Commands
																														.literal(
																																"--uses")
																														.then(
																																Commands
																																		.argument(
																																				"uses",
																																				LongArgumentType
																																						.longArg(
																																								1))
																																		.executes(
																																				context ->
																																						create(
																																								context
																																										.getSource(),
																																								context,
																																								LongArgumentType
																																										.getLong(
																																												context,
																																												"uses"),
																																								LongArgumentType
																																										.getLong(
																																												context,
																																												"expires"))))))))));
		return base;
	}

	private static int create(
			CommandSourceStack source,
			com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
			Long uses,
			Long expires) {
		return run(
				source,
				store ->
						store.create(
								StringArgumentType.getString(context, "name"),
								StringArgumentType.getString(context, "password"),
								StringArgumentType.getString(context, "confirm"),
								uses,
								expires));
	}

	private static int list(CommandSourceStack source) {
		AccountStore store = accountStore;
		if (store == null) return fail(source, "Server Manager account store is not available");
		try {
			List<String> accounts = store.list();
			if (accounts.isEmpty())
				source.sendSuccess(() -> Component.literal("No Server Manager accounts"), false);
			else {
				source.sendSuccess(
						() ->
								Component.literal(
										"Server Manager accounts (" + accounts.size() + "):"),
						false);
				for (String line : accounts)
					source.sendSuccess(() -> Component.literal(line), false);
			}
			return Command.SINGLE_SUCCESS;
		} catch (IOException | RuntimeException exception) {
			ServerManagerMod.LOGGER.error("Failed to list Server Manager accounts", exception);
			return fail(source, "Failed to read account database: " + exception.getMessage());
		}
	}

	private static int run(CommandSourceStack source, StoreOperation operation) {
		AccountStore store = accountStore;
		if (store == null) return fail(source, "Server Manager account store is not available");
		try {
			store.purgeInvalid();
			AccountStore.OperationResult result = operation.run(store);
			if (!result.succeed()) return fail(source, result.message());
			source.sendSuccess(() -> Component.literal(result.message()), true);
			return Command.SINGLE_SUCCESS;
		} catch (IOException | IllegalArgumentException exception) {
			ServerManagerMod.LOGGER.error("Server Manager account command failed", exception);
			return fail(
					source,
					exception.getMessage() == null
							? "Account operation failed"
							: exception.getMessage());
		}
	}

	private static int fail(CommandSourceStack source, String message) {
		source.sendFailure(Component.literal(message));
		return 0;
	}

	@FunctionalInterface
	private interface StoreOperation {
		AccountStore.OperationResult run(AccountStore store) throws IOException;
	}
}

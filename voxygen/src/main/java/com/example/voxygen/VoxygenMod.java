package com.example.voxygen;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;

public final class VoxygenMod implements ClientModInitializer {
	private static VoxygenConfig config;

	@Override
	public void onInitializeClient() {
		config = VoxygenConfig.load();
		VoxygenGenerationService.initialize();
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> dispatcher.register(buildCommand()));
		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					String worldKey = getCurrentWorldKey();
					VoxygenGenerationService.tick(
							client, config, worldKey, getEffectiveSeed(client, worldKey));
					VoxygenProgressBossBar.tick(client, config.showProgress);
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> buildCommand() {
		LiteralArgumentBuilder<FabricClientCommandSource> root =
				ClientCommands.literal("voxygen")
						.executes(context -> sendStatus(context.getSource()));
		root.then(
				ClientCommands.literal("on")
						.executes(context -> setEnabled(context.getSource(), true)));
		root.then(
				ClientCommands.literal("off")
						.executes(context -> setEnabled(context.getSource(), false)));
		root.then(
				ClientCommands.literal("status")
						.executes(context -> sendStatus(context.getSource())));
		root.then(
				ClientCommands.literal("toggle")
						.executes(context -> setEnabled(context.getSource(), !config.enable)));
		root.then(
				ClientCommands.literal("radius")
						.executes(context -> sendRadius(context.getSource()))
						.then(
								ClientCommands.argument("radius", IntegerArgumentType.integer(1))
										.executes(
												context ->
														setRadius(
																context.getSource(),
																IntegerArgumentType.getInteger(
																		context, "radius")))));
		root.then(
				ClientCommands.literal("threads")
						.executes(context -> sendThreads(context.getSource()))
						.then(
								ClientCommands.argument(
												"threads", IntegerArgumentType.integer(1, 256))
										.executes(
												context ->
														setThreads(
																context.getSource(),
																IntegerArgumentType.getInteger(
																		context, "threads")))));
		root.then(
				ClientCommands.literal("progress")
						.executes(context -> sendProgress(context.getSource()))
						.then(
								ClientCommands.literal("on")
										.executes(
												context -> setProgress(context.getSource(), true)))
						.then(
								ClientCommands.literal("off")
										.executes(
												context -> setProgress(context.getSource(), false)))
						.then(
								ClientCommands.literal("toggle")
										.executes(
												context ->
														setProgress(
																context.getSource(),
																!config.showProgress))));
		root.then(
				ClientCommands.literal("seed")
						.executes(context -> sendSeed(context.getSource()))
						.then(
								ClientCommands.argument("seed", LongArgumentType.longArg())
										.requires(ignored -> getLocalServerSeed() == null)
										.executes(
												context ->
														setSeed(
																context.getSource(),
																LongArgumentType.getLong(
																		context, "seed")))));
		return root;
	}

	public static boolean isEnabled() {
		return config != null && config.enable;
	}

	private static int setEnabled(FabricClientCommandSource source, boolean enabled) {
		config.enable = enabled;
		config.save();
		if (!enabled) {
			VoxygenGenerationService.invalidate();
		}
		source.sendFeedback(
				Component.translatable(
						enabled ? "command.voxygen.enabled" : "command.voxygen.disabled"));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendStatus(FabricClientCommandSource source) {
		String worldKey = requireCurrentWorld(source);
		if (worldKey == null) {
			return 0;
		}
		source.sendFeedback(
				Component.translatable(
						"command.voxygen.status",
						config.enable ? "ON" : "OFF",
						getCurrentWorldLabel(),
						seedText(getEffectiveSeed(Minecraft.getInstance(), worldKey)),
						config.radius,
						VoxygenGenerationService.describeState()));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendRadius(FabricClientCommandSource source) {
		source.sendFeedback(Component.translatable("command.voxygen.radius", config.radius));
		return Command.SINGLE_SUCCESS;
	}

	private static int setRadius(FabricClientCommandSource source, int radius) {
		config.radius = radius;
		config.save();
		source.sendFeedback(Component.translatable("command.voxygen.radius_set", radius));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendThreads(FabricClientCommandSource source) {
		source.sendFeedback(Component.translatable("command.voxygen.threads", config.threads));
		return Command.SINGLE_SUCCESS;
	}

	private static int setThreads(FabricClientCommandSource source, int threads) {
		config.threads = threads;
		config.save();
		source.sendFeedback(Component.translatable("command.voxygen.threads_set", threads));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendProgress(FabricClientCommandSource source) {
		source.sendFeedback(
				Component.translatable(
						"command.voxygen.progress",
						Component.translatable(
								config.showProgress
										? "command.voxygen.option.on"
										: "command.voxygen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int setProgress(FabricClientCommandSource source, boolean enabled) {
		config.showProgress = enabled;
		config.save();
		if (!enabled) {
			VoxygenProgressBossBar.hide(Minecraft.getInstance());
		}
		source.sendFeedback(
				Component.translatable(
						"command.voxygen.progress_set",
						Component.translatable(
								enabled
										? "command.voxygen.option.on"
										: "command.voxygen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendSeed(FabricClientCommandSource source) {
		String worldKey = requireCurrentWorld(source);
		if (worldKey == null) {
			return 0;
		}
		source.sendFeedback(
				Component.translatable(
						"command.voxygen.seed",
						getCurrentWorldLabel(),
						seedText(getEffectiveSeed(Minecraft.getInstance(), worldKey))));
		return Command.SINGLE_SUCCESS;
	}

	private static int setSeed(FabricClientCommandSource source, long seed) {
		if (getLocalServerSeed() != null) {
			source.sendError(Component.translatable("command.voxygen.seed_local_read_only"));
			return 0;
		}
		String worldKey = requireCurrentWorld(source);
		if (worldKey == null) {
			return 0;
		}
		config.seeds.put(worldKey, seed);
		config.save();
		VoxygenGenerationService.invalidate();
		source.sendFeedback(
				Component.translatable("command.voxygen.seed_set", getCurrentWorldLabel(), seed));
		return Command.SINGLE_SUCCESS;
	}

	private static String seedText(Long seed) {
		return seed == null
				? Component.translatable("command.voxygen.seed_unset").getString()
				: Long.toString(seed);
	}

	private static String requireCurrentWorld(FabricClientCommandSource source) {
		String worldKey = getCurrentWorldKey();
		if (worldKey == null) {
			source.sendError(Component.translatable("command.voxygen.no_server"));
		}
		return worldKey;
	}

	private static String getCurrentWorldKey() {
		Minecraft client = Minecraft.getInstance();
		if (client.getSingleplayerServer() != null) {
			return "singleplayer:"
					+ client.getSingleplayerServer()
							.getWorldPath(LevelResource.ROOT)
							.toAbsolutePath()
							.normalize();
		}
		ServerData server = client.getCurrentServer();
		if (server == null || server.ip == null || server.ip.isBlank()) {
			return null;
		}
		return server.ip;
	}

	private static String getCurrentWorldLabel() {
		Minecraft client = Minecraft.getInstance();
		if (client.getSingleplayerServer() != null) {
			return client.getSingleplayerServer().getWorldData().getLevelName();
		}
		ServerData server = client.getCurrentServer();
		return server == null ? "unknown" : server.ip;
	}

	private static Long getEffectiveSeed(Minecraft client, String worldKey) {
		Long localSeed = getLocalServerSeed(client);
		if (localSeed != null) {
			return localSeed;
		}
		return worldKey == null ? null : config.seeds.get(worldKey);
	}

	private static Long getLocalServerSeed() {
		return getLocalServerSeed(Minecraft.getInstance());
	}

	private static Long getLocalServerSeed(Minecraft client) {
		if (client.getSingleplayerServer() == null) {
			return null;
		}
		try {
			return client.getSingleplayerServer().getWorldGenSettings().options().seed();
		} catch (RuntimeException ignored) {
			return null;
		}
	}
}

package com.example.seedgen;

import com.example.seedgen.core.SeedgenGenerationService;
import com.example.seedgen.journeymap.JourneyMapCompat;
import com.example.seedgen.voxy.GeneratedChunkReceiver;
import com.example.seedgen.voxy.VoxyCompat;
import com.example.seedgen.xaero.XaeroWorldMapCompat;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

public final class SeedgenMod implements ClientModInitializer {
	private static SeedgenConfig config;

	@Override
	public void onInitializeClient() {
		config = SeedgenConfig.load();
		SeedgenGenerationService.initialize();
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> dispatcher.register(buildCommand()));
		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					ClientWorldSeeds.capture(client.level);
					String worldKey = getCurrentWorldKey();
					GeneratedChunkReceiver receiver =
							client.level == null ? null : VoxyCompat.find(client.level);
					receiver = XaeroWorldMapCompat.addReceiver(receiver, client.level);
					receiver = JourneyMapCompat.addReceiver(receiver, client.level);
					SeedgenGenerationService.tick(
							client, config, worldKey, getEffectiveSeed(client, worldKey), receiver);
					SeedgenProgressBossBar.tick(client, config.showProgress);
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> buildCommand() {
		LiteralArgumentBuilder<FabricClientCommandSource> root =
				ClientCommands.literal("seedgen")
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
				ClientCommands.literal("generateforfreecam")
						.executes(context -> sendFreecam(context.getSource()))
						.then(
								ClientCommands.literal("on")
										.executes(context -> setFreecam(context.getSource(), true)))
						.then(
								ClientCommands.literal("off")
										.executes(
												context -> setFreecam(context.getSource(), false)))
						.then(
								ClientCommands.literal("toggle")
										.executes(
												context ->
														setFreecam(
																context.getSource(),
																!config.generateForFreecam))));
		root.then(
				ClientCommands.literal("generateforworldmapcenter")
						.executes(context -> sendWorldMapCenter(context.getSource()))
						.then(
								ClientCommands.literal("on")
										.executes(
												context ->
														setWorldMapCenter(
																context.getSource(), true)))
						.then(
								ClientCommands.literal("off")
										.executes(
												context ->
														setWorldMapCenter(
																context.getSource(), false)))
						.then(
								ClientCommands.literal("toggle")
										.executes(
												context ->
														setWorldMapCenter(
																context.getSource(),
																!config.generateForWorldMapCenter))));
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
			SeedgenGenerationService.invalidate();
		}
		source.sendFeedback(
				Component.translatable(
						enabled ? "command.seedgen.enabled" : "command.seedgen.disabled"));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendStatus(FabricClientCommandSource source) {
		String worldKey = requireCurrentWorld(source);
		if (worldKey == null) {
			return 0;
		}
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.status",
						Component.translatable(
								config.enable
										? "command.seedgen.option.on"
										: "command.seedgen.option.off"),
						getCurrentWorldLabel(),
						seedText(getEffectiveSeed(Minecraft.getInstance(), worldKey)),
						config.radius,
						Component.translatable(
								config.generateForFreecam
										? "command.seedgen.option.on"
										: "command.seedgen.option.off"),
						Component.translatable(
								config.generateForWorldMapCenter
										? "command.seedgen.option.on"
										: "command.seedgen.option.off"),
						SeedgenGenerationService.describeState()));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendRadius(FabricClientCommandSource source) {
		source.sendFeedback(Component.translatable("command.seedgen.radius", config.radius));
		return Command.SINGLE_SUCCESS;
	}

	private static int setRadius(FabricClientCommandSource source, int radius) {
		config.radius = radius;
		config.save();
		source.sendFeedback(Component.translatable("command.seedgen.radius_set", radius));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendThreads(FabricClientCommandSource source) {
		source.sendFeedback(Component.translatable("command.seedgen.threads", config.threads));
		return Command.SINGLE_SUCCESS;
	}

	private static int setThreads(FabricClientCommandSource source, int threads) {
		config.threads = threads;
		config.save();
		source.sendFeedback(Component.translatable("command.seedgen.threads_set", threads));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendProgress(FabricClientCommandSource source) {
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.progress",
						Component.translatable(
								config.showProgress
										? "command.seedgen.option.on"
										: "command.seedgen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int setProgress(FabricClientCommandSource source, boolean enabled) {
		config.showProgress = enabled;
		config.save();
		if (!enabled) {
			SeedgenProgressBossBar.hide(Minecraft.getInstance());
		}
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.progress_set",
						Component.translatable(
								enabled
										? "command.seedgen.option.on"
										: "command.seedgen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendFreecam(FabricClientCommandSource source) {
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.freecam",
						Component.translatable(
								config.generateForFreecam
										? "command.seedgen.option.on"
										: "command.seedgen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int setFreecam(FabricClientCommandSource source, boolean enabled) {
		config.generateForFreecam = enabled;
		config.save();
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.freecam_set",
						Component.translatable(
								enabled
										? "command.seedgen.option.on"
										: "command.seedgen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendWorldMapCenter(FabricClientCommandSource source) {
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.world_map_center",
						Component.translatable(
								config.generateForWorldMapCenter
										? "command.seedgen.option.on"
										: "command.seedgen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int setWorldMapCenter(FabricClientCommandSource source, boolean enabled) {
		config.generateForWorldMapCenter = enabled;
		config.save();
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.world_map_center_set",
						Component.translatable(
								enabled
										? "command.seedgen.option.on"
										: "command.seedgen.option.off")));
		return Command.SINGLE_SUCCESS;
	}

	private static int sendSeed(FabricClientCommandSource source) {
		String worldKey = requireCurrentWorld(source);
		if (worldKey == null) {
			return 0;
		}
		source.sendFeedback(
				Component.translatable(
						"command.seedgen.seed",
						getCurrentWorldLabel(),
						seedText(getEffectiveSeed(Minecraft.getInstance(), worldKey))));
		return Command.SINGLE_SUCCESS;
	}

	private static int setSeed(FabricClientCommandSource source, long seed) {
		if (getLocalServerSeed() != null) {
			source.sendError(Component.translatable("command.seedgen.seed_local_read_only"));
			return 0;
		}
		String worldKey = requireCurrentWorld(source);
		if (worldKey == null) {
			return 0;
		}
		config.seeds.put(worldKey, seed);
		config.save();
		SeedgenGenerationService.invalidate();
		source.sendFeedback(
				Component.translatable("command.seedgen.seed_set", getCurrentWorldLabel(), seed));
		return Command.SINGLE_SUCCESS;
	}

	private static String seedText(Long seed) {
		return seed == null
				? Component.translatable("command.seedgen.seed_unset").getString()
				: Long.toString(seed);
	}

	private static String requireCurrentWorld(FabricClientCommandSource source) {
		String worldKey = getCurrentWorldKey();
		if (worldKey == null) {
			source.sendError(Component.translatable("command.seedgen.no_server"));
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
		String serverKey = getServerSeedKey();
		if (serverKey == null) {
			return null;
		}
		return appendWorldHash(client, serverKey + "#" + getCurrentWorldName(client));
	}

	private static String appendWorldHash(Minecraft client, String key) {
		String worldHash = getWorldHash(client);
		return worldHash == null ? key : key + "#" + worldHash;
	}

	private static String getWorldHash(Minecraft client) {
		Long hashedSeed = ClientWorldSeeds.hashedSeedOf(client.level);
		return hashedSeed == null ? null : String.format("%016x", hashedSeed);
	}

	private static String getServerSeedKey() {
		Minecraft client = Minecraft.getInstance();
		ServerData server = client.getCurrentServer();
		if (server == null || server.ip == null || server.ip.isBlank()) {
			return null;
		}
		return server.ip;
	}

	private static String getCurrentWorldName(Minecraft client) {
		return client.level == null
				? "unknown"
				: client.level.dimension().identifier().toString();
	}

	private static String getCurrentWorldLabel() {
		Minecraft client = Minecraft.getInstance();
		if (client.getSingleplayerServer() != null) {
			return client.getSingleplayerServer().getWorldData().getLevelName();
		}
		String serverKey = getServerSeedKey();
		if (serverKey == null) {
			return "unknown";
		}
		String label = serverKey + " (" + getCurrentWorldName(client);
		String worldHash = getWorldHash(client);
		return worldHash == null
				? label + ")"
				: label + ", " + worldHash.substring(0, 8) + ")";
	}

	private static Long getEffectiveSeed(Minecraft client, String worldKey) {
		Long localSeed = getLocalServerSeed(client);
		if (localSeed != null) {
			return localSeed;
		}
		if (worldKey == null) {
			return null;
		}
		Long seed = config.seeds.get(worldKey);
		if (seed != null) {
			return seed;
		}
		String serverKey = getServerSeedKey();
		if (serverKey == null) {
			return null;
		}
		if (isVanillaWorld(client.level)) {
			if (!Level.OVERWORLD.equals(client.level.dimension())) {
				// The vanilla Nether and End share the seed of the Overworld
				// of their world.
				seed =
						config.seeds.get(
								appendWorldHash(
										client, serverKey + "#" + Level.OVERWORLD.identifier()));
				if (seed != null) {
					return seed;
				}
			}
			// Fall back to entries written before world-seed hashes were
			// tracked, then to the server-wide seed.
			seed = config.seeds.get(serverKey + "#" + getCurrentWorldName(client));
			return seed != null ? seed : config.seeds.get(serverKey);
		}
		// Custom server worlds must have their own explicit seed; a
		// server-wide seed does not apply to them.
		seed = config.seeds.get(serverKey + "#" + getCurrentWorldName(client));
		return seed;
	}

	private static boolean isVanillaWorld(ClientLevel level) {
		ResourceKey<Level> dimension = level == null ? null : level.dimension();
		return Level.OVERWORLD.equals(dimension)
				|| Level.NETHER.equals(dimension)
				|| Level.END.equals(dimension);
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

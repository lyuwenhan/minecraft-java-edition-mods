package com.example.servermanager.web;

import com.google.gson.*;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class BackendService {
	private static volatile BackendService activeInstance;
	private final MinecraftServer server;
	private final Instant startedAt = Instant.now();
	private static final long PLAYER_FULL_SYNC_INTERVAL_MILLIS = 60L * 60L * 1000L;

	private final AtomicReference<String> lastStats = new AtomicReference<>();
	private Map<String, JsonObject> lastPlayers;
	private long lastPlayerFullSyncBucket;
	private String lastAccessControl;
	private int statsTickCounter;
	private int accessControlTickCounter;
	private int playerTickCounter;
	private volatile Boolean whitelistEnabledOverride;

	public BackendService(MinecraftServer server) {
		this.server = server;
		activeInstance = this;
		this.lastPlayers = playerSnapshot();
		this.lastPlayerFullSyncBucket = currentPlayerFullSyncBucket();
		this.lastAccessControl = accessControlPacket();
		CarpetIntegration.registerObserver();
	}

	public void tick() {
		if (++statsTickCounter >= 20) {
			statsTickCounter = 0;
			String json = statsPacket();
			String previous = lastStats.getAndSet(json);
			String delta = statsDeltaPacket(previous, json);
			if (delta != null) broadcast(delta);
		}

		if (++accessControlTickCounter >= 20) {
			accessControlTickCounter = 0;
			String accessControl = accessControlPacket();
			if (!accessControl.equals(lastAccessControl)) {
				lastAccessControl = accessControl;
				broadcast(accessControl);
			}
		}

		if (++playerTickCounter >= 4) {
			playerTickCounter = 0;
			Map<String, JsonObject> currentPlayers = playerSnapshot();
			long currentBucket = currentPlayerFullSyncBucket();
			if (currentBucket != lastPlayerFullSyncBucket) {
				lastPlayerFullSyncBucket = currentBucket;
				lastPlayers = currentPlayers;
				broadcast(playersPacket(currentPlayers));
				return;
			}

			broadcastPlayerChanges(lastPlayers, currentPlayers);
			lastPlayers = currentPlayers;
		}
	}

	public void sendInitial(Channel channel) {
		send(channel, statsPacket());
		send(channel, playersPacket(playerSnapshot()));
		send(channel, accessControlPacket());
		send(channel, CarpetIntegration.packet(server));
		send(channel, commandTreePacket());
		send(channel, LogBuffer.fullLogPacket());
	}

	public void handleWebSocket(Channel channel, JsonObject packet) {
		String type = string(packet, "type");
		if (type == null) return;
		switch (type) {
			case "request-stats" -> send(channel, statsPacket());
			case "request-players" -> send(channel, playersPacket(playerSnapshot()));
			case "request-access-control" -> send(channel, accessControlPacket());
			case "request-gamerule" -> send(channel, gameRulesPacket());
			case "request-carpet-rule" -> send(channel, CarpetIntegration.packet(server));
			case "request-command-tree" -> send(channel, commandTreePacket());
			case "request-full-log" -> send(channel, LogBuffer.fullLogPacket());
			case "update-gamerule" -> updateGameRule(packet);
			case "update-carpet-rule" -> updateCarpetRule(packet);
			case "feed-player" -> feedPlayer(packet);
			case "world-entity-action" -> handleWorldEntityAction(packet);
			default -> {}
		}
	}

	private String commandTreePacket() {
		return CommandTreeExporter.packet(server);
	}

	public List<String> runCommands(List<String> commands) {
		CompletableFuture<List<String>> future = new CompletableFuture<>();
		server.execute(
				() -> {
					List<String> results = new ArrayList<>(commands.size());
					for (String command : commands) {
						try {
							results.add(executeCommandWithMessages(command));
							updateAccessControlStateAfterCommand(command);
						} catch (Exception e) {
							results.add("Error: " + errorMessage(e));
						}
					}
					future.complete(results);
				});
		return future.join();
	}

	private void handleWorldEntityAction(JsonObject packet) {
		String action = string(packet, "action");
		if (action == null) return;

		server.execute(
				() -> {
					try {
						switch (action) {
							case "kill-all-players" ->
									executeCommand("kill @e[type=minecraft:player]");
							case "clear-all-entities" -> {
								executeCommand("kill @e");
								executeCommand("kill @e");
							}
							case "clear-falling-blocks" ->
									executeCommand("kill @e[type=minecraft:falling_block]");
							case "clear-enemies" -> killAllEnemies();
							case "clear-projectiles" ->
									executeCommand("kill @e[type=#minecraft:impact_projectiles]");
							case "clear-item-entities" ->
									executeCommand("kill @e[type=minecraft:item]");
							default -> {
								return;
							}
						}
					} catch (Exception ignored) {
					}
				});
	}

	private void killAllEnemies() {
		for (ServerLevel level : server.getAllLevels()) {
			List<Entity> enemies = new ArrayList<>();
			for (Entity entity : level.getAllEntities()) {
				if (entity instanceof Enemy) enemies.add(entity);
			}
			for (Entity enemy : enemies) enemy.kill(level);
		}
	}

	private void feedPlayer(JsonObject packet) {
		String name = string(packet, "name");
		JsonElement healElement = packet.get("heal");
		if (name == null
				|| healElement == null
				|| !healElement.isJsonPrimitive()
				|| !healElement.getAsJsonPrimitive().isBoolean()) {
			return;
		}

		boolean heal = healElement.getAsBoolean();
		server.execute(
				() -> {
					ServerPlayer player = server.getPlayerList().getPlayerByName(name);
					if (player == null || !player.isAlive() || player.getHealth() <= 0.0F) return;

					player.getFoodData().setFoodLevel(20);
					player.getFoodData().setSaturation(20.0F);
					player.setAirSupply(player.getMaxAirSupply());
					player.removeEffect(MobEffects.HUNGER);

					addHiddenEffect(player, MobEffects.SATURATION);
					addHiddenEffect(player, MobEffects.WATER_BREATHING);

					if (!heal) return;

					player.setHealth(player.getMaxHealth());
					for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
						if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
							player.removeEffect(effect.getEffect());
						}
					}

					player.level().broadcastEntityEvent(player, (byte) 35);
					addHiddenEffect(player, MobEffects.INSTANT_HEALTH);
					addHiddenEffect(player, MobEffects.REGENERATION);
					addHiddenEffect(player, MobEffects.RESISTANCE);
					addHiddenEffect(player, MobEffects.FIRE_RESISTANCE);
				});
	}

	private static void addHiddenEffect(
			ServerPlayer player,
			net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
		player.addEffect(new MobEffectInstance(effect, 10 * 20, 255, false, false, false));
	}

	private void updateCarpetRule(JsonObject packet) {
		String id = string(packet, "id");
		JsonElement value = packet.get("value");
		if (id == null || value == null || value.isJsonNull()) return;
		String text =
				value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
						? value.getAsString()
						: value.toString();
		server.execute(
				() -> {
					try {
						CarpetIntegration.setDefault(server, id, text);
					} catch (RuntimeException ignored) {
					}
				});
	}

	private void updateGameRule(JsonObject packet) {
		String id = string(packet, "id");
		JsonElement value = packet.get("value");
		if (id == null || value == null || value.isJsonNull()) return;

		String text =
				value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
						? value.getAsString()
						: value.toString();
		server.execute(
				() -> {
					try {
						executeCommand("gamerule " + id + " " + text);
					} catch (Exception ignored) {
					}
				});
	}

	private int executeCommand(String command) throws Exception {
		return server.getCommands()
				.getDispatcher()
				.execute(command, server.createCommandSourceStack());
	}

	private String executeCommandWithMessages(String command) throws Exception {
		List<String> messages = new ArrayList<>();
		CommandSource output =
				new CommandSource() {
					@Override
					public void sendSystemMessage(Component message) {
						messages.add(message.getString());
					}

					@Override
					public boolean acceptsSuccess() {
						return true;
					}

					@Override
					public boolean acceptsFailure() {
						return true;
					}

					@Override
					public boolean shouldInformAdmins() {
						return false;
					}
				};

		server.getCommands()
				.getDispatcher()
				.execute(command, server.createCommandSourceStack().withSource(output));
		return String.join("\n", messages);
	}

	public String statsPacket() {
		JsonObject data = new JsonObject();
		data.addProperty("Java Version", "Java " + Runtime.version().feature());
		data.addProperty("Game Version", gameVersion());
		data.addProperty("Fabric Loader Version", loaderVersion());
		data.addProperty("TPS", formatTps());
		data.addProperty("Average Tick Time", formatAverageTickTime());
		data.addProperty("Weather", currentWeather());
		data.addProperty("World Time", formatWorldTime());
		data.addProperty(
				"Run time", formatRuntime(Duration.between(startedAt, Instant.now()).toMinutes()));

		WorldLoadStats loadStats = collectWorldLoadStats();
		JsonObject worldData = new JsonObject();
		worldData.addProperty("Online Players", onlinePlayers());
		worldData.addProperty("Loaded Chunks", loadStats.loadedChunks());
		worldData.addProperty("Loaded Entities", loadStats.loadedEntities());
		worldData.addProperty("Loaded Block Entities", loadStats.loadedBlockEntities());
		worldData.addProperty("Loaded Enemies", loadStats.loadedEnemies());
		worldData.addProperty("Loaded Animals", loadStats.loadedAnimals());
		worldData.addProperty("Loaded Projectiles", loadStats.loadedProjectiles());
		worldData.addProperty("Loaded Item Entities", loadStats.loadedItemEntities());

		JsonObject resourceData = collectResourceStats();

		JsonObject root = new JsonObject();
		root.addProperty("type", "stats");
		root.add("data", data);
		root.add("worldData", worldData);
		root.add("resourceData", resourceData);
		return root.toString();
	}

	private static String statsDeltaPacket(String previousJson, String currentJson) {
		if (previousJson == null) return currentJson;
		JsonObject previous = JsonParser.parseString(previousJson).getAsJsonObject();
		JsonObject current = JsonParser.parseString(currentJson).getAsJsonObject();
		JsonObject dataDelta =
				jsonObjectDelta(previous.getAsJsonObject("data"), current.getAsJsonObject("data"));
		JsonObject worldDelta =
				jsonObjectDelta(
						previous.getAsJsonObject("worldData"),
						current.getAsJsonObject("worldData"));
		JsonObject resourceDelta =
				jsonObjectDelta(
						previous.getAsJsonObject("resourceData"),
						current.getAsJsonObject("resourceData"));
		if (dataDelta.size() == 0 && worldDelta.size() == 0 && resourceDelta.size() == 0)
			return null;
		JsonObject root = new JsonObject();
		root.addProperty("type", "stats");
		if (dataDelta.size() != 0) root.add("data", dataDelta);
		if (worldDelta.size() != 0) root.add("worldData", worldDelta);
		if (resourceDelta.size() != 0) root.add("resourceData", resourceDelta);
		return root.toString();
	}

	private static JsonObject jsonObjectDelta(JsonObject previous, JsonObject current) {
		JsonObject delta = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : current.entrySet()) {
			JsonElement oldValue = previous == null ? null : previous.get(entry.getKey());
			if (!Objects.equals(oldValue, entry.getValue()))
				delta.add(entry.getKey(), entry.getValue());
		}
		return delta;
	}

	private static JsonObject collectResourceStats() {
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		MemoryUsage nonHeap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
		java.lang.management.OperatingSystemMXBean operatingSystem =
				ManagementFactory.getOperatingSystemMXBean();

		JsonObject data = new JsonObject();
		data.addProperty("Heap Memory", formatMemoryUsage(heap));
		data.addProperty("Non-Heap Memory", formatMemoryUsage(nonHeap));
		data.addProperty("Live Threads", ManagementFactory.getThreadMXBean().getThreadCount());
		data.addProperty("Available Processors", operatingSystem.getAvailableProcessors());

		long collectionCount = 0L;
		long collectionTime = 0L;
		for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
			if (collector.getCollectionCount() >= 0L)
				collectionCount += collector.getCollectionCount();
			if (collector.getCollectionTime() >= 0L)
				collectionTime += collector.getCollectionTime();
		}
		data.addProperty("GC Collections", collectionCount);
		data.addProperty("GC Time", collectionTime + " ms");

		if (operatingSystem instanceof com.sun.management.OperatingSystemMXBean extended) {
			data.addProperty("Process CPU Load", formatCpuLoad(extended.getProcessCpuLoad()));
			data.addProperty("System CPU Load", formatCpuLoad(extended.getCpuLoad()));
		}
		return data;
	}

	private static String formatMemoryUsage(MemoryUsage usage) {
		String used = formatBytes(usage.getUsed());
		long maximum = usage.getMax();
		return maximum < 0L ? used : used + "/" + formatBytes(maximum);
	}

	private static String formatBytes(long bytes) {
		if (bytes < 1024L) return bytes + " B";
		double value = bytes;
		String[] units = {"KiB", "MiB", "GiB", "TiB"};
		int unit = -1;
		do {
			value /= 1024.0;
			unit++;
		} while (value >= 1024.0 && unit < units.length - 1);
		return String.format(Locale.ROOT, "%.2f %s", value, units[unit]);
	}

	private static String formatCpuLoad(double load) {
		return load < 0.0 ? "N/A" : String.format(Locale.ROOT, "%.1f%%", load * 100.0);
	}

	private WorldLoadStats collectWorldLoadStats() {
		int loadedChunks = 0;
		int loadedEntities = 0;
		int loadedEnemies = 0;
		int loadedAnimals = 0;
		int loadedProjectiles = 0;
		int loadedItemEntities = 0;

		for (ServerLevel level : server.getAllLevels()) {
			loadedChunks += level.getChunkSource().getLoadedChunksCount();

			for (Entity entity : level.getAllEntities()) {
				loadedEntities++;
				if (entity instanceof Enemy) loadedEnemies++;
				if (entity instanceof Animal) loadedAnimals++;
				if (entity instanceof Projectile) loadedProjectiles++;
				if (entity instanceof ItemEntity) loadedItemEntities++;
			}
		}

		return new WorldLoadStats(
				loadedChunks,
				loadedEntities,
				LoadedChunkTracker.loadedBlockEntityCount(),
				loadedEnemies,
				loadedAnimals,
				loadedProjectiles,
				loadedItemEntities);
	}

	private record WorldLoadStats(
			int loadedChunks,
			int loadedEntities,
			int loadedBlockEntities,
			int loadedEnemies,
			int loadedAnimals,
			int loadedProjectiles,
			int loadedItemEntities) {}

	private String accessControlPacket() {
		Path gameDirectory = FabricLoader.getInstance().getGameDir();
		JsonObject root = new JsonObject();
		root.addProperty("type", "access-control");
		boolean whitelistEnabled =
				whitelistEnabledOverride != null
						? whitelistEnabledOverride
						: readWhitelistEnabled(gameDirectory.resolve("server.properties"));
		root.addProperty("whitelistEnabled", whitelistEnabled);
		root.add("whitelist", readIdentityList(gameDirectory.resolve("whitelist.json")));
		root.add("blacklist", readIdentityList(gameDirectory.resolve("banned-players.json")));
		root.add("ops", readIdentityList(gameDirectory.resolve("ops.json")));
		return root.toString();
	}

	private void updateAccessControlStateAfterCommand(String command) {
		String normalized = command.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
		if (normalized.equals("whitelist on")) whitelistEnabledOverride = true;
		else if (normalized.equals("whitelist off")) whitelistEnabledOverride = false;
	}

	private static boolean readWhitelistEnabled(Path propertiesPath) {
		Properties properties = new Properties();
		if (!Files.isRegularFile(propertiesPath)) return false;
		try (var reader = Files.newBufferedReader(propertiesPath, StandardCharsets.UTF_8)) {
			properties.load(reader);
			return Boolean.parseBoolean(properties.getProperty("white-list", "false"));
		} catch (IOException ignored) {
			return false;
		}
	}

	private static JsonArray readIdentityList(Path jsonPath) {
		JsonArray result = new JsonArray();
		if (!Files.isRegularFile(jsonPath)) return result;
		try (var reader = Files.newBufferedReader(jsonPath, StandardCharsets.UTF_8)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonArray()) return result;
			for (JsonElement element : parsed.getAsJsonArray()) {
				if (!element.isJsonObject()) continue;
				JsonObject source = element.getAsJsonObject();
				JsonElement uuid = source.get("uuid");
				JsonElement name = source.get("name");
				if (uuid == null
						|| name == null
						|| !uuid.isJsonPrimitive()
						|| !name.isJsonPrimitive()) continue;
				JsonObject entry = new JsonObject();
				entry.addProperty("uuid", uuid.getAsString());
				entry.addProperty("name", name.getAsString());
				result.add(entry);
			}
		} catch (IOException | JsonParseException ignored) {
		}
		return result;
	}

	private Map<String, JsonObject> playerSnapshot() {
		Map<String, JsonObject> players = new LinkedHashMap<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			JsonObject entry = new JsonObject();
			entry.addProperty("name", player.getScoreboardName());
			entry.addProperty("uuid", player.getUUID().toString());
			entry.addProperty("dimension", player.level().dimension().identifier().toString());

			JsonObject position = new JsonObject();
			position.addProperty("x", player.getX());
			position.addProperty("y", player.getY());
			position.addProperty("z", player.getZ());
			entry.add("position", position);

			entry.addProperty("health", player.getHealth());
			entry.addProperty("alive", player.isAlive() && player.getHealth() > 0.0F);
			entry.addProperty("maxHealth", player.getMaxHealth());
			entry.addProperty("hunger", player.getFoodData().getFoodLevel());
			entry.addProperty("level", player.experienceLevel);
			entry.addProperty("gamemode", player.gameMode.getGameModeForPlayer().getName());
			entry.addProperty("ping", playerPing(player));
			entry.addProperty(
					"op",
					server.getPlayerList()
							.isOp(new NameAndId(player.getUUID(), player.getScoreboardName())));
			players.put(player.getUUID().toString(), entry);
		}
		return players;
	}

	private static String playersPacket(Map<String, JsonObject> players) {
		JsonObject root = new JsonObject();
		root.addProperty("type", "players");
		JsonArray data = new JsonArray();
		players.values().forEach(data::add);
		root.add("data", data);
		return root.toString();
	}

	/**
	 * Incremental packets are batched by type. Add/update entries remain complete player records so
	 * every dashboard table can consume the same player store.
	 */
	private static String playerBatchPacket(String type, Collection<JsonObject> players) {
		JsonObject root = new JsonObject();
		root.addProperty("type", type);
		JsonArray data = new JsonArray();
		players.forEach(player -> data.add(player.deepCopy()));
		root.add("data", data);
		return root.toString();
	}

	private static String playerRemoveBatchPacket(Collection<String> uuids) {
		JsonObject root = new JsonObject();
		root.addProperty("type", "player-remove");
		JsonArray data = new JsonArray();
		uuids.forEach(data::add);
		root.add("data", data);
		return root.toString();
	}

	private static void broadcastPlayerChanges(
			Map<String, JsonObject> previous, Map<String, JsonObject> current) {
		List<JsonObject> added = new ArrayList<>();
		List<JsonObject> updated = new ArrayList<>();
		List<String> removed = new ArrayList<>();

		for (Map.Entry<String, JsonObject> entry : current.entrySet()) {
			JsonObject oldValue = previous.get(entry.getKey());
			if (oldValue == null) added.add(entry.getValue());
			else if (!oldValue.equals(entry.getValue())) updated.add(entry.getValue());
		}
		for (String uuid : previous.keySet()) {
			if (!current.containsKey(uuid)) removed.add(uuid);
		}

		if (!added.isEmpty()) broadcast(playerBatchPacket("player-add", added));
		if (!updated.isEmpty()) broadcast(playerBatchPacket("player-update", updated));
		if (!removed.isEmpty()) broadcast(playerRemoveBatchPacket(removed));
	}

	private static long currentPlayerFullSyncBucket() {
		return System.currentTimeMillis() / PLAYER_FULL_SYNC_INTERVAL_MILLIS;
	}

	private static int playerPing(ServerPlayer player) {
		return Math.max(0, player.connection.latency());
	}

	private String onlinePlayers() {
		var playerList = server.getPlayerList();
		return playerList.getPlayerCount() + "/" + playerList.getMaxPlayers();
	}

	private String gameVersion() {
		return FabricLoader.getInstance()
				.getModContainer("minecraft")
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("Unknown");
	}

	private String loaderVersion() {
		return FabricLoader.getInstance()
				.getModContainer("fabricloader")
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("Unknown");
	}

	private String formatTps() {
		double targetTickRate = targetTickRate();
		Double averageTickTime = averageTickTimeMillis();
		if (averageTickTime == null) return formatDecimal(targetTickRate);
		double tps = Math.min(targetTickRate, 1000.0 / Math.max(averageTickTime, 0.001));
		return formatDecimal(tps);
	}

	private String formatAverageTickTime() {
		Double averageTickTime = averageTickTimeMillis();
		return averageTickTime == null ? "Unknown" : formatDecimal(averageTickTime) + " ms";
	}

	private Double averageTickTimeMillis() {
		return (double) server.getCurrentSmoothedTickTime();
	}

	private String formatWorldTime() {
		long dayTime = server.overworld().getOverworldClockTime();
		long normalized = Math.floorMod(dayTime, 24_000L);
		long totalMinutes = Math.floorMod(normalized + 6_000L, 24_000L) * 1_440L / 24_000L;
		long hour = totalMinutes / 60L;
		long minute = totalMinutes % 60L;
		long day = Math.floorDiv(dayTime, 24_000L) + 1L;
		return String.format(Locale.ROOT, "Day %d, %02d:%02d", day, hour, minute);
	}

	private String currentWeather() {
		var overworld = server.overworld();
		if (overworld.isThundering()) return "Thunder";
		if (overworld.isRaining()) return "Rain";
		return "Clear";
	}

	private double targetTickRate() {
		return server.tickRateManager().tickrate();
	}

	private static String formatDecimal(double value) {
		return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.?0+$", "");
	}

	private static String formatRuntime(long elapsedMinutes) {
		List<String> out = new ArrayList<>();
		long day = elapsedMinutes / 1440;
		long hour = elapsedMinutes / 60 % 24;
		long minute = elapsedMinutes % 60;
		if (day != 0) out.add(day + " day" + (day == 1 ? "" : "s"));
		if (hour != 0) out.add(hour + " hour" + (hour == 1 ? "" : "s"));
		if (minute != 0) out.add(minute + " minute" + (minute == 1 ? "" : "s"));
		return out.isEmpty() ? "0 minutes" : String.join(", ", out);
	}

	private static String errorMessage(Exception exception) {
		Throwable cause = exception;
		while (cause.getCause() != null) cause = cause.getCause();
		return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
	}

	public void close() {
		if (activeInstance == this) activeInstance = null;
	}

	public static void onCarpetRuleChanged(String id, Object value) {
		BackendService service = activeInstance;
		if (service == null) return;
		JsonObject packet = new JsonObject();
		packet.addProperty("type", "update-carpet-rule");
		packet.addProperty("id", id);
		addJsonValue(packet, "value", value);
		broadcast(packet.toString());
	}

	public static void onGameRuleChanged(
			MinecraftServer sourceServer, GameRule<?> rule, Object value) {
		BackendService service = activeInstance;
		if (service == null || service.server != sourceServer) return;
		JsonObject packet = new JsonObject();
		packet.addProperty("type", "update-gamerule");
		packet.addProperty("id", rule.getIdentifier().toString());
		addJsonValue(packet, "value", value);
		broadcast(packet.toString());
	}

	private String gameRulesPacket() {
		JsonObject data = new JsonObject();
		GameRules gameRules = server.getGameRules();
		List<GameRule<?>> rules =
				gameRules
						.availableRules()
						.sorted(
								Comparator.comparing(
												(GameRule<?> rule) ->
														rule.category().id().toString())
										.thenComparing(rule -> rule.getIdentifier().toString()))
						.toList();

		for (GameRule<?> rule : rules) {
			String category = rule.category().label().getString();
			JsonObject categoryData =
					data.has(category) ? data.getAsJsonObject(category) : new JsonObject();
			if (!data.has(category)) data.add(category, categoryData);

			JsonObject entry = new JsonObject();
			entry.addProperty("name", Component.translatable(rule.getDescriptionId()).getString());
			addRuleValue(entry, "currentValue", gameRules, rule);
			entry.addProperty("type", rule.gameRuleType().getSerializedName());
			addJsonValue(entry, "defaultValue", rule.defaultValue());
			entry.addProperty("editable", true);
			categoryData.add(rule.getIdentifier().toString(), entry);
		}

		JsonObject root = new JsonObject();
		root.addProperty("type", "gamerule");
		root.add("data", data);
		return root.toString();
	}

	private static <T> void addRuleValue(
			JsonObject object, String key, GameRules gameRules, GameRule<T> rule) {
		addJsonValue(object, key, gameRules.get(rule));
	}

	private static void addJsonValue(JsonObject object, String key, Object value) {
		if (value instanceof Boolean booleanValue) object.addProperty(key, booleanValue);
		else if (value instanceof Number numberValue) object.addProperty(key, numberValue);
		else object.addProperty(key, String.valueOf(value));
	}

	private static String string(JsonObject object, String key) {
		return object.has(key) && object.get(key).isJsonPrimitive()
				? object.get(key).getAsString()
				: null;
	}

	public static void send(Channel channel, String json) {
		if (channel.isActive()) channel.writeAndFlush(new TextWebSocketFrame(json));
	}

	public static void broadcast(String json) {
		for (Channel channel : SessionRegistry.authenticatedChannels()) send(channel, json);
	}
}

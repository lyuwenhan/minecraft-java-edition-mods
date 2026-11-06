package com.example.baritonepatrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PatrolConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE =
			FabricLoader.getInstance().getConfigDir().resolve("baritone-patrol.json");

	private Map<String, List<PatrolPoint>> singleplayer = new LinkedHashMap<>();
	private Map<String, List<PatrolPoint>> multiplayer = new LinkedHashMap<>();

	static PatrolConfig load() {
		if (!Files.isRegularFile(FILE)) {
			return new PatrolConfig();
		}
		try (Reader reader = Files.newBufferedReader(FILE)) {
			PatrolConfig config = GSON.fromJson(reader, PatrolConfig.class);
			if (config == null) {
				return new PatrolConfig();
			}
			config.normalize();
			return config;
		} catch (IOException | JsonParseException exception) {
			BaritonePatrolClient.LOGGER.error("Unable to read {}", FILE, exception);
			return new PatrolConfig();
		}
	}

	List<PatrolPoint> points(boolean multiplayerWorld, String key) {
		Map<String, List<PatrolPoint>> category = multiplayerWorld ? multiplayer : singleplayer;
		return category.computeIfAbsent(key, ignored -> new ArrayList<>());
	}

	void clear(boolean multiplayerWorld, String key) {
		(multiplayerWorld ? multiplayer : singleplayer).remove(key);
		save();
	}

	void save() {
		try {
			Files.createDirectories(FILE.getParent());
			Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(this, writer);
			}
			try {
				Files.move(
						temporary,
						FILE,
						StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException ignored) {
				Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException exception) {
			BaritonePatrolClient.LOGGER.error("Unable to write {}", FILE, exception);
		}
	}

	private void normalize() {
		if (singleplayer == null) {
			singleplayer = new LinkedHashMap<>();
		}
		if (multiplayer == null) {
			multiplayer = new LinkedHashMap<>();
		}
		singleplayer.replaceAll(
				(key, value) -> value == null ? new ArrayList<>() : new ArrayList<>(value));
		multiplayer.replaceAll(
				(key, value) -> value == null ? new ArrayList<>() : new ArrayList<>(value));
	}
}

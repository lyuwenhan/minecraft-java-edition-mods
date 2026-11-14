package com.example.seedgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class SeedgenConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("seedgen");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("seedgen.json");

	public boolean enable = true;
	public boolean showProgress = false;
	public boolean generateForFreecam = false;
	public int radius = 64;
	public int threads = 32;
	public Map<String, Long> seeds = new HashMap<>();

	public static SeedgenConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			SeedgenConfig config = new SeedgenConfig();
			config.save();
			return config;
		}

		try {
			SeedgenConfig config =
					GSON.fromJson(Files.readString(CONFIG_PATH), SeedgenConfig.class);
			if (config == null) {
				config = new SeedgenConfig();
			}
			if (config.seeds == null) {
				config.seeds = new HashMap<>();
			}
			if (config.radius < 1) {
				config.radius = 64;
			}
			if (config.threads < 1) {
				config.threads = 32;
			} else if (config.threads > 256) {
				config.threads = 256;
			}
			config.save();
			return config;
		} catch (IOException | JsonParseException e) {
			LOGGER.error("Failed to load Seedgen config", e);
			return new SeedgenConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			LOGGER.error("Failed to save Seedgen config", e);
		}
	}
}

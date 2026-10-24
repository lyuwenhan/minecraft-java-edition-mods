package com.example.voxyclientside;

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

public final class VoxyClientSideConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("voxy-client-side");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("voxy-client-side.json");

	public boolean enable = true;
	public int radius = 64;
	public int threads = 32;
	public Map<String, Long> seeds = new HashMap<>();

	public static VoxyClientSideConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			VoxyClientSideConfig config = new VoxyClientSideConfig();
			config.save();
			return config;
		}

		try {
			VoxyClientSideConfig config =
					GSON.fromJson(Files.readString(CONFIG_PATH), VoxyClientSideConfig.class);
			if (config == null) {
				config = new VoxyClientSideConfig();
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
			LOGGER.error("Failed to load Voxy Client Side config", e);
			return new VoxyClientSideConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			LOGGER.error("Failed to save Voxy Client Side config", e);
		}
	}
}

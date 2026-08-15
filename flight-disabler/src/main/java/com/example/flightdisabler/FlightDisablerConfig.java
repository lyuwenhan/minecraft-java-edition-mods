package com.example.flightdisabler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FlightDisablerConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("flight-disabler.json");
	public boolean enabled = true;

	public static FlightDisablerConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			FlightDisablerConfig config = new FlightDisablerConfig();
			config.save();
			return config;
		}

		try {
			FlightDisablerConfig config =
					GSON.fromJson(Files.readString(CONFIG_PATH), FlightDisablerConfig.class);
			if (config == null) {
				config = new FlightDisablerConfig();
				config.save();
			}
			return config;
		} catch (IOException | JsonParseException exception) {
			throw new RuntimeException("Failed to load flight-disabler config", exception);
		}
	}

	public void save() {
		try {
			Path parent = CONFIG_PATH.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException exception) {
			throw new RuntimeException("Failed to save flight-disabler config", exception);
		}
	}

	public FlightDisablerConfig copy() {
		FlightDisablerConfig copy = new FlightDisablerConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

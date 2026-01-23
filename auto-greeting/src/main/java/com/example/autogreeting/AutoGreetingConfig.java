package com.example.autogreeting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutoGreetingConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
		FabricLoader.getInstance().getConfigDir().resolve("auto-greeting.json");

	public boolean selfEnabled = true;
	public List<String> selfGreetings = new ArrayList<>();

	public boolean otherEnabled = true;
	public List<String> otherGreetings = new ArrayList<>();

	public static AutoGreetingConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			AutoGreetingConfig cfg = new AutoGreetingConfig();
			cfg.save();
			return cfg;
		}

		try {
			return GSON.fromJson(
				Files.readString(CONFIG_PATH),
				AutoGreetingConfig.class
			);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load auto-greeting config", e);
		}
	}

	public void save() {
		try {
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			throw new RuntimeException("Failed to save auto-greeting config", e);
		}
	}
}

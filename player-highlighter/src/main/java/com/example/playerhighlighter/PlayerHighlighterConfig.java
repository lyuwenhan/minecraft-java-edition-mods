package com.example.playerhighlighter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PlayerHighlighterConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("player-highlighter.json");

	public boolean keep = false;

	public static PlayerHighlighterConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			PlayerHighlighterConfig cfg = new PlayerHighlighterConfig();
			cfg.save();
			return cfg;
		}

		try {
			return GSON.fromJson(
				Files.readString(CONFIG_PATH),
				PlayerHighlighterConfig.class
			);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load player-highlighter config", e);
		}
	}

	public void save() {
		try {
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			throw new RuntimeException("Failed to save player-highlighter config", e);
		}
	}
}

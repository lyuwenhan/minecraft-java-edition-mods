package com.example.entityhighlighter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EntityHighlighterConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("entity-highlighter.json");

	public List<TypeHighlightRule> typeRules = new ArrayList<>();
	public List<GroupHighlightRule> groupRules = new ArrayList<>();

	public static EntityHighlighterConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			return new EntityHighlighterConfig();
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			EntityHighlighterConfig loaded = GSON.fromJson(reader, EntityHighlighterConfig.class);
			if (loaded == null) {
				loaded = new EntityHighlighterConfig();
			}
			if (loaded.typeRules == null) {
				loaded.typeRules = new ArrayList<>();
			}
			if (loaded.groupRules == null) {
				loaded.groupRules = new ArrayList<>();
			}
			return loaded;
		} catch (Exception exception) {
			System.err.println(
					"[EntityHighlighter] Failed to load config: " + exception.getMessage());
			return new EntityHighlighterConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			System.err.println(
					"[EntityHighlighter] Failed to save config: " + exception.getMessage());
		}
	}
}

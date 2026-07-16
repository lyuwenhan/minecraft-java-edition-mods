package com.example.autogreetingserver;

import com.example.autogreetingserver.rules.StringMatchRules;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public class AutoGreetingServerConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("auto-greeting-server.json");

	public boolean serverEnabled = true;
	public List<String> serverGreetings = new ArrayList<>();

	public StringMatchRules serverBlacklist = new StringMatchRules();
	public StringMatchRules serverBlacklistExcept = new StringMatchRules();
	public StringMatchRules serverWhitelist = new StringMatchRules();
	public StringMatchRules serverWhitelistExcept = new StringMatchRules();

	public static AutoGreetingServerConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			AutoGreetingServerConfig cfg = new AutoGreetingServerConfig();
			cfg.save();
			return cfg;
		}

		try {
			AutoGreetingServerConfig cfg =
					GSON.fromJson(Files.readString(CONFIG_PATH), AutoGreetingServerConfig.class);

			if (cfg == null) {
				return new AutoGreetingServerConfig();
			}

			return cfg;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load auto-greeting server config", e);
		}
	}

	public void save() {
		try {
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			throw new RuntimeException("Failed to save auto-greeting server config", e);
		}
	}
}

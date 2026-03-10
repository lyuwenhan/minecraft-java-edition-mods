package com.example.autogreeting.server;

import com.example.autogreeting.rules.StringMatchRules;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutoGreetingServerConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
		FabricLoader.getInstance().getConfigDir().resolve("auto-greeting-server.json");

	public boolean enabled = true;
	public List<String> greetings = new ArrayList<>();

	public StringMatchRules blacklist = new StringMatchRules();
	public StringMatchRules blacklistExcept = new StringMatchRules();
	public StringMatchRules whitelist = new StringMatchRules();
	public StringMatchRules whitelistExcept = new StringMatchRules();

	public static AutoGreetingServerConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			AutoGreetingServerConfig cfg = new AutoGreetingServerConfig();
			cfg.save();
			return cfg;
		}

		try {
			AutoGreetingServerConfig cfg = GSON.fromJson(
				Files.readString(CONFIG_PATH),
				AutoGreetingServerConfig.class
			);
			if (cfg == null) {
				cfg = new AutoGreetingServerConfig();
			}
			if (cfg.greetings == null) cfg.greetings = new ArrayList<>();
			if (cfg.blacklist == null) cfg.blacklist = new StringMatchRules();
			if (cfg.blacklistExcept == null) cfg.blacklistExcept = new StringMatchRules();
			if (cfg.whitelist == null) cfg.whitelist = new StringMatchRules();
			if (cfg.whitelistExcept == null) cfg.whitelistExcept = new StringMatchRules();
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
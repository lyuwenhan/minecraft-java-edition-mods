package com.example.autogreetingclient;

import com.example.autogreetingclient.rules.StringMatchRules;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutoGreetingClientConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
		FabricLoader.getInstance().getConfigDir().resolve("auto-greeting.json");

	public boolean selfEnabled = true;
	public List<String> selfGreetings = new ArrayList<>();

	public boolean otherEnabled = true;
	public List<String> otherGreetings = new ArrayList<>();

	public StringMatchRules otherBlacklist = new StringMatchRules();
	public StringMatchRules otherBlacklistExcept = new StringMatchRules();
	public StringMatchRules otherWhitelist = new StringMatchRules();
	public StringMatchRules otherWhitelistExcept = new StringMatchRules();

	public static AutoGreetingClientConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			AutoGreetingClientConfig cfg = new AutoGreetingClientConfig();
			cfg.save();
			return cfg;
		}

		try {
			AutoGreetingClientConfig cfg = GSON.fromJson(
				Files.readString(CONFIG_PATH),
				AutoGreetingClientConfig.class
			);
			return cfg != null ? cfg : new AutoGreetingClientConfig();
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

	public AutoGreetingClientConfig copy() {
		AutoGreetingClientConfig c = new AutoGreetingClientConfig();
		c.selfEnabled = this.selfEnabled;
		c.selfGreetings = new ArrayList<>(this.selfGreetings);
		c.otherEnabled = this.otherEnabled;
		c.otherGreetings = new ArrayList<>(this.otherGreetings);
		c.otherBlacklist = this.otherBlacklist.copy();
		c.otherBlacklistExcept = this.otherBlacklistExcept.copy();
		c.otherWhitelist = this.otherWhitelist.copy();
		c.otherWhitelistExcept = this.otherWhitelistExcept.copy();
		return c;
	}
}

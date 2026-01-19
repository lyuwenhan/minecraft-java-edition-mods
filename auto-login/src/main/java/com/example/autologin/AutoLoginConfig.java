package com.example.autologin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AutoLoginConfig {

	public Map<String, Credential> servers = new HashMap<>();

	public static class Credential {
		public String enc;
		public String salt;
		public String iv;
		public boolean enabled = true;
	}

	private static final Gson GSON =
			new GsonBuilder().setPrettyPrinting().create();

	private static Path path() {
		return Path.of("config", "auto-login.json");
	}

	public static AutoLoginConfig load() {
		Path p = path();
		if (!Files.exists(p)) {
			return new AutoLoginConfig();
		}
		try {
			return GSON.fromJson(Files.readString(p), AutoLoginConfig.class);
		} catch (IOException e) {
			return new AutoLoginConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(path().getParent());
			Files.writeString(path(), GSON.toJson(this));
		} catch (IOException ignored) {}
	}
}

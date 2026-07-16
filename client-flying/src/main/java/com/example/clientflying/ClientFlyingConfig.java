package com.example.clientflying;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ClientFlyingConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve("client-flying.json");

	public boolean enabled = true;

	public static ClientFlyingConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			ClientFlyingConfig cfg = new ClientFlyingConfig();
			cfg.save();
			return cfg;
		}

		try {
			ClientFlyingConfig cfg =
					GSON.fromJson(Files.readString(CONFIG_PATH), ClientFlyingConfig.class);

			if (cfg == null) {
				cfg = new ClientFlyingConfig();
				cfg.save();
			}

			return cfg;
		} catch (IOException | JsonParseException e) {
			throw new RuntimeException("Failed to load client-flying config", e);
		}
	}

	public void save() {
		try {
			Path parent = CONFIG_PATH.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			throw new RuntimeException("Failed to save client-flying config", e);
		}
	}

	public ClientFlyingConfig copy() {
		ClientFlyingConfig c = new ClientFlyingConfig();
		c.enabled = this.enabled;
		return c;
	}
}

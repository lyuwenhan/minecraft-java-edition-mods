package com.example.doublejump;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class DoubleJumpConfig {
	public static final boolean DEFAULT_ENABLED = true;
	public static final int DEFAULT_JUMP_COUNT = 2;
	public static final boolean DEFAULT_INFINITE_JUMPS = false;
	public static final boolean DEFAULT_COOLDOWN_ENABLED = false;
	public static final int DEFAULT_COOLDOWN_TICKS = 5;
	public static final int MIN_JUMP_COUNT = 2;
	public static final int SLIDER_MAX_JUMP_COUNT = 10;
	public static final int MAX_JUMP_COUNT = Integer.MAX_VALUE;
	public static final int MIN_COOLDOWN_TICKS = 0;
	public static final int MAX_COOLDOWN_TICKS = Integer.MAX_VALUE;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve(DoubleJumpMod.MOD_ID + ".json");
	private static Values current = Values.defaults();

	private DoubleJumpConfig() {}

	public static synchronized void load() {
		if (!Files.exists(CONFIG_PATH)) {
			current = Values.defaults();
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			Values loaded = GSON.fromJson(reader, Values.class);
			current = sanitize(loaded);
		} catch (IOException | RuntimeException ignored) {
			current = Values.defaults();
		}

		save();
	}

	public static synchronized void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(current, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static synchronized Values get() {
		return current.copy();
	}

	public static synchronized void set(Values values) {
		current = sanitize(values);
		save();
	}

	public static synchronized boolean enabled() {
		return current.enabled;
	}

	public static synchronized int jumpCount() {
		return current.jumpCount;
	}

	public static synchronized boolean infiniteJumps() {
		return current.infiniteJumps;
	}

	public static synchronized boolean cooldownEnabled() {
		return current.cooldownEnabled;
	}

	public static synchronized int cooldownTicks() {
		return current.cooldownTicks;
	}

	public static Values sanitize(Values values) {
		Values sanitized = values == null ? Values.defaults() : values.copy();
		if (sanitized.jumpCount < MIN_JUMP_COUNT) {
			sanitized.jumpCount = MIN_JUMP_COUNT;
		}

		if (sanitized.cooldownTicks < MIN_COOLDOWN_TICKS) {
			sanitized.cooldownTicks = MIN_COOLDOWN_TICKS;
		}
		return sanitized;
	}

	public static final class Values {
		public boolean enabled = DEFAULT_ENABLED;
		public int jumpCount = DEFAULT_JUMP_COUNT;
		public boolean infiniteJumps = DEFAULT_INFINITE_JUMPS;
		public boolean cooldownEnabled = DEFAULT_COOLDOWN_ENABLED;
		public int cooldownTicks = DEFAULT_COOLDOWN_TICKS;

		public static Values defaults() {
			return new Values();
		}

		public Values copy() {
			Values copy = new Values();
			copy.enabled = this.enabled;
			copy.jumpCount = this.jumpCount;
			copy.infiniteJumps = this.infiniteJumps;
			copy.cooldownEnabled = this.cooldownEnabled;
			copy.cooldownTicks = this.cooldownTicks;
			return copy;
		}
	}
}

package com.example.boatutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BoatUtilsConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH =
			FabricLoader.getInstance().getConfigDir().resolve(BoatUtilsMod.MOD_ID + ".json");
	private static Values current = Values.defaults();

	private BoatUtilsConfig() {}

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

	public static synchronized boolean unrestrictedViewRotation() {
		return current.unrestrictedViewRotation;
	}

	public static synchronized boolean viewDirectionLockEnabled() {
		return current.viewDirectionLockEnabled;
	}

	public static synchronized boolean blueIceSpeedEverywhere() {
		return current.blueIceSpeedEverywhere;
	}

	public static synchronized boolean preventSinking() {
		return current.preventSinking;
	}

	public static synchronized float boatStepHeight() {
		return current.boatStepHeight;
	}

	public static synchronized boolean handbrakeEnabled() {
		return current.handbrakeEnabled;
	}

	public static synchronized boolean handbrakeBoostEnabled() {
		return current.handbrakeBoostEnabled;
	}

	private static Values sanitize(Values values) {
		if (values == null) {
			return Values.defaults();
		}

		Values sanitized = values.copy();
		sanitized.boatStepHeight =
				Math.round(Math.max(0.0F, Math.min(10.0F, sanitized.boatStepHeight)) * 10.0F)
						/ 10.0F;
		return sanitized;
	}

	public static final class Values {
		public boolean unrestrictedViewRotation;
		public boolean viewDirectionLockEnabled;
		public boolean blueIceSpeedEverywhere;
		public boolean preventSinking;
		public float boatStepHeight;
		public boolean handbrakeEnabled;
		public boolean handbrakeBoostEnabled;

		public static Values defaults() {
			Values values = new Values();
			values.unrestrictedViewRotation = false;
			values.viewDirectionLockEnabled = false;
			values.blueIceSpeedEverywhere = false;
			values.preventSinking = false;
			values.boatStepHeight = 0.0F;
			values.handbrakeEnabled = false;
			values.handbrakeBoostEnabled = false;
			return values;
		}

		public Values copy() {
			Values copy = new Values();
			copy.unrestrictedViewRotation = this.unrestrictedViewRotation;
			copy.viewDirectionLockEnabled = this.viewDirectionLockEnabled;
			copy.blueIceSpeedEverywhere = this.blueIceSpeedEverywhere;
			copy.preventSinking = this.preventSinking;
			copy.boatStepHeight = this.boatStepHeight;
			copy.handbrakeEnabled = this.handbrakeEnabled;
			copy.handbrakeBoostEnabled = this.handbrakeBoostEnabled;
			return copy;
		}
	}
}

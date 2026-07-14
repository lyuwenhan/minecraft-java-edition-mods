package com.example.nohungry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NoHungryConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("no-hungry.json");

    private boolean enabled = true;
    private int foodLevel = 18;

    public static NoHungryConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            NoHungryConfig config = new NoHungryConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            NoHungryConfig config = GSON.fromJson(reader, NoHungryConfig.class);
            if (config == null) {
                config = new NoHungryConfig();
            }

            config.normalize();
            config.save();
            return config;
        } catch (IOException | JsonParseException exception) {
            NoHungryMod.LOGGER.error("Failed to load config from {}. Using defaults.", CONFIG_PATH, exception);
            NoHungryConfig config = new NoHungryConfig();
            config.save();
            return config;
        }
    }

    public void save() {
        this.normalize();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            NoHungryMod.LOGGER.error("Failed to save config to {}.", CONFIG_PATH, exception);
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean toggleEnabled() {
        this.enabled = !this.enabled;
        return this.enabled;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public int getMinimumFoodLevel() {
        return Math.min(this.foodLevel, 20);
    }

    public float getMinimumSaturationLevel() {
        return Math.max(this.foodLevel - 20, 0);
    }

    public void setFoodMinimum(int minimumFoodLevel) {
        this.foodLevel = minimumFoodLevel;
        this.normalize();
    }

    public void setSaturationMinimum(int minimumSaturationLevel) {
        this.foodLevel = minimumSaturationLevel + 20;
        this.normalize();
    }

    private void normalize() {
        this.foodLevel = Math.clamp(this.foodLevel, 0, 40);
    }
}

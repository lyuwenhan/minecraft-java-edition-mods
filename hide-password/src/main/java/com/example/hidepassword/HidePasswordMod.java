package com.example.hidepassword;

import com.example.hidepassword.config.HidePasswordConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HidePasswordMod implements ModInitializer {

    public static final String MOD_ID = "hide-password";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static HidePasswordConfig CONFIG;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;

    private static KeyBinding toggleKey;

    @Override
    public void onInitialize() {

        configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("hide-password.json");

        loadConfig();

        LOGGER.info("HidePassword loaded, enabled={}", CONFIG.enabled);

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.hidepassword.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                KeyBinding.Category.MISC
            )
        );


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                CONFIG.enabled = !CONFIG.enabled;
                saveConfig();
                LOGGER.info("HidePassword enabled={}", CONFIG.enabled);
                if (client.player != null) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal(
                            "HidePassword " + (CONFIG.enabled ? "Enabled" : "Disabled")
                        ),
                        true
                    );
                }
            }
        });
    }

    private static void loadConfig() {
        try {
            if (Files.exists(configPath)) {
                CONFIG = GSON.fromJson(Files.readString(configPath), HidePasswordConfig.class);
            } else {
                CONFIG = new HidePasswordConfig();
                saveConfig();
            }
        } catch (Exception e) {
            CONFIG = new HidePasswordConfig();
            LOGGER.error("Failed to load config", e);
        }
    }

    public static void saveConfig() {
        try {
            Files.writeString(configPath, GSON.toJson(CONFIG));
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}

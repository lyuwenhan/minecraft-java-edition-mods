package com.example.hidepassword;

import com.example.hidepassword.config.HidePasswordConfig;
import com.mojang.brigadier.Command;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
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

        configPath = FabricLoader.getInstance().getConfigDir().resolve("hide-password.json");

        loadConfig();

        LOGGER.info("HidePassword loaded, enabled={}", CONFIG.enabled);

        registerCommands();

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding( "key.hidepassword.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, KeyBinding.Category.MISC)
        );


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                CONFIG.enabled = !CONFIG.enabled;
                saveConfig();
                LOGGER.info("HidePassword enabled={}", CONFIG.enabled);
                if (client.player != null) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal("HidePassword " + (CONFIG.enabled ? "Enabled" : "Disabled")),
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

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("hidepassword")
                .executes(context -> sendStatus(context.getSource()))
                .then(ClientCommandManager.literal("status")
                    .executes(context -> sendStatus(context.getSource())))
                .then(ClientCommandManager.literal("toggle")
                    .executes(context -> setEnabled(context.getSource(), !CONFIG.enabled)))
                .then(ClientCommandManager.literal("on")
                    .executes(context -> setEnabled(context.getSource(), true)))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> setEnabled(context.getSource(), false)))
                .then(ClientCommandManager.literal("hide-length")
                    .executes(context -> sendHideLengthStatus(context.getSource()))
                    .then(ClientCommandManager.literal("status")
                        .executes(context -> sendHideLengthStatus(context.getSource())))
                    .then(ClientCommandManager.literal("toggle")
                        .executes(context -> setHideLength(context.getSource(), !CONFIG.hideLength)))
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setHideLength(context.getSource(), true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setHideLength(context.getSource(), false))))
        ));
    }

    private static int setEnabled(FabricClientCommandSource source, boolean enabled) {
        CONFIG.enabled = enabled;
        saveConfig();
        source.sendFeedback(Text.literal("HidePassword " + (CONFIG.enabled ? "Enabled" : "Disabled")));
        LOGGER.info("HidePassword enabled={}", CONFIG.enabled);
        return Command.SINGLE_SUCCESS;
    }

    private static int sendStatus(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("HidePassword is " + (CONFIG.enabled ? "Enabled" : "Disabled")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHideLength(FabricClientCommandSource source, boolean hideLength) {
        CONFIG.hideLength = hideLength;
        saveConfig();
        source.sendFeedback(Text.literal("HidePassword hide length " + (CONFIG.hideLength ? "Enabled" : "Disabled")));
        LOGGER.info("HidePassword hideLength={}", CONFIG.hideLength);
        return Command.SINGLE_SUCCESS;
    }

    private static int sendHideLengthStatus(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("HidePassword hide length is " + (CONFIG.hideLength ? "Enabled" : "Disabled")));
        return Command.SINGLE_SUCCESS;
    }
}

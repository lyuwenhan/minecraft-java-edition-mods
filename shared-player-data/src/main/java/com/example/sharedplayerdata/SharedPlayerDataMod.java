package com.example.sharedplayerdata;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SharedPlayerDataMod implements DedicatedServerModInitializer {
    public static final String MOD_ID = "shared-player-data";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final SharedProfileManager MANAGER = new SharedProfileManager(LOGGER);

    @Override
    public void onInitializeServer() {
        MANAGER.loadConfig();
        SharedPlayerDataCommands.register();

        ServerLifecycleEvents.SERVER_STOPPING.register(MANAGER::onServerStopping);
        ServerTickEvents.END_SERVER_TICK.register(MANAGER::enforceExclusiveOnlinePlayers);
        ServerLoginConnectionEvents.DISCONNECT.register(
                (listener, server) -> MANAGER.releaseLoginListener(listener));

        LOGGER.info("Shared Player Data initialized for dedicated server use.");
    }
}

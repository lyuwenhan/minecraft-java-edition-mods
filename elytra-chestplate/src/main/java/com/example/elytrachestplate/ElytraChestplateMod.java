package com.example.elytrachestplate;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElytraChestplateMod implements ModInitializer {
    public static final String MOD_ID = "elytra-chestplate";
    public static final Logger LOGGER = LoggerFactory.getLogger("Elytra Chestplate");

    @Override
    public void onInitialize() {
        ModItems.initialize();
    }
}

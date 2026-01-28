package com.example.playerhighlighter;

import net.fabricmc.api.ClientModInitializer;

public class PlayerHighlighterMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[PlayerHighlighter] Client initialized");
    }
}

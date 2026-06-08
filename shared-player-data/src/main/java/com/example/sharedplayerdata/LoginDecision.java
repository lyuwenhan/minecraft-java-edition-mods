package com.example.sharedplayerdata;

import net.minecraft.network.chat.Component;

public record LoginDecision(boolean allowed, Component reason) {
    private static final LoginDecision ALLOWED = new LoginDecision(true, Component.empty());

    public static LoginDecision allow() {
        return ALLOWED;
    }

    public static LoginDecision rejected(Component reason) {
        return new LoginDecision(false, reason);
    }
}

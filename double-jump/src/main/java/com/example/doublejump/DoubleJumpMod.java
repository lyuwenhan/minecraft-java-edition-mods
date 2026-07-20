package com.example.doublejump;

import net.fabricmc.api.ClientModInitializer;

public final class DoubleJumpMod implements ClientModInitializer {
	public static final String MOD_ID = "double-jump";

	@Override
	public void onInitializeClient() {
		DoubleJumpConfig.load();
	}
}

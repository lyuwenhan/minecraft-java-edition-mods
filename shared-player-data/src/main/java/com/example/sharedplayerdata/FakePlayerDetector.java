package com.example.sharedplayerdata;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public final class FakePlayerDetector {
	private static final String CARPET_MOD_ID = "carpet";
	private static final boolean CARPET_LOADED =
			FabricLoader.getInstance().isModLoaded(CARPET_MOD_ID);

	private FakePlayerDetector() {}

	public static boolean isCarpetFakePlayer(ServerPlayer player) {
		if (!CARPET_LOADED) {
			return false;
		}

		return CarpetCompat.isCarpetFakePlayer(player);
	}

	public static boolean isCarpetLoaded() {
		return CARPET_LOADED;
	}
}

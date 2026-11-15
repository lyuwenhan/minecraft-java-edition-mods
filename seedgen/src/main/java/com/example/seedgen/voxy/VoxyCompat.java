package com.example.seedgen.voxy;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;

public final class VoxyCompat {
	private VoxyCompat() {}

	public static GeneratedChunkReceiver find(ClientLevel level) {
		if (!FabricLoader.getInstance().isModLoaded("voxy")) {
			return null;
		}
		return VoxyChunkReceiver.find(level);
	}
}

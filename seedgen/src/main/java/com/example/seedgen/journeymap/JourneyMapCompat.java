package com.example.seedgen.journeymap;

import com.example.seedgen.core.GenerationTarget;
import com.example.seedgen.voxy.GeneratedChunkReceiver;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class JourneyMapCompat {
	private JourneyMapCompat() {}

	public static GeneratedChunkReceiver addReceiver(
			GeneratedChunkReceiver current, ClientLevel level) {
		if (!FabricLoader.getInstance().isModLoaded("journeymap")) {
			return current;
		}
		return JourneyMapIntegration.addReceiver(current, level);
	}

	public static GenerationTarget getTarget(Minecraft client, boolean enabled) {
		if (!FabricLoader.getInstance().isModLoaded("journeymap")) {
			return GenerationTarget.inactive();
		}
		return JourneyMapIntegration.getTarget(client, enabled);
	}
}

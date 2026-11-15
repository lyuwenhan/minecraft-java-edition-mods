package com.example.seedgen.journeymap;

import com.example.seedgen.core.GenerationTarget;
import com.example.seedgen.voxy.CompositeGeneratedChunkReceiver;
import com.example.seedgen.voxy.GeneratedChunkReceiver;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

final class JourneyMapIntegration {
	private JourneyMapIntegration() {}

	static GeneratedChunkReceiver addReceiver(GeneratedChunkReceiver current, ClientLevel level) {
		JourneyMapWriteBridge.useLevel(level);
		if (!JourneyMapGeneratedChunkReceiver.INSTANCE.isLive()) {
			return current;
		}
		if (current == null) {
			return JourneyMapGeneratedChunkReceiver.INSTANCE;
		}
		return new CompositeGeneratedChunkReceiver(
				current, JourneyMapGeneratedChunkReceiver.INSTANCE);
	}

	static GenerationTarget getTarget(Minecraft client, boolean enabled) {
		return JourneyMapGenerationTargetProvider.getTarget(client, enabled);
	}
}

package com.example.seedgen.journeymap;

import com.example.seedgen.voxy.GeneratedChunkReceiver;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class JourneyMapGeneratedChunkReceiver implements GeneratedChunkReceiver {
	public static final JourneyMapGeneratedChunkReceiver INSTANCE =
			new JourneyMapGeneratedChunkReceiver();

	private JourneyMapGeneratedChunkReceiver() {}

	@Override
	public Object destinationKey() {
		return JourneyMapGeneratedChunkReceiver.class;
	}

	@Override
	public boolean isLive() {
		return JourneyMapWriteBridge.isLive();
	}

	@Override
	public boolean hasData(LevelHeightAccessor height, int chunkX, int chunkZ) {
		return JourneyMapWriteBridge.hasData(chunkX, chunkZ);
	}

	@Override
	public int receive(ProtoChunk chunk, LevelLightEngine lightEngine) {
		JourneyMapWriteBridge.enqueue(chunk);
		return 0;
	}
}

package com.example.seedgen.voxy;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class CompositeGeneratedChunkReceiver implements GeneratedChunkReceiver {
	private final GeneratedChunkReceiver primary;
	private final GeneratedChunkReceiver secondary;
	private final DestinationKey destinationKey;

	public CompositeGeneratedChunkReceiver(
			GeneratedChunkReceiver primary, GeneratedChunkReceiver secondary) {
		this.primary = primary;
		this.secondary = secondary;
		this.destinationKey =
				new DestinationKey(primary.destinationKey(), secondary.destinationKey());
	}

	@Override
	public Object destinationKey() {
		return destinationKey;
	}

	@Override
	public boolean isLive() {
		return primary.isLive() && secondary.isLive();
	}

	@Override
	public boolean hasData(LevelHeightAccessor height, int chunkX, int chunkZ) {
		return primary.hasData(height, chunkX, chunkZ) && secondary.hasData(height, chunkX, chunkZ);
	}

	@Override
	public int receive(ProtoChunk chunk, LevelLightEngine lightEngine) {
		int chunkX = chunk.getPos().x();
		int chunkZ = chunk.getPos().z();
		int primarySections = 0;
		if (!primary.hasData(chunk, chunkX, chunkZ)) {
			primarySections = primary.receive(chunk, lightEngine);
		}
		if (!secondary.hasData(chunk, chunkX, chunkZ)) {
			secondary.receive(chunk, lightEngine);
		}
		return primarySections;
	}

	private record DestinationKey(Object primary, Object secondary) {}
}

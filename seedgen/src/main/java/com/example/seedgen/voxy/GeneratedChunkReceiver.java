package com.example.seedgen.voxy;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface GeneratedChunkReceiver {
	Object destinationKey();

	boolean isLive();

	boolean hasData(LevelHeightAccessor height, int chunkX, int chunkZ);

	int receive(ProtoChunk chunk, LevelLightEngine lightEngine);
}

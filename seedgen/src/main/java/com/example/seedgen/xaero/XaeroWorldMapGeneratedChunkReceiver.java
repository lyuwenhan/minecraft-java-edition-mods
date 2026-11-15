package com.example.seedgen.xaero;

import com.example.seedgen.voxy.GeneratedChunkReceiver;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.region.MapTile;

public final class XaeroWorldMapGeneratedChunkReceiver implements GeneratedChunkReceiver {
	public static final XaeroWorldMapGeneratedChunkReceiver INSTANCE =
			new XaeroWorldMapGeneratedChunkReceiver();

	private XaeroWorldMapGeneratedChunkReceiver() {}

	@Override
	public Object destinationKey() {
		return XaeroWorldMapGeneratedChunkReceiver.class;
	}

	@Override
	public boolean isLive() {
		return currentProcessor() != null;
	}

	@Override
	public boolean hasData(LevelHeightAccessor height, int chunkX, int chunkZ) {
		MapProcessor processor = currentProcessor();
		if (processor == null) {
			return false;
		}
		MapTile tile = processor.getMapTile(processor.getCurrentCaveLayer(), chunkX, chunkZ);
		return tile != null && tile.wasWrittenOnce();
	}

	@Override
	public int receive(ProtoChunk chunk, LevelLightEngine lightEngine) {
		MapProcessor processor = currentProcessor();
		if (processor == null) {
			throw new IllegalStateException("Xaero's World Map is not ready to receive chunks");
		}
		ClientLevel level = Minecraft.getInstance().level;
		XaeroWorldMapWriteBridge.enqueue(
				level,
				processor.getCurrentWorldId(),
				processor.getCurrentDimId(),
				processor.getCurrentMWId(),
				chunk);
		return 0;
	}

	private static MapProcessor currentProcessor() {
		Minecraft client = Minecraft.getInstance();
		ClientLevel level = client.level;
		WorldMapSession session = WorldMapSession.getCurrentSession();
		if (level == null || session == null || !session.isUsable()) {
			return null;
		}
		MapProcessor processor = session.getMapProcessor();
		return processor != null
						&& processor.getWorld() == level
						&& processor.isMapWorldUsable()
						&& processor.isCurrentMultiworldWritable()
				? processor
				: null;
	}
}

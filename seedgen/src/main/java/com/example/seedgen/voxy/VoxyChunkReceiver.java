package com.example.seedgen.voxy;

import me.cortex.voxy.common.voxelization.VoxelizedSection;
import me.cortex.voxy.common.voxelization.WorldConversionFactory;
import me.cortex.voxy.common.voxelization.WorldVoxilizedSectionMipper;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldSection;
import me.cortex.voxy.common.world.WorldUpdater;
import me.cortex.voxy.common.world.other.Mapper;
import me.cortex.voxy.commonImpl.WorldIdentifier;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class VoxyChunkReceiver implements GeneratedChunkReceiver {
	private final WorldEngine engine;

	private VoxyChunkReceiver(WorldEngine engine) {
		this.engine = engine;
	}

	public static VoxyChunkReceiver find(ClientLevel level) {
		WorldEngine engine = WorldIdentifier.ofEngineNullable(level);
		return engine == null ? null : new VoxyChunkReceiver(engine);
	}

	@Override
	public Object destinationKey() {
		return engine;
	}

	@Override
	public boolean isLive() {
		return engine.isLive();
	}

	@Override
	public boolean hasData(LevelHeightAccessor height, int chunkX, int chunkZ) {
		for (int sectionY = height.getMinSectionY();
				sectionY < height.getMaxSectionY();
				sectionY++) {
			WorldSection section =
					engine.acquireIfExists(0, chunkX >> 1, sectionY >> 1, chunkZ >> 1);
			if (section == null) {
				continue;
			}
			try {
				if (section.getNonEmptyBlockCount() == 0) {
					continue;
				}
				long[] data = section._unsafeGetRawDataArray();
				int startX = (chunkX & 1) << 4;
				int startY = (sectionY & 1) << 4;
				int startZ = (chunkZ & 1) << 4;
				for (int y = 0; y < 16; y++) {
					int yOffset = (startY + y) << 10;
					for (int z = 0; z < 16; z++) {
						int startIndex = yOffset | ((startZ + z) << 5) | startX;
						int endIndex = startIndex + 16;
						for (int dataIndex = startIndex; dataIndex < endIndex; dataIndex++) {
							long mapping = data[dataIndex];
							if (mapping != Mapper.UNKNOWN_MAPPING
									&& Mapper.isNotAirInt(mapping) != 0) {
								return true;
							}
						}
					}
				}
			} finally {
				section.release();
			}
		}
		return false;
	}

	@Override
	public int receive(ProtoChunk chunk, LevelLightEngine lightEngine) {
		int inserted = 0;
		Mapper mapper = engine.getMapper();
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		var skyLight = lightEngine.getLayerListener(LightLayer.SKY);
		var blockLight = lightEngine.getLayerListener(LightLayer.BLOCK);
		int chunkBaseX = chunk.getPos().x() << 4;
		int chunkBaseZ = chunk.getPos().z() << 4;
		LevelChunkSection[] sections = chunk.getSections();
		for (int index = 0; index < sections.length; index++) {
			LevelChunkSection section = sections[index];
			int sectionY = chunk.getMinSectionY() + index;
			int sectionBaseY = sectionY << 4;
			SectionPos sectionPos = SectionPos.of(chunk.getPos(), sectionY);
			DataLayer blockData = blockLight.getDataLayerData(sectionPos);
			DataLayer skyData = skyLight.getDataLayerData(sectionPos);
			if (section.hasOnlyAir() && (blockData == null || blockData.isEmpty())) {
				continue;
			}
			VoxelizedSection voxelized =
					WorldConversionFactory.convert(
							VoxelizedSection.createEmpty()
									.setPosition(chunk.getPos().x(), sectionY, chunk.getPos().z()),
							mapper,
							section.getStates(),
							section.getBiomes(),
							(x, y, z) -> {
								int sky;
								if (skyData != null) {
									sky = skyData.get(x, y, z);
								} else {
									mutablePos.set(
											chunkBaseX + x, sectionBaseY + y, chunkBaseZ + z);
									sky = skyLight.getLightValue(mutablePos);
								}
								int block = blockData == null ? 0 : blockData.get(x, y, z);
								return (byte) ((block << 4) | sky);
							});
			WorldVoxilizedSectionMipper.mipSection(voxelized, mapper);
			synchronized (engine) {
				WorldUpdater.insertUpdate(engine, voxelized);
			}
			inserted++;
		}
		return inserted;
	}
}

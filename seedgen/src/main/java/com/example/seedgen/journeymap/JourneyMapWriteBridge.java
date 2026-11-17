package com.example.seedgen.journeymap;

import com.mojang.blaze3d.platform.NativeImage;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.io.FileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.model.chunk.ChunkMD;
import journeymap.client.model.map.MapType;
import journeymap.client.model.region.RegionCoord;
import journeymap.client.model.region.RegionImageCache;
import journeymap.client.model.region.RegionImageSet;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.chunk.JMChunkCache;
import journeymap.common.chunk.JMChunkSnapshot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

final class JourneyMapWriteBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger("seedgen");
	private static final int WRITE_BATCH_SIZE = 32;
	private static final ConcurrentLinkedDeque<PendingChunk> PENDING =
			new ConcurrentLinkedDeque<>();
	private static final Set<ChunkKey> QUEUED = ConcurrentHashMap.newKeySet();
	private static final Set<ChunkKey> AVAILABLE = ConcurrentHashMap.newKeySet();
	private static final ConcurrentHashMap<RegionKey, Long> SCANNED_REGIONS =
			new ConcurrentHashMap<>();
	private static final AtomicBoolean TASK_QUEUED = new AtomicBoolean();
	private static volatile ClientLevel currentLevel;

	private JourneyMapWriteBridge() {}

	static synchronized void useLevel(ClientLevel level) {
		if (currentLevel != level) {
			currentLevel = level;
			PENDING.clear();
			QUEUED.clear();
			AVAILABLE.clear();
			SCANNED_REGIONS.clear();
			TASK_QUEUED.set(false);
		}
		pump();
	}

	static boolean isLive() {
		ClientLevel level = currentLevel;
		JourneymapClient client = JourneymapClient.getInstance();
		return level != null
				&& Minecraft.getInstance().level == level
				&& Boolean.TRUE.equals(client.started())
				&& client.enabled()
				&& Boolean.TRUE.equals(client.isMapping())
				&& client.getChunkRenderController() != null;
	}

	static boolean hasData(int chunkX, int chunkZ) {
		ClientLevel level = currentLevel;
		if (level == null) {
			return false;
		}
		ChunkKey key = new ChunkKey(level, ChunkPos.pack(chunkX, chunkZ));
		if (QUEUED.contains(key) || AVAILABLE.contains(key)) {
			return true;
		}
		try {
			scanRegion(level, chunkX, chunkZ);
		} catch (Throwable throwable) {
			LOGGER.warn(
					"Failed to inspect JourneyMap data for generated chunk [{}, {}]",
					chunkX,
					chunkZ,
					throwable);
		}
		return AVAILABLE.contains(key);
	}

	static void enqueue(ProtoChunk chunk) {
		ClientLevel level = currentLevel;
		if (level == null) {
			throw new IllegalStateException("JourneyMap is not ready to receive chunks");
		}
		ChunkKey key = new ChunkKey(level, ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z()));
		if (AVAILABLE.contains(key) || !QUEUED.add(key)) {
			return;
		}
		PENDING.addLast(new PendingChunk(key, chunk));
		pump();
	}

	private static void pump() {
		if (PENDING.isEmpty() || !isLive() || !TASK_QUEUED.compareAndSet(false, true)) {
			return;
		}
		try {
			JourneymapClient.getInstance().queueOneOff(JourneyMapWriteBridge::drain);
		} catch (Exception exception) {
			TASK_QUEUED.set(false);
			LOGGER.warn("Failed to queue generated chunks for JourneyMap", exception);
		}
	}

	private static void drain() {
		Set<RegionImageSet> changedRegions = new HashSet<>();
		try {
			if (!isLive()) {
				return;
			}
			JourneymapClient journeyMap = JourneymapClient.getInstance();
			ChunkRenderController renderer = journeyMap.getChunkRenderController();
			Minecraft minecraft = Minecraft.getInstance();
			File worldDir = FileHandler.getJMWorldDir(minecraft);
			for (int index = 0; index < WRITE_BATCH_SIZE; index++) {
				PendingChunk pending = PENDING.pollFirst();
				if (pending == null) {
					break;
				}
				if (pending.key().level() != currentLevel) {
					QUEUED.remove(pending.key());
					continue;
				}
				try {
					if (hasAuthoritativeData(
							pending.key().level(), pending.chunkX(), pending.chunkZ())) {
						AVAILABLE.add(pending.key());
						QUEUED.remove(pending.key());
						continue;
					}
					LevelChunk chunk = snapshot(pending.key().level(), pending.source());
					ChunkMD metadata = new ChunkMD(chunk, true);
					MapType mapType = MapType.day(pending.key().level().dimension());
					RegionCoord region =
							RegionCoord.fromChunkPos(
									worldDir, mapType, pending.chunkX(), pending.chunkZ());
					JMChunkSnapshot cacheSnapshot = JMChunkSnapshot.fromLevelChunk(chunk, null);
					JMChunkCache.INSTANCE.put(
							cacheSnapshot, pending.key().level().registryAccess());
					DataCache.INSTANCE.addChunkMD(metadata);
					if (!renderer.renderChunk(region, mapType, metadata)) {
						PENDING.addFirst(pending);
						break;
					}
					changedRegions.add(RegionImageCache.INSTANCE.getRegionImageSet(region));
					AVAILABLE.add(pending.key());
					QUEUED.remove(pending.key());
					metadata.stopChunkRetention();
				} catch (Throwable throwable) {
					PENDING.addLast(pending);
					LOGGER.warn(
							"Failed to submit generated chunk [{}, {}] to JourneyMap; retrying",
							pending.chunkX(),
							pending.chunkZ(),
							throwable);
				}
			}
			for (RegionImageSet region : changedRegions) {
				region.finishChunkUpdates();
			}
			if (!changedRegions.isEmpty()) {
				RegionImageCache.INSTANCE.flushToDiskAsync(false);
				Fullscreen.state().requireRefresh();
			}
		} finally {
			TASK_QUEUED.set(false);
			pump();
		}
	}

	private static boolean hasAuthoritativeData(ClientLevel level, int chunkX, int chunkZ) {
		ChunkKey key = new ChunkKey(level, ChunkPos.pack(chunkX, chunkZ));
		scanRegion(level, chunkX, chunkZ, true);
		return AVAILABLE.contains(key);
	}

	private static void scanRegion(ClientLevel level, int chunkX, int chunkZ) {
		scanRegion(level, chunkX, chunkZ, false);
	}

	private static void scanRegion(
			ClientLevel level, int chunkX, int chunkZ, boolean forceRefresh) {
		Minecraft minecraft = Minecraft.getInstance();
		File worldDir = FileHandler.getJMWorldDir(minecraft);
		MapType mapType = MapType.day(level.dimension());
		RegionCoord region = RegionCoord.fromChunkPos(worldDir, mapType, chunkX, chunkZ);
		File imageFile = RegionImageHandler.getRegionImageFile(region, mapType);
		RegionKey regionKey =
				new RegionKey(level, region.regionX, region.regionZ, imageFile.getAbsolutePath());
		long modified = imageFile.isFile() ? imageFile.lastModified() : 0L;
		Long previous = SCANNED_REGIONS.putIfAbsent(regionKey, modified);
		if (!forceRefresh && previous != null && previous == modified) {
			return;
		}
		SCANNED_REGIONS.put(regionKey, modified);
		if (!imageFile.isFile()) {
			return;
		}
		NativeImage image = RegionImageHandler.readRegionImage(imageFile);
		if (image == null) {
			return;
		}
		try (image) {
			for (int localChunkZ = 0; localChunkZ < 32; localChunkZ++) {
				for (int localChunkX = 0; localChunkX < 32; localChunkX++) {
					if (hasOpaquePixel(image, localChunkX << 4, localChunkZ << 4)) {
						int discoveredX = (region.regionX << 5) + localChunkX;
						int discoveredZ = (region.regionZ << 5) + localChunkZ;
						AVAILABLE.add(new ChunkKey(level, ChunkPos.pack(discoveredX, discoveredZ)));
					}
				}
			}
		}
	}

	private static boolean hasOpaquePixel(NativeImage image, int startX, int startZ) {
		for (int z = startZ; z < startZ + 16; z++) {
			for (int x = startX; x < startX + 16; x++) {
				if ((image.getPixel(x, z) >>> 24) != 0) {
					return true;
				}
			}
		}
		return false;
	}

	private static LevelChunk snapshot(ClientLevel level, ProtoChunk source) {
		LevelChunkSection[] sourceSections = source.getSections();
		LevelChunkSection[] sections = new LevelChunkSection[sourceSections.length];
		for (int index = 0; index < sourceSections.length; index++) {
			sections[index] = sourceSections[index].copy();
		}
		LevelChunk chunk =
				new LevelChunk(
						level,
						source.getPos(),
						UpgradeData.EMPTY,
						new LevelChunkTicks<Block>(),
						new LevelChunkTicks<Fluid>(),
						0L,
						sections,
						null,
						null);
		Heightmap.primeHeightmaps(
				chunk, EnumSet.of(Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING));
		chunk.setLightCorrect(true);
		return chunk;
	}

	private record ChunkKey(ClientLevel level, long packedPos) {}

	private record RegionKey(ClientLevel level, int regionX, int regionZ, String imagePath) {}

	private record PendingChunk(ChunkKey key, ProtoChunk source) {
		private int chunkX() {
			return ChunkPos.getX(key.packedPos());
		}

		private int chunkZ() {
			return ChunkPos.getZ(key.packedPos());
		}
	}
}

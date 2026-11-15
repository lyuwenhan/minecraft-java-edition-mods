package com.example.seedgen.xaero;

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

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class XaeroWorldMapWriteBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger("seedgen");
	private static final ConcurrentLinkedDeque<PendingChunk> PENDING =
			new ConcurrentLinkedDeque<>();
	private static final Set<PendingKey> QUEUED = ConcurrentHashMap.newKeySet();
	private static final ThreadLocal<LevelChunk> ACTIVE_CHUNK = new ThreadLocal<>();
	private static volatile ClientLevel currentLevel;

	private XaeroWorldMapWriteBridge() {}

	public static synchronized void useLevel(ClientLevel level) {
		if (currentLevel == level) {
			return;
		}
		currentLevel = level;
		PENDING.clear();
		QUEUED.clear();
	}

	public static void enqueue(
			ClientLevel level,
			String worldId,
			String dimensionId,
			String multiworldId,
			ProtoChunk chunk) {
		PendingKey key =
				new PendingKey(
						level,
						worldId,
						dimensionId,
						multiworldId,
						ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z()));
		if (QUEUED.add(key)) {
			PENDING.addLast(new PendingChunk(key, chunk));
		}
	}

	public static PendingChunk poll() {
		return PENDING.pollFirst();
	}

	public static void retry(PendingChunk chunk) {
		PENDING.addLast(chunk);
	}

	public static void retryFirst(PendingChunk chunk) {
		PENDING.addFirst(chunk);
	}

	public static void complete(PendingChunk chunk) {
		QUEUED.remove(chunk.key());
	}

	public static void fail(PendingChunk chunk, Throwable throwable) {
		LOGGER.warn(
				"Failed to submit generated chunk [{}, {}] to Xaero's World Map; retrying",
				chunk.chunkX(),
				chunk.chunkZ(),
				throwable);
		retry(chunk);
	}

	public static LevelChunk activeChunk() {
		return ACTIVE_CHUNK.get();
	}

	public static void runWithChunk(LevelChunk chunk, Runnable action) {
		ACTIVE_CHUNK.set(chunk);
		try {
			action.run();
		} finally {
			ACTIVE_CHUNK.remove();
		}
	}

	public static final class PendingChunk {
		private final PendingKey key;
		private final ProtoChunk source;
		private LevelChunk snapshot;

		private PendingChunk(PendingKey key, ProtoChunk source) {
			this.key = key;
			this.source = source;
		}

		public ClientLevel level() {
			return key.level();
		}

		public int chunkX() {
			return ChunkPos.getX(key.packedPos());
		}

		public int chunkZ() {
			return ChunkPos.getZ(key.packedPos());
		}

		public boolean belongsTo(String worldId, String dimensionId, String multiworldId) {
			return java.util.Objects.equals(key.worldId(), worldId)
					&& java.util.Objects.equals(key.dimensionId(), dimensionId)
					&& java.util.Objects.equals(key.multiworldId(), multiworldId);
		}

		public LevelChunk snapshot() {
			if (snapshot == null) {
				LevelChunkSection[] sourceSections = source.getSections();
				LevelChunkSection[] sections = new LevelChunkSection[sourceSections.length];
				for (int index = 0; index < sourceSections.length; index++) {
					sections[index] = sourceSections[index].copy();
				}
				snapshot =
						new LevelChunk(
								key.level(),
								source.getPos(),
								UpgradeData.EMPTY,
								new LevelChunkTicks<Block>(),
								new LevelChunkTicks<Fluid>(),
								0L,
								sections,
								null,
								null);
				Heightmap.primeHeightmaps(
						snapshot,
						EnumSet.of(Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING));
				snapshot.setLightCorrect(true);
			}
			return snapshot;
		}

		private PendingKey key() {
			return key;
		}
	}

	private record PendingKey(
			ClientLevel level,
			String worldId,
			String dimensionId,
			String multiworldId,
			long packedPos) {}
}

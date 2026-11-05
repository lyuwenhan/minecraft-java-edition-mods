package com.example.voxygen;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

import me.cortex.voxy.common.voxelization.VoxelizedSection;
import me.cortex.voxy.common.voxelization.WorldConversionFactory;
import me.cortex.voxy.common.voxelization.WorldVoxilizedSectionMipper;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.common.world.WorldSection;
import me.cortex.voxy.common.world.WorldUpdater;
import me.cortex.voxy.common.world.other.Mapper;
import me.cortex.voxy.commonImpl.WorldIdentifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.ticks.BlackholeTickAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

final class VoxygenGenerationService {
	private static final Logger LOGGER = LoggerFactory.getLogger("voxygen");
	private static final ThreadPoolExecutor EXECUTOR =
			new ThreadPoolExecutor(
					32,
					32,
					30L,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue<>(),
					new WorkerThreadFactory());
	private static final ScheduledExecutorService WINDOW_EXECUTOR =
			Executors.newSingleThreadScheduledExecutor(new WindowThreadFactory());
	private static volatile RegistryAccess vanillaWorldgenRegistries;
	private static volatile MultiPackResourceManager vanillaDataResources;
	private static final int REGION_CHUNK_SHIFT = 4;
	private static final int REGION_CHUNK_SIZE = 1 << REGION_CHUNK_SHIFT;
	private static final long NO_ACTIVE_REGION = Long.MIN_VALUE;

	private static volatile Session session;
	private static volatile Component state =
			Component.translatable("command.voxygen.state.inactive");

	private VoxygenGenerationService() {}

	static void initialize() {
		EXECUTOR.allowCoreThreadTimeOut(true);
	}

	static void tick(
			Minecraft client, VoxygenConfig config, String serverAddress, Long effectiveSeed) {
		if (!config.enable
				|| client.level == null
				|| client.player == null
				|| serverAddress == null) {
			retire(
					Component.translatable(
							config.enable
									? "command.voxygen.state.waiting_world"
									: "command.voxygen.state.disabled"));
			return;
		}

		Long seed = effectiveSeed;
		if (seed == null) {
			retire(Component.translatable("command.voxygen.state.no_seed"));
			return;
		}

		ClientLevel level = client.level;
		configureWorkers(config.threads);
		ResourceKey<Level> dimension = level.dimension();
		if (!isSupportedDimension(dimension)) {
			retire(
					Component.translatable(
							"command.voxygen.state.unsupported_dimension",
							dimension.identifier().toString()));
			return;
		}

		WorldEngine engine = WorldIdentifier.ofEngineNullable(level);
		if (engine == null || !engine.isLive()) {
			retire(Component.translatable("command.voxygen.state.voxy_not_ready"));
			return;
		}

		int centerX = Math.floorDiv(client.player.chunkPosition().x(), REGION_CHUNK_SIZE);
		int centerZ = Math.floorDiv(client.player.chunkPosition().z(), REGION_CHUNK_SIZE);
		int radius =
				Math.floorDiv(
						Math.max(0, config.radius) + REGION_CHUNK_SIZE - 1, REGION_CHUNK_SIZE);
		Session current = session;
		if (current != null
				&& (current.level != level || current.seed != seed || current.engine != engine)) {
			current.retire();
			state = Component.translatable("command.voxygen.state.restarting");
		}
		if (current != null && current.retiring) {
			if (!current.isDrained()) {
				return;
			}
			current.closeContext();
			session = null;
			current = null;
		}

		if (current == null) {
			current =
					new Session(
							level,
							engine,
							seed,
							dimension,
							centerX,
							centerZ,
							radius,
							config.threads);
			session = current;
			state =
					Component.translatable(
							"command.voxygen.state.preparing", dimension.identifier().toString());
		}

		current.setWorkerCount(config.threads);
		current.requestWindowUpdate(centerX, centerZ, radius);
		current.pump();
	}

	static void invalidate() {
		retire(Component.translatable("command.voxygen.state.restarting"));
	}

	static Component describeState() {
		Session current = session;
		if (current == null) {
			return state;
		}
		int completed = current.finished.size();
		int total = current.coverage.size();
		int percent = total == 0 ? 100 : (int) ((long) completed * 100L / total);
		return Component.translatable(
				"command.voxygen.generator.details",
				state,
				completed,
				total,
				percent,
				current.workerCount,
				current.inFlight.get(),
				current.generatedChunks.get(),
				current.insertedChunks.get(),
				current.insertedSections.get(),
				current.skippedVoxy.get(),
				current.emptyChunks.get(),
				current.failedChunks.size(),
				current.verificationFailures.get());
	}

	static ProgressSnapshot progressSnapshot() {
		Session current = session;
		if (current == null
				|| current.retiring
				|| current.preparing
				|| current.preparationFailure != null
				|| !current.windowReady) {
			return null;
		}
		int total = current.coverage.size();
		if (total == 0) {
			return null;
		}
		int completed = Math.min(current.finished.size(), total);
		boolean complete;
		synchronized (current.windowLock) {
			complete =
					completed == total
							&& current.inFlight.get() == 0
							&& current.pending.isEmpty()
							&& current.pendingRegions.isEmpty()
							&& !current.windowTaskScheduled
							&& !current.windowTaskRunning;
		}
		return new ProgressSnapshot(current, (float) completed / (float) total, complete);
	}

	record ProgressSnapshot(Object sessionKey, float progress, boolean complete) {}

	private static void retire(Component reason) {
		Session current = session;
		if (current != null) {
			current.retire();
			if (current.isDrained()) {
				current.closeContext();
				session = null;
			}
		}
		state = reason;
	}

	private static boolean isSupportedDimension(ResourceKey<Level> dimension) {
		return Level.OVERWORLD.equals(dimension)
				|| Level.NETHER.equals(dimension)
				|| Level.END.equals(dimension);
	}

	private static synchronized void configureWorkers(int requestedThreads) {
		int threads = Math.max(1, Math.min(256, requestedThreads));
		if (EXECUTOR.getCorePoolSize() == threads && EXECUTOR.getMaximumPoolSize() == threads) {
			return;
		}
		if (threads > EXECUTOR.getMaximumPoolSize()) {
			EXECUTOR.setMaximumPoolSize(threads);
			EXECUTOR.setCorePoolSize(threads);
		} else {
			EXECUTOR.setCorePoolSize(threads);
			EXECUTOR.setMaximumPoolSize(threads);
		}
	}

	private static final class Session {
		private final ClientLevel level;
		private final WorldEngine engine;
		private final long seed;
		private volatile int centerX;
		private volatile int centerZ;
		private volatile int radius;
		private volatile int workerCount;
		private final Object windowLock = new Object();
		private final Object coverageLock = new Object();
		private int requestedCenterX;
		private int requestedCenterZ;
		private int requestedRadius;
		private long requestedWindowVersion;
		private long nextWindowUpdateNanos;
		private boolean windowTaskScheduled;
		private boolean windowTaskRunning;
		private ScheduledFuture<?> windowFuture;
		private volatile boolean windowReady;
		private final Set<Long> coverage = ConcurrentHashMap.newKeySet();
		private final Set<Long> regionCoverage = ConcurrentHashMap.newKeySet();
		private final Set<Long> finished = ConcurrentHashMap.newKeySet();
		private final Set<Long> queued = ConcurrentHashMap.newKeySet();
		private final Set<Long> queuedRegions = ConcurrentHashMap.newKeySet();
		private final Set<Long> activeChunks = ConcurrentHashMap.newKeySet();
		private final Set<Long> failedChunks = ConcurrentHashMap.newKeySet();
		private final ConcurrentLinkedDeque<Long> pending = new ConcurrentLinkedDeque<>();
		private final ConcurrentLinkedDeque<Long> pendingRegions = new ConcurrentLinkedDeque<>();
		private final ConcurrentLinkedDeque<FailedAttempt> failedAttempts =
				new ConcurrentLinkedDeque<>();
		private final Map<Long, Integer> failureCounts = new ConcurrentHashMap<>();
		private final AtomicInteger inFlight = new AtomicInteger();
		private volatile long activeRegion = NO_ACTIVE_REGION;
		private final AtomicInteger generatedChunks = new AtomicInteger();
		private final AtomicInteger insertedChunks = new AtomicInteger();
		private final AtomicInteger insertedSections = new AtomicInteger();
		private final AtomicInteger skippedVoxy = new AtomicInteger();
		private final AtomicInteger emptyChunks = new AtomicInteger();
		private final AtomicInteger verificationFailures = new AtomicInteger();
		private final AtomicInteger finishedSinceCacheCleanup = new AtomicInteger();
		private int cacheCenterX;
		private int cacheCenterZ;
		private int evictedInnerRadius = -1;
		private volatile GenerationContext context;
		private volatile Throwable preparationFailure;
		private volatile boolean preparing;
		private volatile boolean retiring;

		private Session(
				ClientLevel level,
				WorldEngine engine,
				long seed,
				ResourceKey<Level> dimension,
				int centerX,
				int centerZ,
				int radius,
				int workerCount) {
			this.level = level;
			this.engine = engine;
			this.seed = seed;
			this.centerX = centerX;
			this.centerZ = centerZ;
			this.radius = radius;
			this.workerCount = workerCount;
			this.requestedCenterX = centerX;
			this.requestedCenterZ = centerZ;
			this.requestedRadius = radius;
			this.cacheCenterX = (centerX << REGION_CHUNK_SHIFT) + (REGION_CHUNK_SIZE >>> 1);
			this.cacheCenterZ = (centerZ << REGION_CHUNK_SHIFT) + (REGION_CHUNK_SIZE >>> 1);
			this.requestedWindowVersion = 1L;
			this.preparing = true;
			EXECUTOR.execute(
					() -> {
						try {
							context = GenerationContext.create(level, dimension, seed);
							if (!retiring && session == this) {
								state =
										Component.translatable(
												"command.voxygen.state.running",
												dimension.identifier().toString());
							}
						} catch (Throwable throwable) {
							preparationFailure = throwable;
							if (!retiring && session == this) {
								state =
										Component.translatable(
												"command.voxygen.state.worldgen_unavailable");
							}
							LOGGER.warn(
									"Cannot create local generator for {}, reason: {}",
									dimension.identifier(),
									failureReason(throwable),
									throwable);
						} finally {
							preparing = false;
						}
					});
		}

		private void setWorkerCount(int requestedThreads) {
			workerCount = Math.max(1, Math.min(256, requestedThreads));
		}

		private void retire() {
			retiring = true;
			GenerationContext currentContext = context;
			if (currentContext != null) {
				currentContext.cancel();
			}
			synchronized (windowLock) {
				if (windowFuture != null && windowFuture.cancel(false)) {
					windowTaskScheduled = false;
					windowFuture = null;
				}
			}
		}

		private void closeContext() {
			GenerationContext currentContext = context;
			if (currentContext != null) {
				currentContext.close();
			}
		}

		private boolean isDrained() {
			synchronized (windowLock) {
				return !preparing
						&& inFlight.get() == 0
						&& !windowTaskScheduled
						&& !windowTaskRunning;
			}
		}

		private void requestWindowUpdate(int newCenterX, int newCenterZ, int newRadius) {
			synchronized (windowLock) {
				if (retiring) {
					return;
				}
				if (requestedCenterX == newCenterX
						&& requestedCenterZ == newCenterZ
						&& requestedRadius == newRadius) {
					if (!windowTaskScheduled
							&& (!windowReady
									|| centerX != newCenterX
									|| centerZ != newCenterZ
									|| radius != newRadius)) {
						long delay = Math.max(0L, nextWindowUpdateNanos - System.nanoTime());
						scheduleWindowUpdate(delay);
					}
					return;
				}
				requestedCenterX = newCenterX;
				requestedCenterZ = newCenterZ;
				requestedRadius = newRadius;
				requestedWindowVersion++;
				if (!windowTaskScheduled) {
					long delay = Math.max(0L, nextWindowUpdateNanos - System.nanoTime());
					scheduleWindowUpdate(delay);
				}
			}
		}

		private void scheduleWindowUpdate(long delayNanos) {
			windowTaskScheduled = true;
			windowFuture =
					WINDOW_EXECUTOR.schedule(
							this::runWindowUpdate, delayNanos, TimeUnit.NANOSECONDS);
		}

		private void runWindowUpdate() {
			synchronized (windowLock) {
				windowTaskRunning = true;
			}
			try {
				performWindowUpdate();
			} catch (Throwable throwable) {
				synchronized (windowLock) {
					nextWindowUpdateNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
					finishWindowTask();
				}
				LOGGER.warn(
						"Failed to update the Voxygen generation window, reason: {}",
						failureReason(throwable),
						throwable);
			}
		}

		private void performWindowUpdate() {
			int newCenterX;
			int newCenterZ;
			int newRadius;
			long version;
			synchronized (windowLock) {
				if (retiring) {
					finishWindowTask();
					return;
				}
				newCenterX = requestedCenterX;
				newCenterZ = requestedCenterZ;
				newRadius = requestedRadius;
				version = requestedWindowVersion;
			}

			List<Long> orderedRegions = squareInChebyshevOrder(newCenterX, newCenterZ, newRadius);
			Set<Long> nextRegionCoverage = new HashSet<>(orderedRegions);
			Set<Long> nextCoverage =
					new HashSet<>(orderedRegions.size() * REGION_CHUNK_SIZE * REGION_CHUNK_SIZE);
			for (long region : orderedRegions) {
				addRegionChunks(nextCoverage, region);
			}
			synchronized (windowLock) {
				if (retiring || version != requestedWindowVersion) {
					completeWindowUpdate(version);
					return;
				}
			}

			synchronized (coverageLock) {
				coverage.retainAll(nextCoverage);
				regionCoverage.clear();
				regionCoverage.addAll(nextRegionCoverage);
				finished.retainAll(nextCoverage);
				failedChunks.retainAll(nextCoverage);
				failureCounts.keySet().retainAll(nextCoverage);
				failedAttempts.removeIf(failure -> !nextCoverage.contains(failure.target()));
				coverage.addAll(nextCoverage);
				pending.clear();
				queued.clear();
				pendingRegions.clear();
				queuedRegions.clear();
				boolean keepActive =
						activeRegion != NO_ACTIVE_REGION
								&& nextRegionCoverage.contains(activeRegion);
				if (keepActive) {
					enqueueRegionChunks(activeRegion);
				} else if (inFlight.get() == 0) {
					activeRegion = NO_ACTIVE_REGION;
				}
				for (long region : orderedRegions) {
					if (region != activeRegion && queuedRegions.add(region)) {
						pendingRegions.addLast(region);
					}
				}
				centerX = newCenterX;
				centerZ = newCenterZ;
				radius = newRadius;
				int newCacheCenterX =
						(newCenterX << REGION_CHUNK_SHIFT) + (REGION_CHUNK_SIZE >>> 1);
				int newCacheCenterZ =
						(newCenterZ << REGION_CHUNK_SHIFT) + (REGION_CHUNK_SIZE >>> 1);
				if (cacheCenterX != newCacheCenterX || cacheCenterZ != newCacheCenterZ) {
					cacheCenterX = newCacheCenterX;
					cacheCenterZ = newCacheCenterZ;
					evictedInnerRadius = -1;
				}
				windowReady = true;
			}
			synchronized (windowLock) {
				completeWindowUpdate(version);
			}
		}

		private void completeWindowUpdate(long completedVersion) {
			nextWindowUpdateNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
			if (!retiring && requestedWindowVersion != completedVersion) {
				windowTaskRunning = false;
				windowFuture =
						WINDOW_EXECUTOR.schedule(this::runWindowUpdate, 1L, TimeUnit.SECONDS);
				return;
			}
			finishWindowTask();
		}

		private void finishWindowTask() {
			windowTaskScheduled = false;
			windowTaskRunning = false;
			windowFuture = null;
		}

		private static void addRegionChunks(Set<Long> destination, long region) {
			int startX = ChunkPos.getX(region) << REGION_CHUNK_SHIFT;
			int startZ = ChunkPos.getZ(region) << REGION_CHUNK_SHIFT;
			for (int offsetZ = 0; offsetZ < REGION_CHUNK_SIZE; offsetZ++) {
				for (int offsetX = 0; offsetX < REGION_CHUNK_SIZE; offsetX++) {
					destination.add(ChunkPos.pack(startX + offsetX, startZ + offsetZ));
				}
			}
		}

		private void enqueueRegionChunks(long region) {
			int startX = ChunkPos.getX(region) << REGION_CHUNK_SHIFT;
			int startZ = ChunkPos.getZ(region) << REGION_CHUNK_SHIFT;
			switch (traversalFor(region)) {
				case SOUTH -> {
					for (int offsetZ = 0; offsetZ < REGION_CHUNK_SIZE; offsetZ++) {
						for (int offsetX = 0; offsetX < REGION_CHUNK_SIZE; offsetX++) {
							enqueueChunk(startX + offsetX, startZ + offsetZ);
						}
					}
				}
				case WEST -> {
					for (int offsetX = REGION_CHUNK_SIZE - 1; offsetX >= 0; offsetX--) {
						for (int offsetZ = 0; offsetZ < REGION_CHUNK_SIZE; offsetZ++) {
							enqueueChunk(startX + offsetX, startZ + offsetZ);
						}
					}
				}
				case NORTH -> {
					for (int offsetZ = REGION_CHUNK_SIZE - 1; offsetZ >= 0; offsetZ--) {
						for (int offsetX = 0; offsetX < REGION_CHUNK_SIZE; offsetX++) {
							enqueueChunk(startX + offsetX, startZ + offsetZ);
						}
					}
				}
				case CENTER, EAST -> {
					for (int offsetX = 0; offsetX < REGION_CHUNK_SIZE; offsetX++) {
						for (int offsetZ = 0; offsetZ < REGION_CHUNK_SIZE; offsetZ++) {
							enqueueChunk(startX + offsetX, startZ + offsetZ);
						}
					}
				}
			}
		}

		private RegionTraversal traversalFor(long region) {
			int deltaX = ChunkPos.getX(region) - centerX;
			int deltaZ = ChunkPos.getZ(region) - centerZ;
			int distance = Math.max(Math.abs(deltaX), Math.abs(deltaZ));
			if (distance == 0) {
				return RegionTraversal.CENTER;
			}
			if (deltaX == distance && deltaZ == 1 - distance) {
				return RegionTraversal.EAST;
			}
			if (deltaX == distance && deltaZ > -distance) {
				return RegionTraversal.SOUTH;
			}
			if (deltaZ == distance && deltaX < distance) {
				return RegionTraversal.WEST;
			}
			if (deltaX == -distance && deltaZ < distance) {
				return RegionTraversal.NORTH;
			}
			return RegionTraversal.EAST;
		}

		private void enqueueChunk(int chunkX, int chunkZ) {
			long packed = ChunkPos.pack(chunkX, chunkZ);
			if (coverage.contains(packed)
					&& !finished.contains(packed)
					&& !activeChunks.contains(packed)
					&& queued.add(packed)) {
				pending.addLast(packed);
			}
		}

		private boolean ensureActiveRegion() {
			synchronized (coverageLock) {
				if (activeRegion != NO_ACTIVE_REGION
						&& (!pending.isEmpty() || inFlight.get() > 0)) {
					return true;
				}
				if (activeRegion != NO_ACTIVE_REGION && recoverFailedAttempts()) {
					return true;
				}
				activeRegion = NO_ACTIVE_REGION;
				Long nextRegion;
				while ((nextRegion = pendingRegions.pollFirst()) != null) {
					queuedRegions.remove(nextRegion);
					if (!regionCoverage.contains(nextRegion)) {
						continue;
					}
					activeRegion = nextRegion;
					enqueueRegionChunks(nextRegion);
					if (!pending.isEmpty()) {
						return true;
					}
					activeRegion = NO_ACTIVE_REGION;
				}
				return false;
			}
		}

		private boolean recoverFailedAttempts() {
			Set<Long> accessed = new HashSet<>();
			Set<Long> retryTargets = new HashSet<>();
			FailedAttempt failure;
			while ((failure = failedAttempts.pollFirst()) != null) {
				accessed.addAll(failure.accessedChunks());
				int failureCount = failureCounts.merge(failure.target(), 1, Integer::sum);
				if (failureCount <= 2) {
					retryTargets.add(failure.target());
				} else {
					failedChunks.add(failure.target());
					finished.add(failure.target());
				}
			}
			if (accessed.isEmpty() && retryTargets.isEmpty()) {
				return false;
			}

			context.invalidateAccessed(accessed);
			for (long packed : accessed) {
				if (isInRegion(packed, activeRegion)
						&& coverage.contains(packed)
						&& !failedChunks.contains(packed)) {
					finished.remove(packed);
				}
			}
			for (long packed : retryTargets) {
				finished.remove(packed);
			}
			enqueueRegionChunks(activeRegion);
			return !pending.isEmpty();
		}

		private static boolean isInRegion(long chunk, long region) {
			return Math.floorDiv(ChunkPos.getX(chunk), REGION_CHUNK_SIZE) == ChunkPos.getX(region)
					&& Math.floorDiv(ChunkPos.getZ(chunk), REGION_CHUNK_SIZE)
							== ChunkPos.getZ(region);
		}

		private void pump() {
			if (retiring
					|| preparing
					|| !windowReady
					|| preparationFailure != null
					|| context == null
					|| session != this) {
				return;
			}
			state =
					Component.translatable(
							"command.voxygen.state.running",
							level.dimension().identifier().toString());
			if (!ensureActiveRegion()) {
				if (inFlight.get() == 0) {
					state = Component.translatable("command.voxygen.state.complete");
				}
				return;
			}
			while (true) {
				ScheduledChunk next;
				synchronized (coverageLock) {
					if (inFlight.get() >= workerCount) {
						return;
					}
					next = pollNext();
					if (next != null) {
						inFlight.incrementAndGet();
					}
				}
				if (next == null) {
					return;
				}
				long packed = next.packed();
				int chunkX = ChunkPos.getX(packed);
				int chunkZ = ChunkPos.getZ(packed);

				EXECUTOR.execute(
						() -> {
							boolean succeeded = false;
							boolean abandoned = false;
							Set<Long> accessed = context.beginAccessTracking();
							try {
								succeeded = generateOne(this, chunkX, chunkZ);
							} catch (Throwable throwable) {
								if (throwable instanceof CancellationException
										|| retiring
										|| !engine.isLive()) {
									abandoned = true;
									if (!engine.isLive()) {
										retire();
									}
								} else {
									LOGGER.warn(
											"Failed to generate chunk [{}, {}], reason: {}",
											chunkX,
											chunkZ,
											failureReason(throwable),
											throwable);
								}
							} finally {
								context.endAccessTracking(accessed);
								activeChunks.remove(packed);
								if (abandoned) {
								} else if (succeeded) {
									failedChunks.remove(packed);
									failureCounts.remove(packed);
									markFinished(packed);
								} else {
									handleFailure(next, accessed);
								}
								inFlight.decrementAndGet();
							}
						});
			}
		}

		private ScheduledChunk pollNext() {
			return poll(pending, queued);
		}

		private ScheduledChunk poll(ConcurrentLinkedDeque<Long> queue, Set<Long> membership) {
			Long packed;
			while ((packed = queue.pollFirst()) != null) {
				if (packed == null) {
					return null;
				}
				membership.remove(packed);
				if (coverage.contains(packed)
						&& !finished.contains(packed)
						&& activeChunks.add(packed)) {
					return new ScheduledChunk(packed);
				}
			}
			return null;
		}

		private void handleFailure(ScheduledChunk task, Set<Long> accessed) {
			long packed = task.packed();
			if (retiring || !coverage.contains(packed)) {
				return;
			}
			failedAttempts.addLast(new FailedAttempt(packed, Set.copyOf(accessed)));
		}

		private void markFinished(long packed) {
			synchronized (coverageLock) {
				if (coverage.contains(packed)) {
					finished.add(packed);
				}
			}
			if (finishedSinceCacheCleanup.incrementAndGet() >= 128
					&& finishedSinceCacheCleanup.getAndSet(0) >= 128) {
				cleanupFinishedInnerCache();
			}
		}

		private void cleanupFinishedInnerCache() {
			GenerationContext currentContext = context;
			if (currentContext == null || retiring) {
				return;
			}
			synchronized (coverageLock) {
				int minimumUnfinishedDistance = Integer.MAX_VALUE;
				for (long candidate : coverage) {
					if (!finished.contains(candidate)) {
						int distance =
								Math.max(
										Math.abs(ChunkPos.getX(candidate) - cacheCenterX),
										Math.abs(ChunkPos.getZ(candidate) - cacheCenterZ));
						minimumUnfinishedDistance = Math.min(minimumUnfinishedDistance, distance);
					}
				}
				int safeRadius;
				if (minimumUnfinishedDistance == Integer.MAX_VALUE) {
					safeRadius =
							radius * REGION_CHUNK_SIZE
									+ (REGION_CHUNK_SIZE >>> 1)
									+ currentContext.cacheDependencyMargin();
				} else {
					safeRadius =
							minimumUnfinishedDistance - currentContext.cacheDependencyMargin() - 1;
				}
				if (safeRadius > evictedInnerRadius) {
					currentContext.evictInnerRings(
							cacheCenterX, cacheCenterZ, evictedInnerRadius + 1, safeRadius);
					evictedInnerRadius = safeRadius;
				}
			}
		}
	}

	private record ScheduledChunk(long packed) {}

	private record FailedAttempt(long target, Set<Long> accessedChunks) {}

	private enum RegionTraversal {
		CENTER,
		SOUTH,
		WEST,
		NORTH,
		EAST
	}

	private static boolean generateOne(Session owner, int chunkX, int chunkZ) {
		if (owner.retiring || !owner.engine.isLive()) {
			throw new CancellationException("Voxygen world is no longer live");
		}
		if (hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
			owner.skippedVoxy.incrementAndGet();
			return true;
		}

		owner.context.prepareFull(chunkX, chunkZ);
		GeneratedChunk generated = owner.context.generatePreparedFull(chunkX, chunkZ);
		ProtoChunk chunk = generated.chunk();
		owner.generatedChunks.incrementAndGet();
		if (hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
			owner.skippedVoxy.incrementAndGet();
			return true;
		}

		LevelLightEngine lightEngine = owner.context.light(chunkX, chunkZ);
		List<VoxelizedSection> voxelized = voxelize(owner.engine, chunk, lightEngine);
		if (voxelized.isEmpty()) {
			owner.emptyChunks.incrementAndGet();
			return true;
		}
		synchronized (owner.engine) {
			if (hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
				owner.skippedVoxy.incrementAndGet();
				return true;
			}
			for (VoxelizedSection section : voxelized) {
				WorldUpdater.insertUpdate(owner.engine, section);
			}
			owner.insertedChunks.incrementAndGet();
			owner.insertedSections.addAndGet(voxelized.size());
			if (!hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
				owner.verificationFailures.incrementAndGet();
				LOGGER.warn(
						"Voxy data was not visible after inserting chunk [{}, {}]", chunkX, chunkZ);
			}
		}
		return true;
	}

	private static List<VoxelizedSection> voxelize(
			WorldEngine engine, ProtoChunk chunk, LevelLightEngine lightEngine) {
		List<VoxelizedSection> result = new ArrayList<>();
		LevelChunkSection[] sections = chunk.getSections();
		for (int index = 0; index < sections.length; index++) {
			LevelChunkSection section = sections[index];
			if (section.hasOnlyAir()) {
				continue;
			}
			int sectionY = chunk.getMinSectionY() + index;
			VoxelizedSection voxelized =
					WorldConversionFactory.convert(
							VoxelizedSection.createEmpty()
									.setPosition(chunk.getPos().x(), sectionY, chunk.getPos().z()),
							engine.getMapper(),
							section.getStates(),
							section.getBiomes(),
							(x, y, z) -> {
								BlockPos pos =
										new BlockPos(
												(chunk.getPos().x() << 4) + x,
												(sectionY << 4) + y,
												(chunk.getPos().z() << 4) + z);
								int sky =
										lightEngine
												.getLayerListener(LightLayer.SKY)
												.getLightValue(pos);
								int block =
										lightEngine
												.getLayerListener(LightLayer.BLOCK)
												.getLightValue(pos);
								return (byte) ((block << 4) | sky);
							});
			WorldVoxilizedSectionMipper.mipSection(voxelized, engine.getMapper());
			result.add(voxelized);
		}
		return result;
	}

	private static boolean hasVoxyData(
			WorldEngine engine, LevelHeightAccessor height, int chunkX, int chunkZ) {
		for (int sectionY = height.getMinSectionY();
				sectionY < height.getMaxSectionY();
				sectionY++) {
			WorldSection section =
					engine.acquireIfExists(0, chunkX >> 1, sectionY >> 1, chunkZ >> 1);
			if (section == null) {
				continue;
			}
			try {
				long[] data = section._unsafeGetRawDataArray();
				int startX = (chunkX & 1) << 4;
				int startY = (sectionY & 1) << 4;
				int startZ = (chunkZ & 1) << 4;
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						for (int x = 0; x < 16; x++) {
							long mapping =
									data[WorldSection.getIndex(startX + x, startY + y, startZ + z)];
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

	private static String failureReason(Throwable throwable) {
		String message = throwable.getMessage();
		return throwable.getClass().getSimpleName()
				+ (message == null || message.isBlank() ? "" : ": " + message);
	}

	static List<Long> squareInChebyshevOrder(int centerX, int centerZ, int radius) {
		List<Long> result = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
		result.add(ChunkPos.pack(centerX, centerZ));
		for (int distance = 1; distance <= radius; distance++) {
			int minX = centerX - distance;
			int maxX = centerX + distance;
			int minZ = centerZ - distance;
			int maxZ = centerZ + distance;

			for (int z = minZ + 1; z <= maxZ; z++) {
				result.add(ChunkPos.pack(maxX, z));
			}
			for (int x = maxX - 1; x >= minX; x--) {
				result.add(ChunkPos.pack(x, maxZ));
			}
			for (int z = maxZ - 1; z >= minZ; z--) {
				result.add(ChunkPos.pack(minX, z));
			}
			for (int x = minX + 1; x <= maxX; x++) {
				result.add(ChunkPos.pack(x, minZ));
			}
		}
		return result;
	}

	private static final class GenerationContext {
		private final ClientLevel sourceLevel;
		private final RegistryAccess registries;
		private final LevelHeightAccessor height;
		private final ChunkGenerator generator;
		private final RandomState randomState;
		private final PalettedContainerFactory containers;
		private final long seed;
		private final ResourceKey<Level> dimension;
		private final ServerLevel serverLevel;
		private final StructureTemplateManager structureTemplates;
		private final ChunkGeneratorStructureState structureState;
		private final LevelStorageSource.LevelStorageAccess storageAccess;
		private final ConcurrentHashMap<Long, ProtoChunk> chunks = new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, CompletableFuture<ProtoChunk>> structureStarts =
				new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, CompletableFuture<ProtoChunk>> structureReferences =
				new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, CompletableFuture<ProtoChunk>> biomes =
				new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, CompletableFuture<ProtoChunk>> terrain =
				new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, CompletableFuture<Void>> featurePreparations =
				new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, CompletableFuture<ProtoChunk>> features =
				new ConcurrentHashMap<>();
		private final ThreadLocal<Set<Long>> accessTracking = new ThreadLocal<>();
		private final ReentrantLock[] writeLocks = new ReentrantLock[4096];
		private final LocalWorld localWorld;
		private final WorldGenLevel world;
		private final StructureManager structures;
		private final WorldGenContext nativeContext;
		private volatile boolean cancelled;
		private boolean closed;

		private GenerationContext(
				ClientLevel sourceLevel,
				RegistryAccess registries,
				LevelHeightAccessor height,
				ChunkGenerator generator,
				RandomState randomState,
				PalettedContainerFactory containers,
				long seed,
				ResourceKey<Level> dimension,
				ServerLevel serverLevel,
				StructureTemplateManager structureTemplates,
				ChunkGeneratorStructureState structureState,
				LevelStorageSource.LevelStorageAccess storageAccess) {
			this.sourceLevel = sourceLevel;
			this.registries = registries;
			this.height = height;
			this.generator = generator;
			this.randomState = randomState;
			this.containers = containers;
			this.seed = seed;
			this.dimension = dimension;
			this.serverLevel = serverLevel;
			this.structureTemplates = structureTemplates;
			this.structureState = structureState;
			this.storageAccess = storageAccess;
			if (serverLevel == null) {
				structureState.ensureStructuresGenerated();
			}
			for (int index = 0; index < writeLocks.length; index++) {
				writeLocks[index] = new ReentrantLock();
			}
			this.localWorld = new LocalWorld(this, chunks);
			this.world = localWorld.proxy();
			this.structures =
					new StructureManager(world, new WorldOptions(seed, false, false), null);
			this.nativeContext =
					serverLevel == null
							? null
							: new WorldGenContext(
									serverLevel,
									generator,
									structureTemplates,
									serverLevel.getChunkSource().getLightEngine(),
									Runnable::run,
									ignored -> {});
		}

		private static GenerationContext create(
				ClientLevel level, ResourceKey<Level> dimension, long seed) {
			Minecraft client = Minecraft.getInstance();
			if (client.getSingleplayerServer() != null) {
				ServerLevel serverLevel = client.getSingleplayerServer().getLevel(dimension);
				if (serverLevel != null) {
					RegistryAccess registries = serverLevel.registryAccess();
					ChunkGenerator serverGenerator = serverLevel.getChunkSource().getGenerator();
					return new GenerationContext(
							level,
							registries,
							LevelHeightAccessor.create(
									serverLevel.getMinY(), serverLevel.getHeight()),
							serverGenerator,
							serverLevel.getChunkSource().randomState(),
							PalettedContainerFactory.create(registries),
							seed,
							dimension,
							serverLevel,
							serverLevel.getStructureTemplateManager(),
							serverLevel.getChunkSource().getGeneratorState(),
							null);
				}
			}

			RegistryAccess registries = worldgenRegistries();
			Holder<NoiseGeneratorSettings> settings;
			BiomeSource biomes;
			if (Level.OVERWORLD.equals(dimension)) {
				settings =
						registries
								.lookupOrThrow(Registries.NOISE_SETTINGS)
								.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
				Holder<MultiNoiseBiomeSourceParameterList> preset =
						registries
								.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
								.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
				biomes = MultiNoiseBiomeSource.createFromPreset(preset);
			} else if (Level.NETHER.equals(dimension)) {
				settings =
						registries
								.lookupOrThrow(Registries.NOISE_SETTINGS)
								.getOrThrow(NoiseGeneratorSettings.NETHER);
				Holder<MultiNoiseBiomeSourceParameterList> preset =
						registries
								.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
								.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
				biomes = MultiNoiseBiomeSource.createFromPreset(preset);
			} else if (Level.END.equals(dimension)) {
				settings =
						registries
								.lookupOrThrow(Registries.NOISE_SETTINGS)
								.getOrThrow(NoiseGeneratorSettings.END);
				HolderGetter<Biome> biomeLookup = registries.lookupOrThrow(Registries.BIOME);
				biomes = TheEndBiomeSource.create(biomeLookup);
			} else {
				throw new IllegalArgumentException(
						"Unsupported dimension " + dimension.identifier());
			}

			HolderGetter<NormalNoise> noises = registries.lookupOrThrow(Registries.NOISE);
			NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(biomes, settings);
			RandomState randomState = RandomState.create(noises, seed, settings.value());
			LevelHeightAccessor height =
					LevelHeightAccessor.create(level.getMinY(), level.getHeight());
			LevelStorageSource storage =
					LevelStorageSource.createDefault(
							client.gameDirectory.toPath().resolve("voxygen-virtual"));
			String encodedDimension =
					Base64.getUrlEncoder()
							.withoutPadding()
							.encodeToString(
									dimension
											.identifier()
											.toString()
											.getBytes(StandardCharsets.UTF_8));
			LevelStorageSource.LevelStorageAccess access;
			try {
				access = storage.createAccess(seed + "-" + encodedDimension);
			} catch (IOException exception) {
				throw new IllegalStateException("Cannot open the virtual world", exception);
			}
			StructureTemplateManager templates =
					new StructureTemplateManager(
							vanillaDataResources,
							access,
							DataFixers.getDataFixer(),
							registries.lookupOrThrow(Registries.BLOCK));
			ChunkGeneratorStructureState structureState =
					generator.createState(
							registries.lookupOrThrow(Registries.STRUCTURE_SET), randomState, seed);
			((VirtualStructureState) structureState).voxygen$markVirtual();
			return new GenerationContext(
					level,
					registries,
					height,
					generator,
					randomState,
					PalettedContainerFactory.create(registries),
					seed,
					dimension,
					null,
					templates,
					structureState,
					access);
		}

		private static RegistryAccess worldgenRegistries() {
			Minecraft client = Minecraft.getInstance();
			if (client.getSingleplayerServer() != null) {
				return client.getSingleplayerServer().registryAccess();
			}

			RegistryAccess cached = vanillaWorldgenRegistries;
			if (cached != null) {
				return cached;
			}
			synchronized (GenerationContext.class) {
				cached = vanillaWorldgenRegistries;
				if (cached != null) {
					return cached;
				}

				LayeredRegistryAccess<RegistryLayer> layers = RegistryLayer.createRegistryAccess();
				RegistryAccess.Frozen loadingAccess =
						layers.getAccessForLoading(RegistryLayer.WORLD);
				MultiPackResourceManager resources =
						new MultiPackResourceManager(
								PackType.SERVER_DATA,
								List.of(client.getVanillaPackResources().fullResources()));
				try {
					RegistryAccess.Frozen worldRegistries =
							RegistryDataLoader.load(
											resources,
											loadingAccess.listRegistries().toList(),
											RegistryDataLoader.WORLD_REGISTRIES,
											Runnable::run)
									.join();
					cached =
							layers.replaceFrom(RegistryLayer.WORLD, worldRegistries)
									.compositeAccess();
					vanillaDataResources = resources;
					vanillaWorldgenRegistries = cached;
					return cached;
				} catch (Throwable throwable) {
					resources.close();
					throw throwable;
				}
			}
		}

		private void prepareFull(int targetX, int targetZ) {
			for (int z = targetZ - 1; z <= targetZ + 1; z++) {
				for (int x = targetX - 1; x <= targetX + 1; x++) {
					prepareFeatureDependenciesOnce(x, z);
				}
			}
		}

		private GeneratedChunk generatePreparedFull(int targetX, int targetZ) {
			for (int z = targetZ - 1; z <= targetZ + 1; z++) {
				for (int x = targetX - 1; x <= targetX + 1; x++) {
					ensureFeatures(x, z);
				}
			}
			ProtoChunk chunk = chunks.get(ChunkPos.pack(targetX, targetZ));
			chunk.setPersistedStatus(ChunkStatus.FULL);
			return new GeneratedChunk(chunk);
		}

		private ProtoChunk ensureStructureStarts(int x, int z) {
			long key = ChunkPos.pack(x, z);
			return ensure(
					structureStarts,
					key,
					() -> {
						ProtoChunk chunk = chunk(x, z);
						if (nativeContext != null) {
							ChunkStep step =
									ChunkPyramid.GENERATION_PYRAMID.getStepTo(
											ChunkStatus.STRUCTURE_STARTS);
							ChunkStatusTasks.generateStructureStarts(
											nativeContext, step, nativeCache(x, z, step), chunk)
									.join();
						} else {
							WorldgenSeedContext.run(
									seed,
									() ->
											generator.createStructures(
													registries,
													structureState,
													structures,
													chunk,
													structureTemplates,
													dimension));
						}
						chunk.setPersistedStatus(ChunkStatus.STRUCTURE_STARTS);
						return chunk;
					});
		}

		private ProtoChunk ensureBiomes(int x, int z) {
			long key = ChunkPos.pack(x, z);
			return ensure(
					biomes,
					key,
					() -> {
						ProtoChunk chunk = ensureStructureReferences(x, z);
						if (nativeContext != null) {
							ChunkStep step =
									ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.BIOMES);
							ChunkStatusTasks.generateBiomes(
											nativeContext, step, nativeCache(x, z, step), chunk)
									.join();
						} else {
							generator
									.createBiomes(randomState, Blender.empty(), structures, chunk)
									.join();
						}
						chunk.setPersistedStatus(ChunkStatus.BIOMES);
						return chunk;
					});
		}

		private ProtoChunk ensureStructureReferences(int x, int z) {
			long key = ChunkPos.pack(x, z);
			return ensure(
					structureReferences,
					key,
					() -> {
						ChunkStep step =
								ChunkPyramid.GENERATION_PYRAMID.getStepTo(
										ChunkStatus.STRUCTURE_REFERENCES);
						int radius = step.directDependencies().size() - 1;
						for (int startZ = z - radius; startZ <= z + radius; startZ++) {
							for (int startX = x - radius; startX <= x + radius; startX++) {
								ensureStructureStarts(startX, startZ);
							}
						}
						ProtoChunk chunk = chunks.get(key);
						if (nativeContext != null) {
							ChunkStatusTasks.generateStructureReferences(
											nativeContext, step, nativeCache(x, z, step), chunk)
									.join();
						} else {
							WorldgenSeedContext.run(
									seed,
									() -> generator.createReferences(world, structures, chunk));
						}
						chunk.setPersistedStatus(ChunkStatus.STRUCTURE_REFERENCES);
						return chunk;
					});
		}

		private ProtoChunk ensureTerrain(int x, int z) {
			long key = ChunkPos.pack(x, z);
			return ensure(
					terrain,
					key,
					() -> {
						for (int biomeZ = z - 1; biomeZ <= z + 1; biomeZ++) {
							for (int biomeX = x - 1; biomeX <= x + 1; biomeX++) {
								ensureBiomes(biomeX, biomeZ);
							}
						}
						ProtoChunk chunk = chunks.get(key);
						if (nativeContext != null) {
							ChunkStep step =
									ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.TERRAIN);
							ChunkStatusTasks.buildTerrain(
											nativeContext, step, nativeCache(x, z, step), chunk)
									.join();
						} else {
							generator
									.buildTerrain(
											chunk,
											Blender.empty(),
											randomState,
											structures,
											new BiomeManager(
													world, BiomeManager.obfuscateSeed(seed)),
											null,
											generator.getBiomeSource().possibleBiomes())
									.join();
						}
						Heightmap.primeHeightmaps(
								chunk,
								EnumSet.of(
										Heightmap.Types.MOTION_BLOCKING,
										Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
										Heightmap.Types.OCEAN_FLOOR,
										Heightmap.Types.WORLD_SURFACE));
						chunk.setPersistedStatus(ChunkStatus.TERRAIN);
						return chunk;
					});
		}

		private ProtoChunk ensureFeatures(int x, int z) {
			long key = ChunkPos.pack(x, z);
			return ensure(
					features,
					key,
					() -> {
						prepareFeatureDependenciesOnce(x, z);
						List<ReentrantLock> locks = lockArea(x, z, 1);
						try {
							ProtoChunk chunk = chunks.get(key);
							if (nativeContext != null) {
								List<ChunkSnapshot> snapshots = snapshotArea(x, z, 1);
								try {
									ChunkStep step =
											ChunkPyramid.GENERATION_PYRAMID.getStepTo(
													ChunkStatus.FEATURES);
									ChunkStatusTasks.generateFeatures(
													nativeContext,
													step,
													nativeCache(x, z, step),
													chunk)
											.join();
								} catch (RuntimeException | Error throwable) {
									for (ChunkSnapshot snapshot : snapshots) {
										try {
											snapshot.restore();
										} catch (RuntimeException | Error rollbackFailure) {
											throwable.addSuppressed(rollbackFailure);
										}
									}
									throw throwable;
								}
							} else {
								ChunkTransaction transaction = localWorld.beginTransaction();
								try {
									ProtoChunk workingChunk = transaction.shadow(chunk);
									localWorld.runInGenerationRegion(
											x,
											z,
											() ->
													WorldgenSeedContext.run(
															seed,
															structureTemplates,
															generator,
															() ->
																	generator.applyBiomeDecoration(
																			world,
																			workingChunk,
																			structures)));
									transaction.commit();
								} finally {
									localWorld.endTransaction(transaction);
								}
							}
							chunk.setPersistedStatus(ChunkStatus.FEATURES);
							return chunk;
						} finally {
							unlock(locks);
						}
					});
		}

		private void prepareFeatureDependencies(int x, int z) {
			for (int neighborZ = z - 1; neighborZ <= z + 1; neighborZ++) {
				for (int neighborX = x - 1; neighborX <= x + 1; neighborX++) {
					ensureTerrain(neighborX, neighborZ);
				}
			}
			int dependencyRadius =
					ChunkPyramid.GENERATION_PYRAMID
									.getStepTo(ChunkStatus.FEATURES)
									.directDependencies()
									.size()
							- 1;
			for (int startZ = z - dependencyRadius; startZ <= z + dependencyRadius; startZ++) {
				for (int startX = x - dependencyRadius; startX <= x + dependencyRadius; startX++) {
					ensureStructureStarts(startX, startZ);
				}
			}
		}

		private void prepareFeatureDependenciesOnce(int x, int z) {
			long key = ChunkPos.pack(x, z);
			CompletableFuture<Void> existing = featurePreparations.get(key);
			if (existing != null) {
				existing.join();
				return;
			}

			CompletableFuture<Void> created = new CompletableFuture<>();
			existing = featurePreparations.putIfAbsent(key, created);
			if (existing != null) {
				existing.join();
				return;
			}
			try {
				prepareFeatureDependencies(x, z);
				created.complete(null);
			} catch (Throwable throwable) {
				featurePreparations.remove(key, created);
				created.completeExceptionally(throwable);
				throw throwable;
			}
		}

		private StaticCache2D<GenerationChunkHolder> nativeCache(
				int centerX, int centerZ, ChunkStep step) {
			int radius = step.directDependencies().size() - 1;
			return StaticCache2D.create(
					centerX,
					centerZ,
					radius,
					(x, z) -> new VirtualGenerationChunkHolder(chunks.get(ChunkPos.pack(x, z))));
		}

		private LevelLightEngine light(int x, int z) {
			LevelLightEngine lightEngine =
					new LevelLightEngine(
							new VirtualLightChunkGetter(this),
							true,
							sourceLevel.dimensionType().hasSkyLight());
			for (int chunkZ = z - 1; chunkZ <= z + 1; chunkZ++) {
				for (int chunkX = x - 1; chunkX <= x + 1; chunkX++) {
					ProtoChunk chunk = ensureFeatures(chunkX, chunkZ);
					chunk.initializeLightSources();
					chunk.setLightEngine(lightEngine);
					LevelChunkSection[] sections = chunk.getSections();
					for (int index = 0; index < sections.length; index++) {
						lightEngine.updateSectionStatus(
								SectionPos.of(chunk.getPos(), chunk.getMinSectionY() + index),
								sections[index].hasOnlyAir());
					}
					lightEngine.setLightEnabled(chunk.getPos(), true);
					lightEngine.propagateLightSources(chunk.getPos());
				}
			}
			while (lightEngine.hasLightWork()) {
				lightEngine.runLightUpdates();
			}
			chunks.get(ChunkPos.pack(x, z)).setLightCorrect(true);
			return lightEngine;
		}

		private ProtoChunk chunk(int x, int z) {
			return chunks.computeIfAbsent(
					ChunkPos.pack(x, z),
					ignored ->
							new ProtoChunk(
									new ChunkPos(x, z),
									UpgradeData.EMPTY,
									height,
									containers,
									null));
		}

		private int cacheDependencyMargin() {
			int structureReferenceRadius =
					ChunkPyramid.GENERATION_PYRAMID
									.getStepTo(ChunkStatus.STRUCTURE_REFERENCES)
									.directDependencies()
									.size()
							- 1;
			return structureReferenceRadius + 4;
		}

		private void evictInnerRings(int centerX, int centerZ, int firstRadius, int lastRadius) {
			for (int distance = Math.max(0, firstRadius); distance <= lastRadius; distance++) {
				if (distance == 0) {
					evict(ChunkPos.pack(centerX, centerZ));
					continue;
				}
				int minX = centerX - distance;
				int maxX = centerX + distance;
				int minZ = centerZ - distance;
				int maxZ = centerZ + distance;
				for (int x = minX; x <= maxX; x++) {
					evict(ChunkPos.pack(x, minZ));
					evict(ChunkPos.pack(x, maxZ));
				}
				for (int z = minZ + 1; z < maxZ; z++) {
					evict(ChunkPos.pack(minX, z));
					evict(ChunkPos.pack(maxX, z));
				}
			}
		}

		private void evict(long key) {
			features.remove(key);
			featurePreparations.remove(key);
			terrain.remove(key);
			biomes.remove(key);
			structureReferences.remove(key);
			structureStarts.remove(key);
			chunks.remove(key);
		}

		private Set<Long> beginAccessTracking() {
			if (accessTracking.get() != null) {
				throw new IllegalStateException(
						"Nested generation access tracking is not supported");
			}
			Set<Long> accessed = new HashSet<>();
			accessTracking.set(accessed);
			return accessed;
		}

		private void endAccessTracking(Set<Long> expected) {
			if (accessTracking.get() != expected) {
				throw new IllegalStateException("Ending inactive generation access tracking");
			}
			accessTracking.remove();
		}

		private void recordAccess(long key) {
			Set<Long> accessed = accessTracking.get();
			if (accessed != null) {
				accessed.add(key);
			}
		}

		private void invalidateAccessed(Set<Long> accessed) {
			for (long key : accessed) {
				evict(key);
			}
		}

		private ProtoChunk ensure(
				ConcurrentHashMap<Long, CompletableFuture<ProtoChunk>> stages,
				long key,
				ChunkOperation operation) {
			if (cancelled) {
				throw new CancellationException("Voxygen session was retired");
			}
			recordAccess(key);
			CompletableFuture<ProtoChunk> existing = stages.get(key);
			if (existing != null) {
				return existing.join();
			}
			CompletableFuture<ProtoChunk> created = new CompletableFuture<>();
			existing = stages.putIfAbsent(key, created);
			if (existing != null) {
				return existing.join();
			}
			try {
				ProtoChunk result = operation.run();
				created.complete(result);
				return result;
			} catch (Throwable throwable) {
				stages.remove(key, created);
				created.completeExceptionally(throwable);
				throw throwable;
			}
		}

		private List<ReentrantLock> lockArea(int centerX, int centerZ, int radius) {
			TreeMap<Integer, ReentrantLock> ordered = new TreeMap<>();
			for (int z = centerZ - radius; z <= centerZ + radius; z++) {
				for (int x = centerX - radius; x <= centerX + radius; x++) {
					int index =
							Math.floorMod(Long.hashCode(ChunkPos.pack(x, z)), writeLocks.length);
					ordered.put(index, writeLocks[index]);
				}
			}
			List<ReentrantLock> result = new ArrayList<>(ordered.values());
			for (ReentrantLock lock : result) {
				lock.lock();
			}
			return result;
		}

		private List<ChunkSnapshot> snapshotArea(int centerX, int centerZ, int radius) {
			List<ChunkSnapshot> snapshots = new ArrayList<>();
			for (int z = centerZ - radius; z <= centerZ + radius; z++) {
				for (int x = centerX - radius; x <= centerX + radius; x++) {
					ProtoChunk chunk = chunks.get(ChunkPos.pack(x, z));
					if (chunk != null) {
						snapshots.add(ChunkSnapshot.capture(chunk));
					}
				}
			}
			return snapshots;
		}

		private static void unlock(List<ReentrantLock> locks) {
			for (int index = locks.size() - 1; index >= 0; index--) {
				locks.get(index).unlock();
			}
		}

		private void cancel() {
			cancelled = true;
		}

		private synchronized void close() {
			if (closed) {
				return;
			}
			closed = true;
			if (storageAccess != null) {
				try {
					storageAccess.close();
				} catch (IOException exception) {
					LOGGER.warn("Cannot close Voxygen virtual world", exception);
				}
			}
		}

		private record ChunkSnapshot(
				ProtoChunk chunk,
				LevelChunkSection[] sections,
				Map<Heightmap.Types, long[]> heightmaps,
				Map<BlockPos, BlockEntity> blockEntities,
				Map<BlockPos, CompoundTag> pendingBlockEntities,
				ShortList[] postProcessing) {
			private static ChunkSnapshot capture(ProtoChunk chunk) {
				LevelChunkSection[] originalSections = chunk.getSections();
				LevelChunkSection[] sections = new LevelChunkSection[originalSections.length];
				for (int index = 0; index < originalSections.length; index++) {
					sections[index] = copySection(originalSections[index]);
				}

				Map<Heightmap.Types, long[]> heightmaps = new HashMap<>();
				for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {
					heightmaps.put(entry.getKey(), entry.getValue().getRawData().clone());
				}

				Map<BlockPos, BlockEntity> blockEntities = new HashMap<>(chunk.getBlockEntities());
				Map<BlockPos, CompoundTag> pendingBlockEntities = new HashMap<>();
				for (Map.Entry<BlockPos, CompoundTag> entry :
						chunk.getBlockEntityNbts().entrySet()) {
					pendingBlockEntities.put(entry.getKey(), entry.getValue().copy());
				}

				ShortList[] sourcePostProcessing = chunk.getPostProcessing();
				ShortList[] postProcessing = new ShortList[sourcePostProcessing.length];
				for (int index = 0; index < sourcePostProcessing.length; index++) {
					ShortList positions = sourcePostProcessing[index];
					postProcessing[index] =
							positions == null ? null : new ShortArrayList(positions);
				}
				return new ChunkSnapshot(
						chunk,
						sections,
						heightmaps,
						blockEntities,
						pendingBlockEntities,
						postProcessing);
			}

			private void restore() {
				System.arraycopy(sections, 0, chunk.getSections(), 0, sections.length);
				for (Map.Entry<Heightmap.Types, long[]> entry : heightmaps.entrySet()) {
					chunk.setHeightmap(entry.getKey(), entry.getValue().clone());
				}

				for (BlockPos pos : new ArrayList<>(chunk.getBlockEntitiesPos())) {
					chunk.removeBlockEntity(pos);
				}
				for (Map.Entry<BlockPos, BlockEntity> entry : blockEntities.entrySet()) {
					BlockEntity blockEntity = entry.getValue();
					blockEntity.setBlockState(chunk.getBlockState(entry.getKey()));
					chunk.setBlockEntity(blockEntity);
				}

				for (Map.Entry<BlockPos, CompoundTag> entry : pendingBlockEntities.entrySet()) {
					chunk.setBlockEntityNbt(entry.getValue().copy());
				}

				ShortList[] currentPostProcessing = chunk.getPostProcessing();
				for (int index = 0; index < currentPostProcessing.length; index++) {
					currentPostProcessing[index] =
							postProcessing[index] == null
									? null
									: new ShortArrayList(postProcessing[index]);
				}
			}
		}
	}

	@FunctionalInterface
	private interface ChunkOperation {
		ProtoChunk run();
	}

	private record GeneratedChunk(ProtoChunk chunk) {}

	private static final class ChunkTransaction {
		private final GenerationContext context;
		private final Map<Long, TransactionalProtoChunk> shadows = new HashMap<>();
		private boolean committed;

		private ChunkTransaction(GenerationContext context) {
			this.context = context;
		}

		private ProtoChunk shadow(ProtoChunk original) {
			if (original == null) {
				return null;
			}
			return shadows.computeIfAbsent(
					ChunkPos.pack(original.getPos().x(), original.getPos().z()),
					ignored -> new TransactionalProtoChunk(context, original));
		}

		private void commit() {
			if (committed) {
				throw new IllegalStateException("Chunk transaction was already committed");
			}
			for (TransactionalProtoChunk shadow : shadows.values()) {
				shadow.commit();
			}
			committed = true;
		}
	}

	private static final class TransactionalProtoChunk extends ProtoChunk {
		private final ProtoChunk original;
		private final boolean[] ownedSections;

		private TransactionalProtoChunk(GenerationContext context, ProtoChunk original) {
			super(original.getPos(), UpgradeData.EMPTY, context.height, context.containers, null);
			this.original = original;
			LevelChunkSection[] sourceSections = original.getSections();
			LevelChunkSection[] workingSections = super.getSections();
			System.arraycopy(sourceSections, 0, workingSections, 0, sourceSections.length);
			this.ownedSections = new boolean[workingSections.length];

			for (Map.Entry<Heightmap.Types, Heightmap> entry : original.getHeightmaps()) {
				setHeightmap(entry.getKey(), entry.getValue().getRawData().clone());
			}
			setAllStarts(new HashMap<>(original.getAllStarts()));
			setAllReferences(new HashMap<>(original.getAllReferences()));
			for (Map.Entry<BlockPos, BlockEntity> entry : original.getBlockEntities().entrySet()) {
				BlockPos position = entry.getKey();
				BlockEntity clone =
						BlockEntity.loadStatic(
								position,
								original.getBlockState(position),
								entry.getValue().saveWithFullMetadata(context.registries),
								context.registries);
				if (clone != null) {
					setBlockEntity(clone);
				}
			}
			for (Map.Entry<BlockPos, CompoundTag> entry :
					original.getBlockEntityNbts().entrySet()) {
				setBlockEntityNbt(entry.getValue().copy());
			}
			ShortList[] sourcePostProcessing = original.getPostProcessing();
			ShortList[] workingPostProcessing = getPostProcessing();
			for (int index = 0; index < sourcePostProcessing.length; index++) {
				ShortList positions = sourcePostProcessing[index];
				workingPostProcessing[index] =
						positions == null ? null : new ShortArrayList(positions);
			}
			setPersistedStatus(original.getPersistedStatus());
			setBelowZeroRetrogen(original.getBelowZeroRetrogen());
			setLightCorrect(original.isLightCorrect());
		}

		@Override
		public BlockState setBlockState(BlockPos position, BlockState state, int flags) {
			ownSection(getSectionIndex(position.getY()));
			return super.setBlockState(position, state, flags);
		}

		private void ownSection(int index) {
			if (index < 0 || index >= ownedSections.length || ownedSections[index]) {
				return;
			}
			super.getSections()[index] = copySection(super.getSections()[index]);
			ownedSections[index] = true;
		}

		private void commit() {
			LevelChunkSection[] sourceSections = super.getSections();
			LevelChunkSection[] targetSections = original.getSections();
			for (int index = 0; index < ownedSections.length; index++) {
				if (ownedSections[index]) {
					targetSections[index] = sourceSections[index];
				}
			}
			for (Map.Entry<Heightmap.Types, Heightmap> entry : getHeightmaps()) {
				original.setHeightmap(entry.getKey(), entry.getValue().getRawData().clone());
			}

			for (BlockPos position : new ArrayList<>(original.getBlockEntitiesPos())) {
				original.removeBlockEntity(position);
			}
			for (Map.Entry<BlockPos, BlockEntity> entry : getBlockEntities().entrySet()) {
				BlockEntity blockEntity = entry.getValue();
				blockEntity.setBlockState(original.getBlockState(entry.getKey()));
				original.setBlockEntity(blockEntity);
			}
			for (Map.Entry<BlockPos, CompoundTag> entry : getBlockEntityNbts().entrySet()) {
				original.setBlockEntityNbt(entry.getValue().copy());
			}

			ShortList[] sourcePostProcessing = getPostProcessing();
			ShortList[] targetPostProcessing = original.getPostProcessing();
			for (int index = 0; index < sourcePostProcessing.length; index++) {
				ShortList positions = sourcePostProcessing[index];
				targetPostProcessing[index] =
						positions == null ? null : new ShortArrayList(positions);
			}
		}
	}

	private static LevelChunkSection copySection(LevelChunkSection source) {
		LevelChunkSection copy = source.copy();
		copy.recalcBlockCounts();
		return copy;
	}

	private static final class VirtualGenerationChunkHolder extends GenerationChunkHolder {
		private final ProtoChunk chunk;

		private VirtualGenerationChunkHolder(ProtoChunk chunk) {
			super(chunk.getPos());
			this.chunk = chunk;
		}

		@Override
		protected void addSaveDependency(CompletableFuture<?> dependency) {}

		@Override
		public int getTicketLevel() {
			return 0;
		}

		@Override
		public int getQueueLevel() {
			return 0;
		}

		@Override
		public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status) {
			return chunk;
		}

		@Override
		public ChunkAccess getChunkIfPresent(ChunkStatus status) {
			return chunk;
		}

		@Override
		public ChunkAccess getLatestChunk() {
			return chunk;
		}

		@Override
		public ChunkStatus getPersistedStatus() {
			return chunk.getPersistedStatus();
		}
	}

	private static final class VirtualLightChunkGetter implements LightChunkGetter {
		private final GenerationContext context;

		private VirtualLightChunkGetter(GenerationContext context) {
			this.context = context;
		}

		@Override
		public LightChunk getChunkForLighting(int x, int z) {
			long key = ChunkPos.pack(x, z);
			context.recordAccess(key);
			return context.chunks.get(key);
		}

		@Override
		public net.minecraft.world.level.BlockGetter getLevel() {
			return context.world;
		}
	}

	private static final class LocalWorld implements InvocationHandler {
		private static final Identifier WORLDGEN_REGION_RANDOM =
				Identifier.withDefaultNamespace("worldgen_region_random");
		private final GenerationContext context;
		private final Map<Long, ProtoChunk> chunks;
		private final AtomicLong subTicks = new AtomicLong();
		private final LocalChunkSource chunkSource;
		private final ThreadLocal<RandomSource> generationRegionRandom = new ThreadLocal<>();
		private final ThreadLocal<ChunkTransaction> transaction = new ThreadLocal<>();
		private WorldGenLevel proxy;

		private LocalWorld(GenerationContext context, Map<Long, ProtoChunk> chunks) {
			this.context = context;
			this.chunks = chunks;
			this.chunkSource = new LocalChunkSource(chunks, this);
		}

		private WorldGenLevel proxy() {
			if (proxy == null) {
				proxy =
						(WorldGenLevel)
								Proxy.newProxyInstance(
										WorldGenLevel.class.getClassLoader(),
										new Class<?>[] {WorldGenLevel.class},
										this);
			}
			return proxy;
		}

		private ChunkTransaction beginTransaction() {
			if (transaction.get() != null) {
				throw new IllegalStateException("Nested chunk transactions are not supported");
			}
			ChunkTransaction created = new ChunkTransaction(context);
			transaction.set(created);
			return created;
		}

		private void endTransaction(ChunkTransaction expected) {
			if (transaction.get() != expected) {
				throw new IllegalStateException("Ending an inactive chunk transaction");
			}
			transaction.remove();
		}

		private void runInGenerationRegion(int chunkX, int chunkZ, Runnable action) {
			RandomSource previous = generationRegionRandom.get();
			RandomSource random =
					context.randomState
							.getOrCreateRandomFactory(WORLDGEN_REGION_RANDOM)
							.at(new ChunkPos(chunkX, chunkZ).getWorldPosition());
			generationRegionRandom.set(random);
			try {
				action.run();
			} finally {
				if (previous == null) {
					generationRegionRandom.remove();
				} else {
					generationRegionRandom.set(previous);
				}
			}
		}

		@Override
		public Object invoke(Object ignoredProxy, Method method, Object[] args) throws Throwable {
			String name = method.getName();
			if (method.getDeclaringClass() == Object.class) {
				return switch (name) {
					case "toString" -> "VoxygenLocalWorld";
					case "hashCode" -> System.identityHashCode(this);
					case "equals" -> ignoredProxy == args[0];
					default -> null;
				};
			}

			return switch (name) {
				case "getSeed" -> context.seed;
				case "registryAccess" -> context.registries;
				case "enabledFeatures" -> context.sourceLevel.enabledFeatures();
				case "dimensionType" -> context.sourceLevel.dimensionType();
				case "environmentAttributes" -> context.sourceLevel.environmentAttributes();
				case "getLevelData" -> context.sourceLevel.getLevelData();
				case "getChunkSource" -> chunkSource;
				case "getMinY" -> context.height.getMinY();
				case "getHeight" -> getHeight(method, args);
				case "getSeaLevel" -> context.generator.getSeaLevel();
				case "getChunk" -> getChunk(args);
				case "hasChunk" -> chunks.containsKey(ChunkPos.pack((int) args[0], (int) args[1]));
				case "getBlockState" -> getBlockState((BlockPos) args[0]);
				case "getFluidState" -> getBlockState((BlockPos) args[0]).getFluidState();
				case "getBlockEntity" -> getBlockEntity(method, (BlockPos) args[0]);
				case "setBlock" -> setBlock(args);
				case "removeBlock", "destroyBlock" -> setAir((BlockPos) args[0]);
				case "isStateAtPosition" ->
						((java.util.function.Predicate<BlockState>) args[1])
								.test(getBlockState((BlockPos) args[0]));
				case "isFluidAtPosition" ->
						((java.util.function.Predicate<Object>) args[1])
								.test(getBlockState((BlockPos) args[0]).getFluidState());
				case "getUncachedNoiseBiome" -> noiseBiome(args);
				case "getBiomeManager" ->
						new BiomeManager(proxy(), BiomeManager.obfuscateSeed(context.seed));
				case "getRandom" -> {
					RandomSource random = generationRegionRandom.get();
					yield random == null ? RandomSource.create(context.seed) : random;
				}
				case "getCurrentDifficultyAt" ->
						new DifficultyInstance(
								context.sourceLevel.getDifficulty(),
								context.sourceLevel.getGameTime(),
								0L,
								0.0F);
				case "nextSubTickCount" -> subTicks.getAndIncrement();
				case "getBlockTicks", "getFluidTicks" -> BlackholeTickAccess.emptyLevelList();
				case "getEntities", "players" -> Collections.emptyList();
				case "isClientSide" -> false;
				case "getSkyDarken" -> 0;
				case "getBrightness",
						"getRawBrightness",
						"getMaxLocalRawBrightness",
						"getEffectiveSkyBrightness" ->
						15;
				case "canSeeSky" -> context.sourceLevel.dimensionType().hasSkyLight();
				case "getLightEngine" -> null;
				case "getServer", "getLevel" -> null;
				case "addFreshEntity" -> false;
				case "playSound",
						"addParticle",
						"levelEvent",
						"gameEvent",
						"setCurrentlyGenerating" ->
						null;
				default -> invokeDefaultOrReturnZero(ignoredProxy, method, args);
			};
		}

		private Object getHeight(Method method, Object[] args) {
			if (method.getParameterCount() == 0) {
				return context.height.getHeight();
			}
			net.minecraft.world.level.levelgen.Heightmap.Types type =
					(net.minecraft.world.level.levelgen.Heightmap.Types) args[0];
			int x;
			int z;
			if (args.length == 2 && args[1] instanceof BlockPos pos) {
				x = pos.getX();
				z = pos.getZ();
			} else if (args.length == 3
					&& args[1] instanceof Integer xCoordinate
					&& args[2] instanceof Integer zCoordinate) {
				x = xCoordinate;
				z = zCoordinate;
			} else {
				throw new IllegalArgumentException(
						"Unsupported getHeight overload: " + method.toGenericString());
			}
			ChunkAccess chunk = chunkAt(x >> 4, z >> 4);
			return chunk == null
					? context.height.getMinY()
					: chunk.getHeight(type, x & 15, z & 15) + 1;
		}

		private Object getChunk(Object[] args) {
			if (args[0] instanceof BlockPos pos) {
				return chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			}
			return chunkAt((int) args[0], (int) args[1]);
		}

		private ProtoChunk chunkAt(int x, int z) {
			long key = ChunkPos.pack(x, z);
			context.recordAccess(key);
			ProtoChunk chunk = chunks.get(key);
			ChunkTransaction active = transaction.get();
			return active == null ? chunk : active.shadow(chunk);
		}

		private BlockState getBlockState(BlockPos pos) {
			ProtoChunk chunk = chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			return chunk == null || !context.height.isInsideBuildHeight(pos)
					? Blocks.VOID_AIR.defaultBlockState()
					: chunk.getBlockState(pos);
		}

		private Object getBlockEntity(Method method, BlockPos pos) {
			ProtoChunk chunk = chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			BlockEntity blockEntity = chunk == null ? null : chunk.getBlockEntity(pos);
			if (method.getReturnType() == java.util.Optional.class) {
				return java.util.Optional.ofNullable(blockEntity);
			}
			return blockEntity;
		}

		private boolean setBlock(Object[] args) {
			BlockPos pos = (BlockPos) args[0];
			ProtoChunk chunk = chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			if (chunk == null || !context.height.isInsideBuildHeight(pos)) {
				return false;
			}
			int flags = args.length > 2 ? (int) args[2] : 0;
			BlockState state = (BlockState) args[1];
			BlockEntity current = chunk.getBlockEntity(pos);
			chunk.setBlockState(pos, state, flags);
			if (state.getBlock() instanceof EntityBlock entityBlock) {
				if (current == null || !current.getType().isValid(state)) {
					if (current != null) {
						chunk.removeBlockEntity(pos);
					}
					BlockEntity replacement = entityBlock.newBlockEntity(pos, state);
					if (replacement != null) {
						chunk.setBlockEntity(replacement);
					}
				}
			} else if (current != null) {
				chunk.removeBlockEntity(pos);
			}
			return true;
		}

		private boolean setAir(BlockPos pos) {
			ProtoChunk chunk = chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			if (chunk == null || !context.height.isInsideBuildHeight(pos)) {
				return false;
			}
			chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), 0);
			chunk.removeBlockEntity(pos);
			return true;
		}

		private Holder<Biome> noiseBiome(Object[] args) {
			int quartX = (int) args[0];
			int quartY = (int) args[1];
			int quartZ = (int) args[2];
			ProtoChunk chunk = chunkAt(Math.floorDiv(quartX, 4), Math.floorDiv(quartZ, 4));
			if (chunk != null) {
				return chunk.getNoiseBiome(quartX, quartY, quartZ);
			}
			return chunks.values().iterator().next().getNoiseBiome(quartX, quartY, quartZ);
		}

		private Object invokeDefaultOrReturnZero(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(
						proxy, method, args == null ? new Object[0] : args);
			}
			Class<?> type = method.getReturnType();
			if (!type.isPrimitive()) {
				return null;
			}
			if (type == boolean.class) return false;
			if (type == char.class) return '\0';
			if (type == byte.class) return (byte) 0;
			if (type == short.class) return (short) 0;
			if (type == int.class) return 0;
			if (type == long.class) return 0L;
			if (type == float.class) return 0.0F;
			if (type == double.class) return 0.0D;
			return null;
		}
	}

	private static final class LocalChunkSource extends ChunkSource {
		private final Map<Long, ProtoChunk> chunks;
		private final LocalWorld world;

		private LocalChunkSource(Map<Long, ProtoChunk> chunks, LocalWorld world) {
			this.chunks = chunks;
			this.world = world;
		}

		@Override
		public net.minecraft.world.level.BlockGetter getLevel() {
			return world.proxy();
		}

		@Override
		public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean create) {
			return world.chunkAt(x, z);
		}

		@Override
		public void tick(java.util.function.BooleanSupplier hasTimeLeft, boolean tickChunks) {}

		@Override
		public String gatherStats() {
			return "Voxygen local chunks: " + chunks.size();
		}

		@Override
		public int getLoadedChunksCount() {
			return chunks.size();
		}

		@Override
		public net.minecraft.world.level.lighting.LevelLightEngine getLightEngine() {
			return null;
		}
	}

	private static final class WorkerThreadFactory implements ThreadFactory {
		private final AtomicInteger nextId = new AtomicInteger();

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, "Voxygen-Worker-" + nextId.incrementAndGet());
			thread.setDaemon(true);
			thread.setUncaughtExceptionHandler(
					(ignored, throwable) ->
							LOGGER.warn(
									"Uncaught Voxygen worker failure, reason: {}",
									failureReason(throwable),
									throwable));
			return thread;
		}
	}

	private static final class WindowThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, "Voxygen-Window-Updater");
			thread.setDaemon(true);
			thread.setUncaughtExceptionHandler(
					(ignored, throwable) ->
							LOGGER.warn(
									"Uncaught Voxygen window-update failure, reason: {}",
									failureReason(throwable),
									throwable));
			return thread;
		}
	}
}

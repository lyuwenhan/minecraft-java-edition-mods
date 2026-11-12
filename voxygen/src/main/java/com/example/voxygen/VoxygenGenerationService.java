package com.example.voxygen;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
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
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.PalettedContainer;
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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
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
	private static final int CHUNK_STATE_FINISHED = 1;
	private static final int CHUNK_STATE_QUEUED = 1 << 1;
	private static final int CHUNK_STATE_ACTIVE = 1 << 2;
	private static final int CHUNK_STATE_FAILED = 1 << 3;
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
		long priorityCenter = freecamPriorityCenter(client, config.generateForFreecam);
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
							priorityCenter,
							config.threads);
			session = current;
			state =
					Component.translatable(
							"command.voxygen.state.preparing", dimension.identifier().toString());
		}

		current.setWorkerCount(config.threads);
		current.requestWindowUpdate(centerX, centerZ, radius, priorityCenter);
		current.pump();
	}

	private static long freecamPriorityCenter(Minecraft client, boolean enabled) {
		if (!enabled || !client.gameRenderer.mainCamera().isInitialized()) {
			return NO_ACTIVE_REGION;
		}
		var position = client.gameRenderer.mainCamera().position();
		int chunkX = Math.floorDiv((int) Math.floor(position.x()), 16);
		int chunkZ = Math.floorDiv((int) Math.floor(position.z()), 16);
		return ChunkPos.pack(
				Math.floorDiv(chunkX, REGION_CHUNK_SIZE), Math.floorDiv(chunkZ, REGION_CHUNK_SIZE));
	}

	static void invalidate() {
		retire(Component.translatable("command.voxygen.state.restarting"));
	}

	static Component describeState() {
		Session current = session;
		if (current == null) {
			return state;
		}
		int completed = current.countState(CHUNK_STATE_FINISHED);
		int total = current.totalChunks();
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
				current.countState(CHUNK_STATE_FAILED));
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
		int total = current.totalChunks();
		if (total == 0) {
			return null;
		}
		int completed = Math.min(current.countState(CHUNK_STATE_FINISHED), total);
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
		private long requestedPriorityCenter;
		private long requestedWindowVersion;
		private long nextWindowUpdateNanos;
		private boolean windowTaskScheduled;
		private boolean windowTaskRunning;
		private ScheduledFuture<?> windowFuture;
		private volatile boolean windowReady;
		private final Set<Long> regionCoverage = ConcurrentHashMap.newKeySet();
		private final Set<Long> priorityRegionCoverage = new HashSet<>();
		private final ChunkStateTable chunkStates = new ChunkStateTable();
		private final Set<Long> queuedRegions = ConcurrentHashMap.newKeySet();
		private final ConcurrentLinkedDeque<Long> pending = new ConcurrentLinkedDeque<>();
		private final ConcurrentLinkedDeque<Long> pendingRegions = new ConcurrentLinkedDeque<>();
		private final ConcurrentLinkedDeque<FailedAttempt> failedAttempts =
				new ConcurrentLinkedDeque<>();
		private final Map<Long, Integer> failureCounts = new ConcurrentHashMap<>();
		private final Set<Long> forcedRetries = ConcurrentHashMap.newKeySet();
		private final AtomicInteger inFlight = new AtomicInteger();
		private volatile long activeRegion = NO_ACTIVE_REGION;
		private final AtomicInteger generatedChunks = new AtomicInteger();
		private final AtomicInteger insertedChunks = new AtomicInteger();
		private final AtomicInteger insertedSections = new AtomicInteger();
		private final AtomicInteger skippedVoxy = new AtomicInteger();
		private final AtomicInteger emptyChunks = new AtomicInteger();
		private int evictedInnerRegionRadius = -1;
		private boolean generationCacheCleared;
		private long priorityCenter;
		private long cachedPriorityCenter = NO_ACTIVE_REGION;
		private boolean processingPriorityRegions;
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
				long priorityCenter,
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
			this.priorityCenter = priorityCenter;
			this.requestedPriorityCenter = priorityCenter;
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

		private void requestWindowUpdate(
				int newCenterX, int newCenterZ, int newRadius, long newPriorityCenter) {
			synchronized (windowLock) {
				if (retiring) {
					return;
				}
				if (requestedCenterX == newCenterX
						&& requestedCenterZ == newCenterZ
						&& requestedRadius == newRadius
						&& requestedPriorityCenter == newPriorityCenter) {
					if (!windowTaskScheduled
							&& (!windowReady
									|| centerX != newCenterX
									|| centerZ != newCenterZ
									|| radius != newRadius
									|| priorityCenter != newPriorityCenter)) {
						long delay = Math.max(0L, nextWindowUpdateNanos - System.nanoTime());
						scheduleWindowUpdate(delay);
					}
					return;
				}
				requestedCenterX = newCenterX;
				requestedCenterZ = newCenterZ;
				requestedRadius = newRadius;
				requestedPriorityCenter = newPriorityCenter;
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
			long newPriorityCenter;
			long version;
			synchronized (windowLock) {
				if (retiring) {
					finishWindowTask();
					return;
				}
				newCenterX = requestedCenterX;
				newCenterZ = requestedCenterZ;
				newRadius = requestedRadius;
				newPriorityCenter = requestedPriorityCenter;
				version = requestedWindowVersion;
			}

			List<Long> orderedPlayerRegions =
					squareInChebyshevOrder(newCenterX, newCenterZ, newRadius);
			List<Long> orderedPriorityRegions;
			if (newPriorityCenter == NO_ACTIVE_REGION) {
				orderedPriorityRegions = List.of();
			} else {
				orderedPriorityRegions =
						squareInChebyshevOrder(
								ChunkPos.getX(newPriorityCenter),
								ChunkPos.getZ(newPriorityCenter),
								1);
			}
			Set<Long> nextPriorityRegionCoverage = new HashSet<>(orderedPriorityRegions);
			List<Long> orderedRegions =
					new ArrayList<>(orderedPriorityRegions.size() + orderedPlayerRegions.size());
			orderedRegions.addAll(orderedPriorityRegions);
			for (long region : orderedPlayerRegions) {
				if (!nextPriorityRegionCoverage.contains(region)) {
					orderedRegions.add(region);
				}
			}
			Set<Long> nextRegionCoverage = new HashSet<>(orderedRegions);
			synchronized (windowLock) {
				if (retiring || version != requestedWindowVersion) {
					completeWindowUpdate(version);
					return;
				}
			}

			synchronized (coverageLock) {
				regionCoverage.clear();
				regionCoverage.addAll(nextRegionCoverage);
				chunkStates.retainRegions(nextRegionCoverage);
				chunkStates.ensureRegions(nextRegionCoverage);
				priorityRegionCoverage.clear();
				priorityRegionCoverage.addAll(nextPriorityRegionCoverage);
				priorityCenter = newPriorityCenter;
				failureCounts.keySet().removeIf(key -> !isCovered(key));
				forcedRetries.removeIf(key -> !isCovered(key));
				failedAttempts.removeIf(failure -> !isCovered(failure.target()));
				pending.clear();
				chunkStates.clearState(CHUNK_STATE_QUEUED);
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
				if (centerX != newCenterX || centerZ != newCenterZ) {
					evictedInnerRegionRadius = -1;
				}
				centerX = newCenterX;
				centerZ = newCenterZ;
				radius = newRadius;
				generationCacheCleared = false;
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
			int traversalCenterX = centerX;
			int traversalCenterZ = centerZ;
			if (priorityCenter != NO_ACTIVE_REGION && priorityRegionCoverage.contains(region)) {
				traversalCenterX = ChunkPos.getX(priorityCenter);
				traversalCenterZ = ChunkPos.getZ(priorityCenter);
			}
			int deltaX = ChunkPos.getX(region) - traversalCenterX;
			int deltaZ = ChunkPos.getZ(region) - traversalCenterZ;
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
			if (isCovered(packed)
					&& !chunkStates.contains(packed, CHUNK_STATE_FINISHED)
					&& !chunkStates.contains(packed, CHUNK_STATE_ACTIVE)
					&& chunkStates.add(packed, CHUNK_STATE_QUEUED)) {
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
				if (activeRegion != NO_ACTIVE_REGION && context != null) {
					context.archiveGenerationUnit(activeRegion);
				}
				activeRegion = NO_ACTIVE_REGION;
				Long nextRegion;
				while ((nextRegion = pendingRegions.pollFirst()) != null) {
					queuedRegions.remove(nextRegion);
					if (!regionCoverage.contains(nextRegion)) {
						continue;
					}
					prepareCacheForRegion(nextRegion);
					activeRegion = nextRegion;
					generationCacheCleared = false;
					enqueueRegionChunks(nextRegion);
					if (!pending.isEmpty()) {
						return true;
					}
					activeRegion = NO_ACTIVE_REGION;
				}
				if (!generationCacheCleared && context != null) {
					context.clearGenerationCache();
					generationCacheCleared = true;
				}
				processingPriorityRegions = false;
				cachedPriorityCenter = NO_ACTIVE_REGION;
				evictedInnerRegionRadius = -1;
				return false;
			}
		}

		private void prepareCacheForRegion(long nextRegion) {
			GenerationContext currentContext = context;
			if (currentContext == null || retiring) {
				return;
			}
			currentContext.compactCompletedStageCaches();
			boolean priority = priorityRegionCoverage.contains(nextRegion);
			if (priority) {
				if (!processingPriorityRegions || cachedPriorityCenter != priorityCenter) {
					currentContext.clearGenerationCache();
					processingPriorityRegions = true;
					cachedPriorityCenter = priorityCenter;
					evictedInnerRegionRadius = -1;
				}
				return;
			}
			if (processingPriorityRegions) {
				currentContext.clearGenerationCache();
				processingPriorityRegions = false;
				cachedPriorityCenter = NO_ACTIVE_REGION;
				evictedInnerRegionRadius = -1;
			}
			evictCompletedInnerRegionRings(nextRegion);
		}

		private void evictCompletedInnerRegionRings(long nextRegion) {
			GenerationContext currentContext = context;
			if (currentContext == null || retiring) {
				return;
			}
			int activeRing =
					Math.max(
							Math.abs(ChunkPos.getX(nextRegion) - centerX),
							Math.abs(ChunkPos.getZ(nextRegion) - centerZ));
			int lastSafeRing = activeRing - 2;
			if (lastSafeRing <= evictedInnerRegionRadius) {
				return;
			}
			currentContext.evictRegionRings(
					centerX, centerZ, evictedInnerRegionRadius + 1, lastSafeRing);
			evictedInnerRegionRadius = lastSafeRing;
		}

		private boolean recoverFailedAttempts() {
			Set<Long> accessed = new HashSet<>();
			Set<Long> retryTargets = new HashSet<>();
			FailedAttempt failure;
			while ((failure = failedAttempts.pollFirst()) != null) {
				if (failure.invalidateAccessed()) {
					accessed.addAll(failure.accessedChunks());
				}
				int failureCount = failureCounts.merge(failure.target(), 1, Integer::sum);
				if (failureCount <= 2) {
					retryTargets.add(failure.target());
					forcedRetries.add(failure.target());
				} else {
					forcedRetries.remove(failure.target());
					chunkStates.add(failure.target(), CHUNK_STATE_FAILED);
					chunkStates.add(failure.target(), CHUNK_STATE_FINISHED);
				}
			}
			if (accessed.isEmpty() && retryTargets.isEmpty()) {
				return false;
			}

			if (!accessed.isEmpty()) {
				context.invalidateStructurePreparation(activeRegion);
				context.invalidateAccessed(accessed);
			}
			for (long packed : accessed) {
				if (isInRegion(packed, activeRegion)
						&& isCovered(packed)
						&& !chunkStates.contains(packed, CHUNK_STATE_FAILED)) {
					chunkStates.remove(packed, CHUNK_STATE_FINISHED);
				}
			}
			for (long packed : retryTargets) {
				chunkStates.remove(packed, CHUNK_STATE_FINISHED);
			}
			enqueueRegionChunks(activeRegion);
			return !pending.isEmpty();
		}

		private static boolean isInRegion(long chunk, long region) {
			return Math.floorDiv(ChunkPos.getX(chunk), REGION_CHUNK_SIZE) == ChunkPos.getX(region)
					&& Math.floorDiv(ChunkPos.getZ(chunk), REGION_CHUNK_SIZE)
							== ChunkPos.getZ(region);
		}

		private boolean isCovered(long chunk) {
			return regionCoverage.contains(ChunkStateTable.regionKey(chunk));
		}

		private int totalChunks() {
			return regionCoverage.size() * REGION_CHUNK_SIZE * REGION_CHUNK_SIZE;
		}

		private int countState(int state) {
			return chunkStates.count(regionCoverage, state);
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
				boolean forcedRetry = forcedRetries.contains(packed);

				EXECUTOR.execute(
						() -> {
							boolean succeeded = false;
							boolean abandoned = false;
							boolean isolatedFailure = false;
							Set<Long> accessed = context.beginAccessTracking();
							try {
								succeeded = generateOne(this, chunkX, chunkZ, forcedRetry);
							} catch (Throwable throwable) {
								if (throwable instanceof IsolatedTaskFailure isolated) {
									isolatedFailure = true;
									throwable = isolated.getCause();
								}
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
								chunkStates.remove(packed, CHUNK_STATE_ACTIVE);
								if (abandoned) {
								} else if (succeeded) {
									forcedRetries.remove(packed);
									chunkStates.remove(packed, CHUNK_STATE_FAILED);
									failureCounts.remove(packed);
									markFinished(packed);
								} else {
									handleFailure(next, accessed, !isolatedFailure);
								}
								inFlight.decrementAndGet();
							}
						});
			}
		}

		private ScheduledChunk pollNext() {
			return poll(pending);
		}

		private ScheduledChunk poll(ConcurrentLinkedDeque<Long> queue) {
			Long packed;
			while ((packed = queue.pollFirst()) != null) {
				if (packed == null) {
					return null;
				}
				chunkStates.remove(packed, CHUNK_STATE_QUEUED);
				if (isCovered(packed)
						&& !chunkStates.contains(packed, CHUNK_STATE_FINISHED)
						&& chunkStates.add(packed, CHUNK_STATE_ACTIVE)) {
					return new ScheduledChunk(packed);
				}
			}
			return null;
		}

		private void handleFailure(
				ScheduledChunk task, Set<Long> accessed, boolean invalidateAccessed) {
			long packed = task.packed();
			if (retiring || !isCovered(packed)) {
				return;
			}
			failedAttempts.addLast(
					new FailedAttempt(packed, Set.copyOf(accessed), invalidateAccessed));
		}

		private void markFinished(long packed) {
			synchronized (coverageLock) {
				if (isCovered(packed)) {
					chunkStates.add(packed, CHUNK_STATE_FINISHED);
				}
			}
		}
	}

	private static final class ChunkStateTable {
		private final ConcurrentHashMap<Long, RegionChunkStates> regions =
				new ConcurrentHashMap<>();

		private static long regionKey(long chunk) {
			return ChunkPos.pack(
					Math.floorDiv(ChunkPos.getX(chunk), REGION_CHUNK_SIZE),
					Math.floorDiv(ChunkPos.getZ(chunk), REGION_CHUNK_SIZE));
		}

		private static int index(long chunk) {
			return (ChunkPos.getZ(chunk) & (REGION_CHUNK_SIZE - 1)) << REGION_CHUNK_SHIFT
					| (ChunkPos.getX(chunk) & (REGION_CHUNK_SIZE - 1));
		}

		private void ensureRegions(Set<Long> regionKeys) {
			for (long key : regionKeys) {
				regions.computeIfAbsent(key, ignored -> new RegionChunkStates());
			}
		}

		private void retainRegions(Set<Long> regionKeys) {
			regions.keySet().retainAll(regionKeys);
		}

		private boolean contains(long chunk, int state) {
			RegionChunkStates region = regions.get(regionKey(chunk));
			return region != null && region.contains(index(chunk), state);
		}

		private boolean add(long chunk, int state) {
			RegionChunkStates region = regions.get(regionKey(chunk));
			return region != null && region.add(index(chunk), state);
		}

		private boolean remove(long chunk, int state) {
			RegionChunkStates region = regions.get(regionKey(chunk));
			return region != null && region.remove(index(chunk), state);
		}

		private void clearState(int state) {
			for (RegionChunkStates region : regions.values()) {
				region.clear(state);
			}
		}

		private int count(Set<Long> regionKeys, int state) {
			int total = 0;
			for (long key : regionKeys) {
				RegionChunkStates region = regions.get(key);
				if (region != null) {
					total += region.count(state);
				}
			}
			return total;
		}
	}

	private static final class RegionChunkStates {
		private final byte[] states = new byte[REGION_CHUNK_SIZE * REGION_CHUNK_SIZE];
		private int finished;
		private int queued;
		private int active;
		private int failed;

		private synchronized boolean contains(int index, int state) {
			return (states[index] & state) != 0;
		}

		private synchronized boolean add(int index, int state) {
			if ((states[index] & state) != 0) {
				return false;
			}
			states[index] = (byte) (states[index] | state);
			adjust(state, 1);
			return true;
		}

		private synchronized boolean remove(int index, int state) {
			if ((states[index] & state) == 0) {
				return false;
			}
			states[index] = (byte) (states[index] & ~state);
			adjust(state, -1);
			return true;
		}

		private synchronized void clear(int state) {
			if (count(state) == 0) {
				return;
			}
			for (int index = 0; index < states.length; index++) {
				states[index] = (byte) (states[index] & ~state);
			}
			setCount(state, 0);
		}

		private synchronized int count(int state) {
			return switch (state) {
				case CHUNK_STATE_FINISHED -> finished;
				case CHUNK_STATE_QUEUED -> queued;
				case CHUNK_STATE_ACTIVE -> active;
				case CHUNK_STATE_FAILED -> failed;
				default -> throw new IllegalArgumentException("Unknown chunk state " + state);
			};
		}

		private void adjust(int state, int amount) {
			setCount(state, count(state) + amount);
		}

		private void setCount(int state, int value) {
			switch (state) {
				case CHUNK_STATE_FINISHED -> finished = value;
				case CHUNK_STATE_QUEUED -> queued = value;
				case CHUNK_STATE_ACTIVE -> active = value;
				case CHUNK_STATE_FAILED -> failed = value;
				default -> throw new IllegalArgumentException("Unknown chunk state " + state);
			}
		}
	}

	private record ScheduledChunk(long packed) {}

	private record FailedAttempt(
			long target, Set<Long> accessedChunks, boolean invalidateAccessed) {}

	private static final class IsolatedTaskFailure extends RuntimeException {
		private IsolatedTaskFailure(Throwable cause) {
			super(cause);
		}
	}

	private enum RegionTraversal {
		CENTER,
		SOUTH,
		WEST,
		NORTH,
		EAST
	}

	private static boolean generateOne(Session owner, int chunkX, int chunkZ, boolean forcedRetry) {
		if (owner.retiring || !owner.engine.isLive()) {
			throw new CancellationException("Voxygen world is no longer live");
		}
		if (!forcedRetry && hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
			owner.skippedVoxy.incrementAndGet();
			return true;
		}

		owner.context.prepareStructureStartsForUnit(
				ChunkStateTable.regionKey(ChunkPos.pack(chunkX, chunkZ)));
		owner.context.prepareFull(chunkX, chunkZ);
		GeneratedChunk generated = owner.context.generatePreparedFull(chunkX, chunkZ);
		ProtoChunk chunk = generated.chunk();
		owner.generatedChunks.incrementAndGet();
		if (!forcedRetry && hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
			owner.skippedVoxy.incrementAndGet();
			return true;
		}

		LevelLightEngine lightEngine = owner.context.light(chunkX, chunkZ);
		int insertedSections = voxelizeAndInsert(owner.engine, chunk, lightEngine);
		if (insertedSections == 0) {
			owner.emptyChunks.incrementAndGet();
			return true;
		}
		owner.insertedChunks.incrementAndGet();
		owner.insertedSections.addAndGet(insertedSections);
		return true;
	}

	private static int voxelizeAndInsert(
			WorldEngine engine, ProtoChunk chunk, LevelLightEngine lightEngine) {
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
		private final ChunkGenerator structureGenerator;
		private final RandomState randomState;
		private final PalettedContainerFactory containers;
		private final long seed;
		private final ResourceKey<Level> dimension;
		private final ServerLevel serverLevel;
		private final StructureTemplateManager structureTemplates;
		private final ChunkGeneratorStructureState structureState;
		private final LevelStorageSource.LevelStorageAccess storageAccess;
		private final ConcurrentHashMap<Long, ProtoChunk> chunks = new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, GenerationStages> stageStates =
				new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, UnitArchive> unitArchives = new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, StructureStartPreparation> structurePreparations =
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
				ChunkGenerator structureGenerator,
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
			this.structureGenerator = structureGenerator;
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
					new StructureManager(world, new WorldOptions(seed, true, false), null);
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
					ChunkGenerator virtualGenerator =
							VoxygenWorldgenRules.simplifyGenerator(
									serverGenerator, registries, dimension);
					RandomState virtualRandomState = serverLevel.getChunkSource().randomState();
					ChunkGeneratorStructureState virtualStructureState =
							serverLevel.getChunkSource().getGeneratorState();
					if (virtualGenerator != serverGenerator
							&& virtualGenerator
									instanceof NoiseBasedChunkGenerator noiseGenerator) {
						virtualRandomState =
								RandomState.create(
										registries.lookupOrThrow(Registries.NOISE),
										seed,
										noiseGenerator.generatorSettings().value());
					}
					return new GenerationContext(
							level,
							registries,
							LevelHeightAccessor.create(
									serverLevel.getMinY(), serverLevel.getHeight()),
							virtualGenerator,
							serverGenerator,
							virtualRandomState,
							PalettedContainerFactory.create(registries),
							seed,
							dimension,
							serverLevel,
							serverLevel.getStructureTemplateManager(),
							virtualStructureState,
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
			NoiseBasedChunkGenerator structureGenerator =
					new NoiseBasedChunkGenerator(biomes, settings);
			ChunkGenerator generator =
					VoxygenWorldgenRules.simplifyGenerator(
							structureGenerator, registries, dimension);
			NoiseGeneratorSettings activeSettings =
					((NoiseBasedChunkGenerator) generator).generatorSettings().value();
			RandomState randomState = RandomState.create(noises, seed, activeSettings);
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
			RandomState structureRandomState = RandomState.create(noises, seed, settings.value());
			ChunkGeneratorStructureState structureState =
					structureGenerator.createState(
							registries.lookupOrThrow(Registries.STRUCTURE_SET),
							structureRandomState,
							seed);
			((VirtualStructureState) structureState).voxygen$markVirtual();
			return new GenerationContext(
					level,
					registries,
					height,
					generator,
					structureGenerator,
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

		private void prepareStructureStartsForUnit(long region) {
			int dependencyRadius =
					ChunkPyramid.GENERATION_PYRAMID
									.getStepTo(ChunkStatus.FEATURES)
									.directDependencies()
									.size()
							- 1;
			StructureStartPreparation preparation =
					structurePreparations.computeIfAbsent(
							region,
							ignored ->
									new StructureStartPreparation(
											(ChunkPos.getX(region) << REGION_CHUNK_SHIFT)
													- dependencyRadius
													- 1,
											(ChunkPos.getZ(region) << REGION_CHUNK_SHIFT)
													- dependencyRadius
													- 1,
											REGION_CHUNK_SIZE + ((dependencyRadius + 1) << 1)));
			try {
				preparation.prepare(this);
			} catch (RuntimeException | Error throwable) {
				structurePreparations.remove(region, preparation);
				throw throwable;
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
			ProtoChunk completed = completedChunk(key, ChunkStatus.STRUCTURE_STARTS);
			if (completed != null) {
				return completed;
			}
			Map<Structure, StructureStart> archived = archivedStarts(x, z);
			return ensure(
					GenerationStage.STRUCTURE_STARTS,
					key,
					() -> {
						ProtoChunk chunk = chunk(x, z);
						if (archived != null) {
							chunk.setAllStarts(archived);
						} else if (nativeContext != null) {
							ChunkStep step =
									ChunkPyramid.GENERATION_PYRAMID.getStepTo(
											ChunkStatus.STRUCTURE_STARTS);
							WorldgenSeedContext.run(
									seed,
									structureTemplates,
									generator,
									() ->
											ChunkStatusTasks.generateStructureStarts(
															nativeContext,
															step,
															nativeCache(x, z, step),
															chunk)
													.join());
						} else {
							WorldgenSeedContext.run(
									seed,
									structureTemplates,
									structureGenerator,
									() ->
											structureGenerator.createStructures(
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
			ProtoChunk completed = completedChunk(key, ChunkStatus.BIOMES);
			if (completed != null) {
				return completed;
			}
			BiomeSnapshot archived = archivedBiomes(x, z);
			return ensure(
					GenerationStage.BIOMES,
					key,
					() -> {
						ProtoChunk chunk = ensureStructureReferences(x, z);
						if (archived != null) {
							archived.restore(chunk);
							chunk.setPersistedStatus(ChunkStatus.BIOMES);
							return chunk;
						}
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
			ProtoChunk completed = completedChunk(key, ChunkStatus.STRUCTURE_REFERENCES);
			if (completed != null) {
				return completed;
			}
			Map<Structure, LongSet> archived = archivedReferences(x, z);
			return ensure(
					GenerationStage.STRUCTURE_REFERENCES,
					key,
					() -> {
						if (archived != null) {
							ProtoChunk chunk = ensureStructureStarts(x, z);
							chunk.setAllReferences(copyStructureReferences(archived));
							chunk.setPersistedStatus(ChunkStatus.STRUCTURE_REFERENCES);
							return chunk;
						}
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
									structureTemplates,
									structureGenerator,
									() ->
											structureGenerator.createReferences(
													world, structures, chunk));
						}
						chunk.setPersistedStatus(ChunkStatus.STRUCTURE_REFERENCES);
						return chunk;
					});
		}

		private ProtoChunk ensureTerrain(int x, int z) {
			long key = ChunkPos.pack(x, z);
			ProtoChunk completed = completedChunk(key, ChunkStatus.TERRAIN);
			if (completed != null) {
				return completed;
			}
			return ensure(
					GenerationStage.TERRAIN,
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
			ProtoChunk completed = completedChunk(key, ChunkStatus.FEATURES);
			if (completed != null) {
				return completed;
			}
			return ensure(
					GenerationStage.FEATURES,
					key,
					() -> {
						prepareFeatureDependenciesOnce(x, z);
						List<ReentrantLock> locks = lockArea(x, z, 1);
						try {
							ProtoChunk chunk = chunks.get(key);
							ChunkTransaction transaction = localWorld.beginTransaction();
							try {
								try {
									ProtoChunk workingChunk = transaction.shadow(chunk);
									if (nativeContext != null) {
										ChunkStep step =
												ChunkPyramid.GENERATION_PYRAMID.getStepTo(
														ChunkStatus.FEATURES);
										WorldgenSeedContext.run(
												seed,
												structureTemplates,
												generator,
												() ->
														ChunkStatusTasks.generateFeatures(
																		nativeContext,
																		step,
																		nativeCache(
																				x,
																				z,
																				step,
																				transaction),
																		workingChunk)
																.join());
									} else {
										localWorld.runInGenerationRegion(
												x,
												z,
												() ->
														WorldgenSeedContext.run(
																seed,
																structureTemplates,
																structureGenerator,
																() ->
																		structureGenerator
																				.applyBiomeDecoration(
																						world,
																						workingChunk,
																						structures)));
									}
								} catch (RuntimeException | Error throwable) {
									throw new IsolatedTaskFailure(throwable);
								}
								transaction.commit();
							} finally {
								localWorld.endTransaction(transaction);
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
		}

		private void prepareFeatureDependenciesOnce(int x, int z) {
			long key = ChunkPos.pack(x, z);
			if (completedChunk(key, ChunkStatus.FEATURES) != null) {
				return;
			}
			GenerationStages stages =
					stageStates.computeIfAbsent(key, ignored -> new GenerationStages());
			CompletableFuture<Void> existing;
			CompletableFuture<Void> created;
			synchronized (stages) {
				existing = stages.featurePreparation;
				if (existing != null) {
					created = null;
				} else {
					created = new CompletableFuture<>();
					stages.featurePreparation = created;
				}
			}
			if (created == null) {
				existing.join();
				return;
			}
			try {
				prepareFeatureDependencies(x, z);
				created.complete(null);
			} catch (Throwable throwable) {
				synchronized (stages) {
					if (stages.featurePreparation == created) {
						stages.featurePreparation = null;
					}
				}
				created.completeExceptionally(throwable);
				throw throwable;
			}
		}

		private StaticCache2D<GenerationChunkHolder> nativeCache(
				int centerX, int centerZ, ChunkStep step) {
			return nativeCache(centerX, centerZ, step, null);
		}

		private StaticCache2D<GenerationChunkHolder> nativeCache(
				int centerX, int centerZ, ChunkStep step, ChunkTransaction transaction) {
			int radius = step.directDependencies().size() - 1;
			return StaticCache2D.create(
					centerX,
					centerZ,
					radius,
					(x, z) -> {
						ProtoChunk chunk = chunks.get(ChunkPos.pack(x, z));
						boolean writable = Math.abs(x - centerX) <= 1 && Math.abs(z - centerZ) <= 1;
						return new VirtualGenerationChunkHolder(
								transaction == null || !writable
										? chunk
										: transaction.shadow(chunk));
					});
		}

		private LevelLightEngine light(int x, int z) {
			LevelLightEngine lightEngine =
					new LevelLightEngine(
							new VirtualLightChunkGetter(this),
							true,
							sourceLevel.dimensionType().hasSkyLight());
			List<ProtoChunk> lightingArea = new ArrayList<>(9);
			for (int chunkZ = z - 1; chunkZ <= z + 1; chunkZ++) {
				for (int chunkX = x - 1; chunkX <= x + 1; chunkX++) {
					ProtoChunk chunk = chunks.get(ChunkPos.pack(chunkX, chunkZ));
					if (chunk == null
							|| chunk.getPersistedStatus().isBefore(ChunkStatus.FEATURES)) {
						throw new IllegalStateException(
								"Missing prepared feature chunk [" + chunkX + ", " + chunkZ + "]");
					}
					lightingArea.add(chunk);
					chunk.initializeLightSources();
					chunk.setLightEngine(lightEngine);
					LevelChunkSection[] sections = chunk.getSections();
					for (int index = 0; index < sections.length; index++) {
						lightEngine.updateSectionStatus(
								SectionPos.of(chunk.getPos(), chunk.getMinSectionY() + index),
								sections[index].hasOnlyAir());
					}
				}
			}
			while (lightEngine.hasLightWork()) {
				lightEngine.runLightUpdates();
			}
			for (ProtoChunk chunk : lightingArea) {
				lightEngine.setLightEnabled(chunk.getPos(), true);
			}
			for (ProtoChunk chunk : lightingArea) {
				lightEngine.propagateLightSources(chunk.getPos());
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
					ignored -> {
						ProtoChunk created =
								new ProtoChunk(
										new ChunkPos(x, z),
										UpgradeData.EMPTY,
										height,
										containers,
										null);
						VoxygenWorldgenRules.registerChunk(created, dimension);
						return created;
					});
		}

		private void archiveGenerationUnit(long region) {
			int startX = ChunkPos.getX(region) << REGION_CHUNK_SHIFT;
			int startZ = ChunkPos.getZ(region) << REGION_CHUNK_SHIFT;
			UnitArchive archive = new UnitArchive();
			List<Long> discard = new ArrayList<>();
			for (int offsetZ = 0; offsetZ < REGION_CHUNK_SIZE; offsetZ++) {
				for (int offsetX = 0; offsetX < REGION_CHUNK_SIZE; offsetX++) {
					long key = ChunkPos.pack(startX + offsetX, startZ + offsetZ);
					ProtoChunk chunk = chunks.get(key);
					if (chunk == null) {
						continue;
					}
					int index = UnitArchive.index(offsetX, offsetZ);
					ChunkStatus status = chunk.getPersistedStatus();
					if (!status.isBefore(ChunkStatus.STRUCTURE_STARTS)) {
						archive.starts[index] = Map.copyOf(chunk.getAllStarts());
					}
					chunk.setLightEngine(null);
					chunk.setLightCorrect(false);
					int edgeDistance =
							Math.min(
									Math.min(offsetX, REGION_CHUNK_SIZE - 1 - offsetX),
									Math.min(offsetZ, REGION_CHUNK_SIZE - 1 - offsetZ));
					if (edgeDistance == 2 && !status.isBefore(ChunkStatus.BIOMES)) {
						archive.biomes[index] = BiomeSnapshot.capture(chunk);
						archive.references[index] =
								copyStructureReferences(chunk.getAllReferences());
					}
					if (edgeDistance >= 2) {
						discard.add(key);
					}
				}
			}
			unitArchives.put(region, archive);
			structurePreparations.remove(region);
			for (long key : discard) {
				evict(key);
			}
		}

		private Map<Structure, StructureStart> archivedStarts(int x, int z) {
			UnitArchive archive =
					unitArchives.get(
							ChunkPos.pack(
									Math.floorDiv(x, REGION_CHUNK_SIZE),
									Math.floorDiv(z, REGION_CHUNK_SIZE)));
			return archive == null ? null : archive.starts[UnitArchive.index(x, z)];
		}

		private BiomeSnapshot archivedBiomes(int x, int z) {
			UnitArchive archive =
					unitArchives.get(
							ChunkPos.pack(
									Math.floorDiv(x, REGION_CHUNK_SIZE),
									Math.floorDiv(z, REGION_CHUNK_SIZE)));
			return archive == null ? null : archive.biomes[UnitArchive.index(x, z)];
		}

		private Map<Structure, LongSet> archivedReferences(int x, int z) {
			UnitArchive archive =
					unitArchives.get(
							ChunkPos.pack(
									Math.floorDiv(x, REGION_CHUNK_SIZE),
									Math.floorDiv(z, REGION_CHUNK_SIZE)));
			return archive == null ? null : archive.references[UnitArchive.index(x, z)];
		}

		private static Map<Structure, LongSet> copyStructureReferences(
				Map<Structure, LongSet> source) {
			Map<Structure, LongSet> copy = new HashMap<>(source.size());
			for (Map.Entry<Structure, LongSet> entry : source.entrySet()) {
				copy.put(entry.getKey(), new LongOpenHashSet(entry.getValue()));
			}
			return copy;
		}

		private void evictRegionRings(int centerX, int centerZ, int firstRadius, int lastRadius) {
			for (int distance = Math.max(0, firstRadius); distance <= lastRadius; distance++) {
				if (distance == 0) {
					evictRegion(centerX, centerZ);
					continue;
				}
				int minX = centerX - distance;
				int maxX = centerX + distance;
				int minZ = centerZ - distance;
				int maxZ = centerZ + distance;
				for (int x = minX; x <= maxX; x++) {
					evictRegion(x, minZ);
					evictRegion(x, maxZ);
				}
				for (int z = minZ + 1; z < maxZ; z++) {
					evictRegion(minX, z);
					evictRegion(maxX, z);
				}
			}
		}

		private void evictRegion(int regionX, int regionZ) {
			long region = ChunkPos.pack(regionX, regionZ);
			unitArchives.remove(region);
			structurePreparations.remove(region);
			int startX = regionX << REGION_CHUNK_SHIFT;
			int startZ = regionZ << REGION_CHUNK_SHIFT;
			for (int offsetZ = 0; offsetZ < REGION_CHUNK_SIZE; offsetZ++) {
				for (int offsetX = 0; offsetX < REGION_CHUNK_SIZE; offsetX++) {
					evict(ChunkPos.pack(startX + offsetX, startZ + offsetZ));
				}
			}
		}

		private void compactCompletedStageCaches() {
			for (Map.Entry<Long, GenerationStages> entry : stageStates.entrySet()) {
				long key = entry.getKey();
				GenerationStages stages = entry.getValue();
				synchronized (stages) {
					for (GenerationStage stage : GenerationStage.values()) {
						CompletableFuture<ProtoChunk> future = stages.get(stage);
						if (future != null
								&& completedSuccessfully(future)
								&& completedChunk(key, stage.status) != null) {
							stages.set(stage, null);
						}
					}
					if (stages.featurePreparation != null
							&& completedSuccessfully(stages.featurePreparation)
							&& completedChunk(key, ChunkStatus.FEATURES) != null) {
						stages.featurePreparation = null;
					}
				}
			}
		}

		private static boolean completedSuccessfully(CompletableFuture<?> future) {
			return future.isDone() && !future.isCompletedExceptionally() && !future.isCancelled();
		}

		private void clearGenerationCache() {
			stageStates.clear();
			unitArchives.clear();
			structurePreparations.clear();
			for (ProtoChunk chunk : chunks.values()) {
				VoxygenWorldgenRules.unregisterChunk(chunk);
			}
			chunks.clear();
		}

		private void evict(long key) {
			stageStates.remove(key);
			VoxygenWorldgenRules.unregisterChunk(chunks.remove(key));
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

		private void invalidateStructurePreparation(long region) {
			structurePreparations.remove(region);
		}

		private void invalidateAccessed(Set<Long> accessed) {
			for (long key : accessed) {
				evict(key);
			}
		}

		private ProtoChunk ensure(GenerationStage stage, long key, ChunkOperation operation) {
			if (cancelled) {
				throw new CancellationException("Voxygen session was retired");
			}
			recordAccess(key);
			GenerationStages stages =
					stageStates.computeIfAbsent(key, ignored -> new GenerationStages());
			CompletableFuture<ProtoChunk> existing;
			CompletableFuture<ProtoChunk> created;
			synchronized (stages) {
				existing = stages.get(stage);
				if (existing == null) {
					created = new CompletableFuture<>();
					stages.set(stage, created);
				} else {
					created = null;
				}
			}
			if (existing != null) {
				return existing.join();
			}
			try {
				ProtoChunk result = operation.run();
				created.complete(result);
				return result;
			} catch (Throwable throwable) {
				synchronized (stages) {
					if (stages.get(stage) == created) {
						stages.set(stage, null);
					}
				}
				created.completeExceptionally(throwable);
				throw throwable;
			}
		}

		private ProtoChunk completedChunk(long key, ChunkStatus status) {
			ProtoChunk chunk = chunks.get(key);
			return chunk != null && !chunk.getPersistedStatus().isBefore(status) ? chunk : null;
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
			clearGenerationCache();
			if (storageAccess != null) {
				try {
					storageAccess.close();
				} catch (IOException exception) {
					LOGGER.warn("Cannot close Voxygen virtual world", exception);
				}
			}
		}
	}

	private enum GenerationStage {
		STRUCTURE_STARTS(ChunkStatus.STRUCTURE_STARTS),
		STRUCTURE_REFERENCES(ChunkStatus.STRUCTURE_REFERENCES),
		BIOMES(ChunkStatus.BIOMES),
		TERRAIN(ChunkStatus.TERRAIN),
		FEATURES(ChunkStatus.FEATURES);

		private final ChunkStatus status;

		GenerationStage(ChunkStatus status) {
			this.status = status;
		}
	}

	private static final class GenerationStages {
		private CompletableFuture<ProtoChunk> structureStarts;
		private CompletableFuture<ProtoChunk> structureReferences;
		private CompletableFuture<ProtoChunk> biomes;
		private CompletableFuture<ProtoChunk> terrain;
		private CompletableFuture<ProtoChunk> features;
		private CompletableFuture<Void> featurePreparation;

		private CompletableFuture<ProtoChunk> get(GenerationStage stage) {
			return switch (stage) {
				case STRUCTURE_STARTS -> structureStarts;
				case STRUCTURE_REFERENCES -> structureReferences;
				case BIOMES -> biomes;
				case TERRAIN -> terrain;
				case FEATURES -> features;
			};
		}

		private void set(GenerationStage stage, CompletableFuture<ProtoChunk> future) {
			switch (stage) {
				case STRUCTURE_STARTS -> structureStarts = future;
				case STRUCTURE_REFERENCES -> structureReferences = future;
				case BIOMES -> biomes = future;
				case TERRAIN -> terrain = future;
				case FEATURES -> features = future;
			}
		}
	}

	private static final class UnitArchive {
		@SuppressWarnings("unchecked")
		private final Map<Structure, StructureStart>[] starts =
				(Map<Structure, StructureStart>[])
						new Map<?, ?>[REGION_CHUNK_SIZE * REGION_CHUNK_SIZE];

		@SuppressWarnings("unchecked")
		private final Map<Structure, LongSet>[] references =
				(Map<Structure, LongSet>[]) new Map<?, ?>[REGION_CHUNK_SIZE * REGION_CHUNK_SIZE];

		private final BiomeSnapshot[] biomes =
				new BiomeSnapshot[REGION_CHUNK_SIZE * REGION_CHUNK_SIZE];

		private static int index(int x, int z) {
			return (z & (REGION_CHUNK_SIZE - 1)) << REGION_CHUNK_SHIFT
					| (x & (REGION_CHUNK_SIZE - 1));
		}
	}

	private static final class StructureStartPreparation {
		private final int startX;
		private final int startZ;
		private final int size;
		private final int total;
		private final AtomicInteger next = new AtomicInteger();
		private final AtomicInteger remaining;
		private final CompletableFuture<Void> completion = new CompletableFuture<>();

		private StructureStartPreparation(int startX, int startZ, int size) {
			this.startX = startX;
			this.startZ = startZ;
			this.size = size;
			this.total = size * size;
			this.remaining = new AtomicInteger(total);
		}

		private void prepare(GenerationContext context) {
			if (!completion.isDone()) {
				try {
					int index;
					while (!completion.isDone() && (index = next.getAndIncrement()) < total) {
						context.ensureStructureStarts(startX + index % size, startZ + index / size);
						if (remaining.decrementAndGet() == 0) {
							completion.complete(null);
						}
					}
				} catch (RuntimeException | Error throwable) {
					completion.completeExceptionally(throwable);
					throw throwable;
				}
			}
			completion.join();
		}
	}

	private record BiomeSnapshot(PalettedContainer<Holder<Biome>>[] sections) {
		@SuppressWarnings("unchecked")
		private static BiomeSnapshot capture(ProtoChunk chunk) {
			LevelChunkSection[] source = chunk.getSections();
			PalettedContainer<Holder<Biome>>[] sections =
					(PalettedContainer<Holder<Biome>>[]) new PalettedContainer<?>[source.length];
			for (int index = 0; index < source.length; index++) {
				sections[index] = source[index].getBiomes().copy();
			}
			return new BiomeSnapshot(sections);
		}

		private void restore(ProtoChunk chunk) {
			LevelChunkSection[] target = chunk.getSections();
			if (target.length != sections.length) {
				throw new IllegalStateException("Archived biome section count changed");
			}
			for (int index = 0; index < target.length; index++) {
				target[index] =
						new LevelChunkSection(target[index].getStates(), sections[index].copy());
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

		private ProtoChunk view(ProtoChunk original) {
			if (original == null) {
				return null;
			}
			TransactionalProtoChunk shadow =
					shadows.get(ChunkPos.pack(original.getPos().x(), original.getPos().z()));
			return shadow == null ? original : shadow;
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
		private boolean initializing = true;
		private boolean metadataDirty;

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
				} else {
					throw new IllegalStateException(
							"Cannot clone block entity at "
									+ position
									+ " for feature transaction");
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
			initializing = false;
		}

		@Override
		public BlockState setBlockState(BlockPos position, BlockState state, int flags) {
			ownSection(getSectionIndex(position.getY()));
			return super.setBlockState(position, state, flags);
		}

		@Override
		public LevelChunkSection getSection(int index) {
			ownSection(index);
			return super.getSection(index);
		}

		@Override
		public LevelChunkSection[] getSections() {
			if (ownedSections != null) {
				for (int index = 0; index < ownedSections.length; index++) {
					ownSection(index);
				}
			}
			return super.getSections();
		}

		@Override
		public BlockEntity getBlockEntity(BlockPos position) {
			if (!initializing) {
				metadataDirty = true;
			}
			return super.getBlockEntity(position);
		}

		@Override
		public void setBlockEntity(BlockEntity blockEntity) {
			if (!initializing) {
				metadataDirty = true;
			}
			super.setBlockEntity(blockEntity);
		}

		@Override
		public void removeBlockEntity(BlockPos position) {
			if (!initializing) {
				metadataDirty = true;
			}
			super.removeBlockEntity(position);
		}

		@Override
		public void setBlockEntityNbt(CompoundTag tag) {
			if (!initializing) {
				metadataDirty = true;
			}
			super.setBlockEntityNbt(tag);
		}

		private void ownSection(int index) {
			if (ownedSections == null
					|| index < 0
					|| index >= ownedSections.length
					|| ownedSections[index]) {
				return;
			}
			super.getSections()[index] = copySection(super.getSections()[index]);
			ownedSections[index] = true;
		}

		private void commit() {
			boolean sectionDirty = false;
			for (boolean owned : ownedSections) {
				sectionDirty |= owned;
			}
			if (!sectionDirty && !metadataDirty) {
				return;
			}
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
						0;
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
			ChunkAccess chunk = viewChunkAt(x >> 4, z >> 4);
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
			return chunkAt(x, z, true);
		}

		private ProtoChunk viewChunkAt(int x, int z) {
			return chunkAt(x, z, false);
		}

		private ProtoChunk chunkAt(int x, int z, boolean writable) {
			long key = ChunkPos.pack(x, z);
			context.recordAccess(key);
			ProtoChunk chunk = chunks.get(key);
			ChunkTransaction active = transaction.get();
			if (active == null) {
				return chunk;
			}
			return writable ? active.shadow(chunk) : active.view(chunk);
		}

		private BlockState getBlockState(BlockPos pos) {
			ProtoChunk chunk = viewChunkAt(pos.getX() >> 4, pos.getZ() >> 4);
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
			BlockState state = (BlockState) args[1];
			if (VoxygenWorldgenRules.shouldRejectGeneratedBlock(state)) {
				return false;
			}
			ProtoChunk chunk = chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			if (chunk == null || !context.height.isInsideBuildHeight(pos)) {
				return false;
			}
			int flags = args.length > 2 ? (int) args[2] : 0;
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
			ProtoChunk chunk = viewChunkAt(Math.floorDiv(quartX, 4), Math.floorDiv(quartZ, 4));
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

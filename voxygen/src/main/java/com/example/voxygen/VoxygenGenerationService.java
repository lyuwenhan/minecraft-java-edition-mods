package com.example.voxygen;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.ticks.BlackholeTickAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/** Generates vanilla chunks off-thread and inserts their voxelized sections into Voxy. */
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
	private static final long SESSION_START_DELAY_NANOS = TimeUnit.SECONDS.toNanos(2);

	private static volatile Session session;
	private static volatile ClientLevel eligibleLevel;
	private static volatile long eligibleSinceNanos;
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

		long now = System.nanoTime();
		if (eligibleLevel != level) {
			eligibleLevel = level;
			eligibleSinceNanos = now;
		}
		int centerX = client.player.chunkPosition().x();
		int centerZ = client.player.chunkPosition().z();
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
			session = null;
			current = null;
		}

		if (current == null) {
			if (now - eligibleSinceNanos < SESSION_START_DELAY_NANOS) {
				state =
						Component.translatable(
								"command.voxygen.state.preparing",
								dimension.identifier().toString());
				return;
			}
			current =
					new Session(
							level,
							engine,
							seed,
							dimension,
							centerX,
							centerZ,
							config.radius,
							config.threads);
			session = current;
			state =
					Component.translatable(
							"command.voxygen.state.preparing", dimension.identifier().toString());
		}

		current.setWorkerCount(config.threads);
		current.requestWindowUpdate(centerX, centerZ, config.radius);
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
				current.failures.get(),
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
							&& current.retries.isEmpty()
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
				session = null;
			}
		}
		eligibleLevel = null;
		eligibleSinceNanos = 0L;
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
		private final Set<Long> finished = ConcurrentHashMap.newKeySet();
		private final Set<Long> queued = ConcurrentHashMap.newKeySet();
		private final Set<Long> retryQueued = ConcurrentHashMap.newKeySet();
		private final Set<Long> activeChunks = ConcurrentHashMap.newKeySet();
		private final ConcurrentLinkedDeque<Long> pending = new ConcurrentLinkedDeque<>();
		private final ConcurrentLinkedDeque<Long> retries = new ConcurrentLinkedDeque<>();
		private final AtomicInteger inFlight = new AtomicInteger();
		private final AtomicLong dispatches = new AtomicLong();
		private final AtomicInteger generatedChunks = new AtomicInteger();
		private final AtomicInteger insertedChunks = new AtomicInteger();
		private final AtomicInteger insertedSections = new AtomicInteger();
		private final AtomicInteger skippedVoxy = new AtomicInteger();
		private final AtomicInteger emptyChunks = new AtomicInteger();
		private final AtomicInteger failures = new AtomicInteger();
		private final AtomicInteger verificationFailures = new AtomicInteger();
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
			synchronized (windowLock) {
				if (windowFuture != null && windowFuture.cancel(false)) {
					windowTaskScheduled = false;
					windowFuture = null;
				}
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

			List<Long> ordered = squareInChebyshevOrder(newCenterX, newCenterZ, newRadius);
			Set<Long> nextCoverage = new HashSet<>(ordered);
			synchronized (windowLock) {
				if (retiring || version != requestedWindowVersion) {
					completeWindowUpdate(version);
					return;
				}
			}

			synchronized (coverageLock) {
				coverage.retainAll(nextCoverage);
				finished.retainAll(nextCoverage);
				retryQueued.retainAll(nextCoverage);
				retries.removeIf(packed -> !nextCoverage.contains(packed));
				coverage.addAll(nextCoverage);
				pending.clear();
				queued.clear();
				for (long packed : ordered) {
					if (!finished.contains(packed)
							&& !activeChunks.contains(packed)
							&& !retryQueued.contains(packed)
							&& queued.add(packed)) {
						pending.addLast(packed);
					}
				}
				centerX = newCenterX;
				centerZ = newCenterZ;
				radius = newRadius;
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
			while (inFlight.get() < workerCount) {
				Long next = pollNext();
				if (next == null) {
					if (inFlight.get() == 0 && pending.isEmpty() && retries.isEmpty()) {
						state = Component.translatable("command.voxygen.state.complete");
					}
					return;
				}
				long packed = next;
				int chunkX = ChunkPos.getX(packed);
				int chunkZ = ChunkPos.getZ(packed);

				inFlight.incrementAndGet();
				EXECUTOR.execute(
						() -> {
							boolean succeeded = false;
							try {
								succeeded = generateOne(this, chunkX, chunkZ);
							} catch (Throwable throwable) {
								failures.incrementAndGet();
								LOGGER.warn(
										"Failed to generate chunk [{}, {}], reason: {}",
										chunkX,
										chunkZ,
										failureReason(throwable),
										throwable);
							} finally {
								activeChunks.remove(packed);
								if (succeeded) {
									markFinished(packed);
								} else {
									enqueueRetry(packed);
								}
								inFlight.decrementAndGet();
							}
						});
			}
		}

		private Long pollNext() {
			boolean retryTurn = (dispatches.get() + 1L) % 100L == 0L;
			Long packed = retryTurn ? poll(retries, retryQueued) : poll(pending, queued);
			if (packed == null) {
				packed = retryTurn ? poll(pending, queued) : poll(retries, retryQueued);
			}
			if (packed != null) {
				dispatches.incrementAndGet();
			}
			return packed;
		}

		private Long poll(ConcurrentLinkedDeque<Long> queue, Set<Long> membership) {
			Long packed;
			while ((packed = queue.pollFirst()) != null) {
				membership.remove(packed);
				if (coverage.contains(packed)
						&& !finished.contains(packed)
						&& activeChunks.add(packed)) {
					return packed;
				}
			}
			return null;
		}

		private void enqueueRetry(long packed) {
			if (coverage.contains(packed) && retryQueued.add(packed)) {
				retries.addLast(packed);
			}
		}

		private void markFinished(long packed) {
			synchronized (coverageLock) {
				if (coverage.contains(packed)) {
					finished.add(packed);
				}
			}
		}
	}

	private static boolean generateOne(Session owner, int chunkX, int chunkZ) {
		if (hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
			owner.skippedVoxy.incrementAndGet();
			return true;
		}

		ProtoChunk chunk = owner.context.generateTerrain(chunkX, chunkZ);
		owner.generatedChunks.incrementAndGet();
		if (hasVoxyData(owner.engine, owner.context.height, chunkX, chunkZ)) {
			owner.skippedVoxy.incrementAndGet();
			return true;
		}

		List<VoxelizedSection> voxelized = voxelize(owner.engine, chunk);
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

	private static List<VoxelizedSection> voxelize(WorldEngine engine, ProtoChunk chunk) {
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
							(x, y, z) -> (byte) 0x0F);
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

	private static List<Long> squareInChebyshevOrder(int centerX, int centerZ, int radius) {
		List<Long> result = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
		result.add(ChunkPos.pack(centerX, centerZ));
		for (int distance = 1; distance <= radius; distance++) {
			int minX = centerX - distance;
			int maxX = centerX + distance;
			int minZ = centerZ - distance;
			int maxZ = centerZ + distance;

			// Continue from the previous ring's north-east corner by stepping east,
			// then walk clockwise along the east, south, west, and north edges.
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

		private GenerationContext(
				ClientLevel sourceLevel,
				RegistryAccess registries,
				LevelHeightAccessor height,
				ChunkGenerator generator,
				RandomState randomState,
				PalettedContainerFactory containers,
				long seed) {
			this.sourceLevel = sourceLevel;
			this.registries = registries;
			this.height = height;
			this.generator = generator;
			this.randomState = randomState;
			this.containers = containers;
			this.seed = seed;
		}

		private static GenerationContext create(
				ClientLevel level, ResourceKey<Level> dimension, long seed) {
			RegistryAccess registries = worldgenRegistries();
			Minecraft client = Minecraft.getInstance();
			if (client.getSingleplayerServer() != null) {
				ServerLevel serverLevel = client.getSingleplayerServer().getLevel(dimension);
				if (serverLevel != null) {
					ChunkGenerator serverGenerator = serverLevel.getChunkSource().getGenerator();
					return new GenerationContext(
							level,
							registries,
							LevelHeightAccessor.create(
									serverLevel.getMinY(), serverLevel.getHeight()),
							serverGenerator,
							serverLevel.getChunkSource().randomState(),
							PalettedContainerFactory.create(registries),
							seed);
				}
			}

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
			return new GenerationContext(
					level,
					registries,
					height,
					generator,
					randomState,
					PalettedContainerFactory.create(registries),
					seed);
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

		private ProtoChunk generateTerrain(int targetX, int targetZ) {
			Map<Long, ProtoChunk> chunks = new ConcurrentHashMap<>();
			ProtoChunk chunk =
					new ProtoChunk(
							new ChunkPos(targetX, targetZ),
							UpgradeData.EMPTY,
							height,
							containers,
							null);
			chunks.put(ChunkPos.pack(targetX, targetZ), chunk);

			LocalWorld handler = new LocalWorld(this, chunks);
			WorldGenLevel world = handler.proxy();
			StructureManager structures =
					new StructureManager(world, new WorldOptions(seed, false, false), null);
			BiomeManager biomeManager = new BiomeManager(world, BiomeManager.obfuscateSeed(seed));

			generator.createBiomes(randomState, Blender.empty(), structures, chunk).join();
			chunk.setPersistedStatus(ChunkStatus.BIOMES);
			generator
					.buildTerrain(
							chunk,
							Blender.empty(),
							randomState,
							structures,
							biomeManager,
							null,
							generator.getBiomeSource().possibleBiomes())
					.join();
			Heightmap.primeHeightmaps(
					chunk,
					EnumSet.of(
							Heightmap.Types.MOTION_BLOCKING,
							Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
							Heightmap.Types.OCEAN_FLOOR,
							Heightmap.Types.WORLD_SURFACE));
			chunk.setPersistedStatus(ChunkStatus.TERRAIN);
			return chunk;
		}
	}

	private static final class LocalWorld implements InvocationHandler {
		private final GenerationContext context;
		private final Map<Long, ProtoChunk> chunks;
		private final AtomicLong subTicks = new AtomicLong();
		private final LocalChunkSource chunkSource;
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
				case "getRandom" -> RandomSource.create(context.seed);
				case "nextSubTickCount" -> subTicks.incrementAndGet();
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
			int x = (int) args[1];
			int z = (int) args[2];
			ChunkAccess chunk = chunkAt(x >> 4, z >> 4);
			return chunk == null ? context.height.getMinY() : chunk.getHeight(type, x & 15, z & 15);
		}

		private Object getChunk(Object[] args) {
			if (args[0] instanceof BlockPos pos) {
				return chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			}
			return chunkAt((int) args[0], (int) args[1]);
		}

		private ProtoChunk chunkAt(int x, int z) {
			return chunks.get(ChunkPos.pack(x, z));
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
			chunk.setBlockState(pos, (BlockState) args[1], flags);
			return true;
		}

		private boolean setAir(BlockPos pos) {
			ProtoChunk chunk = chunkAt(pos.getX() >> 4, pos.getZ() >> 4);
			if (chunk == null || !context.height.isInsideBuildHeight(pos)) {
				return false;
			}
			chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), 0);
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
			return chunks.get(ChunkPos.pack(x, z));
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

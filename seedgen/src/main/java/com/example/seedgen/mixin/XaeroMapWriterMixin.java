package com.example.seedgen.mixin;

import com.example.seedgen.xaero.XaeroWorldMapWriteBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import xaero.map.MapWriter;
import xaero.map.biome.BlockTintProvider;
import xaero.map.cache.BlockStateShortShapeCache;
import xaero.map.region.MapRegion;
import xaero.map.region.MapTile;
import xaero.map.region.MapTileChunk;
import xaero.map.region.MapUpdateFastConfig;
import xaero.map.region.OverlayManager;

@Pseudo
@Mixin(targets = "xaero.map.MapWriter", remap = false)
abstract class XaeroMapWriterMixin {
	@Unique private static final int SEEDGEN_WRITE_BATCH_SIZE = 32;

	@Unique
	private static final ThreadLocal<Integer> SEEDGEN_WRITES_REMAINING = new ThreadLocal<>();

	@Shadow(remap = false)
	private int X;

	@Shadow(remap = false)
	private int Z;

	@Shadow(remap = false)
	private int playerChunkX;

	@Shadow(remap = false)
	private int playerChunkZ;

	@Shadow(remap = false)
	private int insideX;

	@Shadow(remap = false)
	private int insideZ;

	@Shadow(remap = false)
	private BlockStateShortShapeCache blockStateShortShapeCache;

	@Redirect(
			method = "writeMap",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lxaero/map/MapWriter;writeChunk(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Registry;IZLnet/minecraft/core/Registry;Lxaero/map/region/OverlayManager;ZZZZZLnet/minecraft/core/BlockPos$MutableBlockPos;Lxaero/map/biome/BlockTintProvider;IIIIIIIIILxaero/map/region/MapUpdateFastConfig;)Z",
							remap = false),
			remap = false)
	private boolean seedgen$writeGeneratedChunk(
			MapWriter writer,
			Level level,
			Registry<Block> blockRegistry,
			int loadDistance,
			boolean periodicUpdate,
			Registry<Biome> biomeRegistry,
			OverlayManager overlayManager,
			boolean loadNewChunks,
			boolean updateChunks,
			boolean ignoreHeightmaps,
			boolean flowers,
			boolean detailedDebug,
			BlockPos.MutableBlockPos mutablePos,
			BlockTintProvider blockTintProvider,
			int caveDepth,
			int caveStart,
			int writingLayer,
			int tileChunkX,
			int tileChunkZ,
			int regionChunkX,
			int regionChunkZ,
			int chunkX,
			int chunkZ,
			MapUpdateFastConfig fastConfig) {
		Integer writesRemaining = SEEDGEN_WRITES_REMAINING.get();
		if (writesRemaining == null) {
			writesRemaining = SEEDGEN_WRITE_BATCH_SIZE;
			SEEDGEN_WRITES_REMAINING.set(writesRemaining);
		} else if (writesRemaining <= 0) {
			SEEDGEN_WRITES_REMAINING.remove();
			return writer.writeChunk(
					level,
					blockRegistry,
					loadDistance,
					periodicUpdate,
					biomeRegistry,
					overlayManager,
					loadNewChunks,
					updateChunks,
					ignoreHeightmaps,
					flowers,
					detailedDebug,
					mutablePos,
					blockTintProvider,
					caveDepth,
					caveStart,
					writingLayer,
					tileChunkX,
					tileChunkZ,
					regionChunkX,
					regionChunkZ,
					chunkX,
					chunkZ,
					fastConfig);
		}
		XaeroWorldMapWriteBridge.PendingChunk pending = XaeroWorldMapWriteBridge.poll();
		while (pending != null
				&& (pending.level() != level
						|| !pending.belongsTo(
								writer.getMapProcessor().getCurrentWorldId(),
								writer.getMapProcessor().getCurrentDimId(),
								writer.getMapProcessor().getCurrentMWId()))) {
			XaeroWorldMapWriteBridge.complete(pending);
			pending = XaeroWorldMapWriteBridge.poll();
		}
		if (pending == null) {
			SEEDGEN_WRITES_REMAINING.remove();
			return writer.writeChunk(
					level,
					blockRegistry,
					loadDistance,
					periodicUpdate,
					biomeRegistry,
					overlayManager,
					loadNewChunks,
					updateChunks,
					ignoreHeightmaps,
					flowers,
					detailedDebug,
					mutablePos,
					blockTintProvider,
					caveDepth,
					caveStart,
					writingLayer,
					tileChunkX,
					tileChunkZ,
					regionChunkX,
					regionChunkZ,
					chunkX,
					chunkZ,
					fastConfig);
		}

		int generatedChunkX = pending.chunkX();
		int generatedChunkZ = pending.chunkZ();
		int oldX = X;
		int oldZ = Z;
		int oldPlayerChunkX = playerChunkX;
		int oldPlayerChunkZ = playerChunkZ;
		int oldInsideX = insideX;
		int oldInsideZ = insideZ;
		boolean[] skipped = new boolean[1];
		boolean waitForXaero = false;
		try {
			playerChunkX = generatedChunkX;
			playerChunkZ = generatedChunkZ;
			insideX = generatedChunkX & 3;
			insideZ = generatedChunkZ & 3;
			MapTile tile =
					writer.getMapProcessor()
							.getMapTile(writingLayer, generatedChunkX, generatedChunkZ);
			if (tile != null && (tile.isLoaded() || tile.wasWrittenOnce())) {
				XaeroWorldMapWriteBridge.complete(pending);
			} else {
				LevelChunk snapshot = pending.snapshot();
				XaeroWorldMapWriteBridge.runWithChunk(
						snapshot,
						() ->
								skipped[0] =
										writer.writeChunk(
												level,
												blockRegistry,
												1,
												false,
												biomeRegistry,
												overlayManager,
												true,
												true,
												ignoreHeightmaps,
												flowers,
												detailedDebug,
												mutablePos,
												blockTintProvider,
												caveDepth,
												caveStart,
												writingLayer,
												generatedChunkX >> 2,
												generatedChunkZ >> 2,
												(generatedChunkX >> 2) & 7,
												(generatedChunkZ >> 2) & 7,
												generatedChunkX,
												generatedChunkZ,
												fastConfig));

				tile =
						writer.getMapProcessor()
								.getMapTile(writingLayer, generatedChunkX, generatedChunkZ);
				if (!skipped[0]) {
					MapTileChunk tileChunk =
							writer.getMapProcessor()
									.getMapChunk(
											writingLayer,
											generatedChunkX >> 2,
											generatedChunkZ >> 2);
					if (tileChunk != null) {
						tileChunk.updateBuffers(
								writer.getMapProcessor(),
								blockTintProvider,
								overlayManager,
								detailedDebug,
								blockStateShortShapeCache,
								fastConfig);
						tileChunk.setChanged(false);
					}
					XaeroWorldMapWriteBridge.complete(pending);
				} else if (tile != null && (tile.isLoaded() || tile.wasWrittenOnce())) {
					XaeroWorldMapWriteBridge.complete(pending);
				} else {
					MapRegion region =
							writer.getMapProcessor()
									.getLeafMapRegion(
											writingLayer,
											generatedChunkX >> 5,
											generatedChunkZ >> 5,
											true);
					if (region != null && region.getLoadState() != 2) {
						writer.getMapProcessor()
								.getMapSaveLoad()
								.requestLoad(region, "Seedgen generated chunk", false);
					}
					XaeroWorldMapWriteBridge.retryFirst(pending);
					waitForXaero = true;
				}
			}
		} catch (Throwable throwable) {
			XaeroWorldMapWriteBridge.fail(pending, throwable);
			waitForXaero = true;
		} finally {
			X = oldX;
			Z = oldZ;
			playerChunkX = oldPlayerChunkX;
			playerChunkZ = oldPlayerChunkZ;
			insideX = oldInsideX;
			insideZ = oldInsideZ;
		}
		SEEDGEN_WRITES_REMAINING.set(waitForXaero ? 0 : writesRemaining - 1);
		return seedgen$writeGeneratedChunk(
				writer,
				level,
				blockRegistry,
				loadDistance,
				periodicUpdate,
				biomeRegistry,
				overlayManager,
				loadNewChunks,
				updateChunks,
				ignoreHeightmaps,
				flowers,
				detailedDebug,
				mutablePos,
				blockTintProvider,
				caveDepth,
				caveStart,
				writingLayer,
				tileChunkX,
				tileChunkZ,
				regionChunkX,
				regionChunkZ,
				chunkX,
				chunkZ,
				fastConfig);
	}

	@Redirect(
			method = "writeChunk",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/Level;getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
							remap = true),
			remap = false)
	private ChunkAccess seedgen$getGeneratedChunk(
			Level level, int chunkX, int chunkZ, ChunkStatus status, boolean create) {
		LevelChunk active = XaeroWorldMapWriteBridge.activeChunk();
		if (active != null && active.getPos().x() == chunkX && active.getPos().z() == chunkZ) {
			return active;
		}
		return level.getChunk(chunkX, chunkZ, status, create);
	}

	@Redirect(
			method = "writeChunk",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/Level;getChunk(II)Lnet/minecraft/world/level/chunk/LevelChunk;",
							remap = true),
			remap = false)
	private LevelChunk seedgen$getGeneratedNeighbour(Level level, int chunkX, int chunkZ) {
		LevelChunk active = XaeroWorldMapWriteBridge.activeChunk();
		if (active != null
				&& Math.abs(active.getPos().x() - chunkX) <= 1
				&& Math.abs(active.getPos().z() - chunkZ) <= 1) {
			return active;
		}
		return level.getChunk(chunkX, chunkZ);
	}
}

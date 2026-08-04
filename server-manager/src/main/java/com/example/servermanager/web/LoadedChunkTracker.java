package com.example.servermanager.web;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Tracks loaded chunks without scanning or invoking arbitrary Minecraft methods. */
public final class LoadedChunkTracker {
	private static final Set<LevelChunk> LOADED_CHUNKS =
			Collections.newSetFromMap(new IdentityHashMap<>());
	private static boolean registered;

	private LoadedChunkTracker() {}

	public static synchronized void register() {
		if (registered) return;
		registered = true;

		ServerChunkEvents.CHUNK_LOAD.register(
				(level, chunk, generated) -> {
					synchronized (LOADED_CHUNKS) {
						LOADED_CHUNKS.add(chunk);
					}
				});

		ServerChunkEvents.CHUNK_UNLOAD.register(
				(level, chunk) -> {
					synchronized (LOADED_CHUNKS) {
						LOADED_CHUNKS.remove(chunk);
					}
				});
	}

	public static int loadedBlockEntityCount() {
		int count = 0;
		synchronized (LOADED_CHUNKS) {
			for (LevelChunk chunk : LOADED_CHUNKS) {
				count += chunk.getBlockEntities().size();
			}
		}
		return count;
	}

	public static void clear() {
		synchronized (LOADED_CHUNKS) {
			LOADED_CHUNKS.clear();
		}
	}
}

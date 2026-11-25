package com.example.seedgen;

import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class ClientWorldSeeds {
	private static final Map<ClientLevel, Long> SEEDS =
			Collections.synchronizedMap(new WeakHashMap<>());
	private static volatile long pendingSeed;
	private static volatile boolean hasPendingSeed;

	private ClientWorldSeeds() {}

	public static void onSpawnInfo(long hashedSeed) {
		pendingSeed = hashedSeed;
		hasPendingSeed = true;
	}

	public static void capture(ClientLevel level) {
		if (level == null || SEEDS.containsKey(level) || !hasPendingSeed) {
			return;
		}
		SEEDS.put(level, pendingSeed);
		hasPendingSeed = false;
	}

	public static Long hashedSeedOf(ClientLevel level) {
		return level == null ? null : SEEDS.get(level);
	}
}

package com.example.voxygen;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.function.Supplier;

public final class WorldgenSeedContext {
	private static final ThreadLocal<Long> SEED = new ThreadLocal<>();
	private static final ThreadLocal<StructureTemplateManager> TEMPLATES = new ThreadLocal<>();

	private WorldgenSeedContext() {}

	public static <T> T call(long seed, Supplier<T> action) {
		Long previous = SEED.get();
		SEED.set(seed);
		try {
			return action.get();
		} finally {
			if (previous == null) {
				SEED.remove();
			} else {
				SEED.set(previous);
			}
		}
	}

	public static void run(long seed, Runnable action) {
		run(seed, null, action);
	}

	public static void run(long seed, StructureTemplateManager templates, Runnable action) {
		StructureTemplateManager previousTemplates = TEMPLATES.get();
		if (templates == null) {
			TEMPLATES.remove();
		} else {
			TEMPLATES.set(templates);
		}
		call(
				seed,
				() -> {
					try {
						action.run();
						return null;
					} finally {
						if (previousTemplates == null) {
							TEMPLATES.remove();
						} else {
							TEMPLATES.set(previousTemplates);
						}
					}
				});
	}

	public static StructureTemplateManager templates() {
		return TEMPLATES.get();
	}

	public static long seedOrLevel(ServerLevel level) {
		Long seed = SEED.get();
		if (seed != null) {
			return seed;
		}
		if (level == null) {
			throw new IllegalStateException("No virtual world seed is active");
		}
		return level.getSeed();
	}
}

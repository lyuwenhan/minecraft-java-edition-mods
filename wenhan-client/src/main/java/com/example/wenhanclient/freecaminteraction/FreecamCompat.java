package com.example.wenhanclient.freecaminteraction;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

final class FreecamCompat {
	private static boolean resolved;
	private static Method isEnabled;
	private static Method isPlayerControlEnabled;
	private static Method getConfig;
	private static Method shouldPreventInteractions;

	private FreecamCompat() {}

	static boolean isActive() {
		if (!FabricLoader.getInstance().isModLoaded("freecam")) {
			return false;
		}
		try {
			resolve();
			return isEnabled != null && Boolean.TRUE.equals(isEnabled.invoke(null));
		} catch (ReflectiveOperationException | LinkageError ignored) {
			return false;
		}
	}

	static boolean canInteract() {
		if (!FabricLoader.getInstance().isModLoaded("freecam")) {
			return false;
		}
		try {
			resolve();
			return shouldPreventInteractions != null
					&& Boolean.TRUE.equals(isEnabled.invoke(null))
					&& Boolean.FALSE.equals(isPlayerControlEnabled.invoke(null))
					&& Boolean.FALSE.equals(
							shouldPreventInteractions.invoke(getConfig.invoke(null)));
		} catch (ReflectiveOperationException | LinkageError ignored) {
			return false;
		}
	}

	private static void resolve() throws ReflectiveOperationException {
		if (resolved) {
			return;
		}
		resolved = true;
		Class<?> freecam = Class.forName("net.xolt.freecam.Freecam");
		Class<?> config = Class.forName("net.xolt.freecam.config.ModConfig");
		isEnabled = freecam.getMethod("isEnabled");
		isPlayerControlEnabled = freecam.getMethod("isPlayerControlEnabled");
		getConfig = config.getMethod("get");
		shouldPreventInteractions = config.getMethod("shouldPreventInteractions");
	}
}

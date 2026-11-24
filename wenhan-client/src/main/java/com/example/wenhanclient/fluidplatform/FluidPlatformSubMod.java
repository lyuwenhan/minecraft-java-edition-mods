package com.example.wenhanclient.fluidplatform;

import com.example.wenhanclient.WenhanClientMod;

public final class FluidPlatformSubMod {
	private static FluidPlatformConfig config = new FluidPlatformConfig();

	private FluidPlatformSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.fluidPlatform.copy();
	}

	public static FluidPlatformConfig config() {
		return config.copy();
	}

	public static void setConfig(FluidPlatformConfig newConfig) {
		config = newConfig == null ? new FluidPlatformConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.fluidPlatform = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}
}

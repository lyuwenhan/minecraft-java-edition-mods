package com.example.wenhanclient.fluidplatform;

import com.example.wenhanclient.WenhanClientMod;

public final class FluidPlatformConfig {
	public boolean enabled = false;

	public static FluidPlatformConfig load() {
		return WenhanClientMod.CONFIG.fluidPlatform.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.fluidPlatform = copy();
		WenhanClientMod.CONFIG.save();
	}

	public FluidPlatformConfig copy() {
		FluidPlatformConfig copy = new FluidPlatformConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

package com.example.wenhanclient.notexturerotation;

import com.example.wenhanclient.WenhanClientMod;

public final class NoTextureRotationSubMod {
	private static NoTextureRotationConfig config = new NoTextureRotationConfig();

	private NoTextureRotationSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noTextureRotation.copy();
	}

	public static NoTextureRotationConfig config() {
		return config.copy();
	}

	public static void setConfig(NoTextureRotationConfig newConfig) {
		config = newConfig == null ? new NoTextureRotationConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noTextureRotation = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static boolean shouldDisableRandomOffset() {
		return config.enabled && config.disableRandomOffset;
	}

	public static boolean shouldKeepCollisionShapeOffsets() {
		return config.keepCollisionShapeOffsets;
	}
}

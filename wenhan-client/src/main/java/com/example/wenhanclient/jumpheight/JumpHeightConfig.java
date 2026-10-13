package com.example.wenhanclient.jumpheight;

import com.example.wenhanclient.WenhanClientMod;

public final class JumpHeightConfig {
	public static final boolean DEFAULT_ENABLED = false;
	public static final double DEFAULT_TARGET_HEIGHT = 1.25D;
	public static final double MIN_TARGET_HEIGHT = 0.0D;
	public static final double MAX_TARGET_HEIGHT = 256.0D;

	public boolean enabled = DEFAULT_ENABLED;
	public double targetHeight = DEFAULT_TARGET_HEIGHT;

	public static JumpHeightConfig load() {
		return WenhanClientMod.CONFIG.jumpHeight.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.jumpHeight = copy();
		WenhanClientMod.CONFIG.save();
	}

	public JumpHeightConfig copy() {
		JumpHeightConfig copy = new JumpHeightConfig();
		copy.enabled = this.enabled;
		copy.targetHeight = this.targetHeight;
		return copy;
	}

	public static JumpHeightConfig sanitize(JumpHeightConfig config) {
		JumpHeightConfig sanitized = config == null ? new JumpHeightConfig() : config.copy();
		if (!Double.isFinite(sanitized.targetHeight)) {
			sanitized.targetHeight = DEFAULT_TARGET_HEIGHT;
		}
		sanitized.targetHeight =
				Math.max(MIN_TARGET_HEIGHT, Math.min(MAX_TARGET_HEIGHT, sanitized.targetHeight));
		return sanitized;
	}
}

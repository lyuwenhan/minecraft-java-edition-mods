package com.example.wenhanclient.coyote;

import com.example.wenhanclient.WenhanClientMod;

public final class CoyoteSubMod {
	private static CoyoteConfig config = new CoyoteConfig();

	private CoyoteSubMod() {}

	public static void init() {
		config =
				WenhanClientMod.CONFIG.coyote == null
						? new CoyoteConfig()
						: WenhanClientMod.CONFIG.coyote;
		config.sanitize();
		WenhanClientMod.CONFIG.coyote = config.copy();
	}

	public static CoyoteConfig config() {
		return config.copy();
	}

	public static void setConfig(CoyoteConfig newConfig) {
		config = newConfig == null ? new CoyoteConfig() : newConfig.copy();
		config.sanitize();
		WenhanClientMod.CONFIG.coyote = config.copy();
		WenhanClientMod.CONFIG.save();
	}
}

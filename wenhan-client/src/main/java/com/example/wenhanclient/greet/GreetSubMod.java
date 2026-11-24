package com.example.wenhanclient.greet;

import com.example.wenhanclient.WenhanClientMod;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class GreetSubMod {
	public static long joinWorldAt = 0L;
	private static GreetConfig config;

	private GreetSubMod() {}

	public static void init(GreetConfig greetConfig) {
		config = greetConfig;
		config.fillMissing();
		GreetDelay.init();
		ClientPlayConnectionEvents.JOIN.register(
				(handler, sender, client) -> {
					joinWorldAt = System.currentTimeMillis();
					GreetDelay.resetPlayerTracking();
					if (config.selfEnabled) {
						GreetDelay.greetSelfAfter1Second();
					}
				});
	}

	public static GreetConfig config() {
		return config;
	}

	public static void setConfig(GreetConfig newConfig) {
		config = newConfig == null ? new GreetConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.greet = config;
		WenhanClientMod.CONFIG.save();
	}
}

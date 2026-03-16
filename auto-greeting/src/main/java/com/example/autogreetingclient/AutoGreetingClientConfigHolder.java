package com.example.autogreetingclient;

public final class AutoGreetingClientConfigHolder {
	private static AutoGreetingClientConfig config = AutoGreetingClientConfig.load();

	private AutoGreetingClientConfigHolder() {
	}

	public static AutoGreetingClientConfig get() {
		return config;
	}

	public static void set(AutoGreetingClientConfig newConfig) {
		config = newConfig;
	}

	public static void save() {
		config.save();
	}

	public static void reload() {
		config = AutoGreetingClientConfig.load();
	}
}
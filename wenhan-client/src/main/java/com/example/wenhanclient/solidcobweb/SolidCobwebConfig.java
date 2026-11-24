package com.example.wenhanclient.solidcobweb;

public final class SolidCobwebConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public SolidCobwebConfig copy() {
		SolidCobwebConfig copy = new SolidCobwebConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

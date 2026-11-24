package com.example.wenhanclient.safedrop;

import com.example.wenhanclient.WenhanClientMod;

public final class SafeDropConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public static SafeDropConfig load() {
		return WenhanClientMod.CONFIG.safeDrop.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.safeDrop = copy();
		WenhanClientMod.CONFIG.save();
	}

	public SafeDropConfig copy() {
		SafeDropConfig copy = new SafeDropConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

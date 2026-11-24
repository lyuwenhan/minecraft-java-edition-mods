package com.example.wenhanclient.minecartdirection;

public final class MinecartDirectionConfig {
	public static final boolean DEFAULT_KEEP_DIRECTION_IN_MINECART = false;

	public boolean keepDirectionInMinecart = DEFAULT_KEEP_DIRECTION_IN_MINECART;

	public MinecartDirectionConfig copy() {
		MinecartDirectionConfig copy = new MinecartDirectionConfig();
		copy.keepDirectionInMinecart = this.keepDirectionInMinecart;
		return copy;
	}
}

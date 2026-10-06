package com.example.wenhanclient.minecartdirection;

public final class MinecartDirectionConfig {
	public boolean keepDirectionInMinecart = false;

	public MinecartDirectionConfig copy() {
		MinecartDirectionConfig copy = new MinecartDirectionConfig();
		copy.keepDirectionInMinecart = this.keepDirectionInMinecart;
		return copy;
	}
}

package com.example.wenhanclient.clientflying;

import com.example.wenhanclient.WenhanClientMod;

public class ClientFlyingConfig {
	public boolean enabled = false;

	public static ClientFlyingConfig load() {
		return WenhanClientMod.CONFIG.clientFlying.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.clientFlying = copy();
		WenhanClientMod.CONFIG.save();
	}

	public ClientFlyingConfig copy() {
		ClientFlyingConfig c = new ClientFlyingConfig();
		c.enabled = this.enabled;
		return c;
	}
}

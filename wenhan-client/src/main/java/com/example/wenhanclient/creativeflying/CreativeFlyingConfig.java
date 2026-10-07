package com.example.wenhanclient.creativeflying;

import com.example.wenhanclient.WenhanClientMod;

public class CreativeFlyingConfig {
	public boolean enabled = false;

	public static CreativeFlyingConfig load() {
		return WenhanClientMod.CONFIG.creativeFlying.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.creativeFlying = copy();
		WenhanClientMod.CONFIG.save();
	}

	public CreativeFlyingConfig copy() {
		CreativeFlyingConfig c = new CreativeFlyingConfig();
		c.enabled = this.enabled;
		return c;
	}
}

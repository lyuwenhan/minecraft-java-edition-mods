package com.example.wenhanclient.whoiam;

import com.example.wenhanclient.WenhanClientMod;

public final class WhoIAmConfig {
	public boolean showLocalPlayerName = false;
	public boolean appendPlayerNameToTitle = false;

	public static WhoIAmConfig load() {
		return WenhanClientMod.CONFIG.whoIAm.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.whoIAm = copy();
		WenhanClientMod.CONFIG.save();
	}

	public WhoIAmConfig copy() {
		WhoIAmConfig copy = new WhoIAmConfig();
		copy.showLocalPlayerName = this.showLocalPlayerName;
		copy.appendPlayerNameToTitle = this.appendPlayerNameToTitle;
		return copy;
	}
}

package com.example.wenhanclient.whoiam;

import com.example.wenhanclient.WenhanClientMod;

public final class WhoIAmConfig {
	public static final boolean DEFAULT_SHOW_LOCAL_PLAYER_NAME = false;
	public static final boolean DEFAULT_APPEND_PLAYER_NAME_TO_TITLE = false;

	public boolean showLocalPlayerName = DEFAULT_SHOW_LOCAL_PLAYER_NAME;
	public boolean appendPlayerNameToTitle = DEFAULT_APPEND_PLAYER_NAME_TO_TITLE;

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

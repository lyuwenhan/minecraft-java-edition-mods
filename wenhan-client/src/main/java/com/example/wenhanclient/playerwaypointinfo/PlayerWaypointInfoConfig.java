package com.example.wenhanclient.playerwaypointinfo;

import com.example.wenhanclient.WenhanClientMod;

public class PlayerWaypointInfoConfig {
	public static final boolean DEFAULT_ENABLED = false;

	public boolean enabled = DEFAULT_ENABLED;

	public static PlayerWaypointInfoConfig load() {
		return WenhanClientMod.CONFIG.playerWaypointInfo.copy();
	}

	public PlayerWaypointInfoConfig copy() {
		PlayerWaypointInfoConfig copy = new PlayerWaypointInfoConfig();
		copy.enabled = this.enabled;
		return copy;
	}
}

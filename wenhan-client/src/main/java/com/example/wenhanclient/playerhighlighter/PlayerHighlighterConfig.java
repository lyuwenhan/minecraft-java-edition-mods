package com.example.wenhanclient.playerhighlighter;

import com.example.wenhanclient.WenhanClientMod;

public class PlayerHighlighterConfig {
	public static final boolean DEFAULT_ENABLED = false;
	public static final boolean DEFAULT_KEEP = false;
	public static final Boolean DEFAULT_INFORMATION_HUD = true;

	public boolean enabled = DEFAULT_ENABLED;
	public boolean keep = DEFAULT_KEEP;
	public Boolean informationHud = DEFAULT_INFORMATION_HUD;

	public static PlayerHighlighterConfig load() {
		return WenhanClientMod.CONFIG.playerHighlighter.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.playerHighlighter = copy();
		WenhanClientMod.CONFIG.save();
	}

	public PlayerHighlighterConfig copy() {
		PlayerHighlighterConfig copy = new PlayerHighlighterConfig();
		copy.enabled = this.enabled;
		copy.keep = this.keep;
		copy.informationHud = this.informationHud;
		return copy;
	}
}

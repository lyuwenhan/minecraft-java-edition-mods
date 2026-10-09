package com.example.wenhanclient.playerhighlighter;

import com.example.wenhanclient.WenhanClientMod;

public class PlayerHighlighterConfig {
	public boolean enabled = false;
	public boolean keep = false;
	public Boolean informationHud = true;

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

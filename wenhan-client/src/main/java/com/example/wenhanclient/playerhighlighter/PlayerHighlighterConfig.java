package com.example.wenhanclient.playerhighlighter;

import com.example.wenhanclient.WenhanClientMod;

public class PlayerHighlighterConfig {
	public static final boolean DEFAULT_ENABLED = false;
	public static final boolean DEFAULT_KEEP = false;

	public boolean enabled = DEFAULT_ENABLED;
	public boolean keep = DEFAULT_KEEP;
	public transient PlayerHighlightMode mode;

	public static PlayerHighlighterConfig load() {
		return WenhanClientMod.CONFIG.playerHighlighter.copy();
	}

	public void save() {
		applyModeToLegacy();
		WenhanClientMod.CONFIG.playerHighlighter = copy();
		WenhanClientMod.CONFIG.save();
	}

	public PlayerHighlightMode mode() {
		return mode == null ? PlayerHighlightMode.fromLegacy(enabled, keep) : mode;
	}

	public void setMode(PlayerHighlightMode mode) {
		this.mode = mode == null ? PlayerHighlightMode.DISABLED : mode;
		this.mode.applyTo(this);
	}

	public void applyModeToLegacy() {
		setMode(mode());
	}

	public PlayerHighlighterConfig copy() {
		PlayerHighlighterConfig copy = new PlayerHighlighterConfig();
		copy.setMode(mode());
		return copy;
	}
}

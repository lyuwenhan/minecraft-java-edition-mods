package com.example.wenhanclient.playerhighlighter;

public enum PlayerHighlightMode {
	DISABLED,
	HOLD,
	ALWAYS;

	public static PlayerHighlightMode fromLegacy(boolean enabled, boolean keep) {
		if (!enabled) {
			return DISABLED;
		}
		return keep ? ALWAYS : HOLD;
	}

	public void applyTo(PlayerHighlighterConfig config) {
		config.enabled = this != DISABLED;
		config.keep = this == ALWAYS;
	}
}

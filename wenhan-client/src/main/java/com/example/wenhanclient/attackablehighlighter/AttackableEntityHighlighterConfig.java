package com.example.wenhanclient.attackablehighlighter;

public final class AttackableEntityHighlighterConfig {
	public static final boolean DEFAULT_ENABLED = false;
	public static final int DEFAULT_COLOR = 0xFFFFFF;

	public boolean enabled = DEFAULT_ENABLED;
	public int color = DEFAULT_COLOR;

	public void sanitize() {
		color &= 0xFFFFFF;
	}

	public AttackableEntityHighlighterConfig copy() {
		AttackableEntityHighlighterConfig copy = new AttackableEntityHighlighterConfig();
		copy.enabled = this.enabled;
		copy.color = this.color;
		copy.sanitize();
		return copy;
	}
}

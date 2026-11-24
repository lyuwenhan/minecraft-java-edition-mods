package com.example.wenhanclient.entityhighlighter;

public final class TypeHighlightRule {
	public boolean enabled;
	public int color = EntityHighlighterSubMod.DEFAULT_COLOR;

	public TypeHighlightRule() {}

	public TypeHighlightRule(boolean enabled, int color) {
		this.enabled = enabled;
		this.color = color & 0xFFFFFF;
	}

	public TypeHighlightRule copy() {
		return new TypeHighlightRule(enabled, color);
	}
}

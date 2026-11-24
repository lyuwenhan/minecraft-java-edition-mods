package com.example.wenhanclient.entityhighlighter;

public final class GroupHighlightRule {
	public boolean enabled;
	public int color = EntityHighlighterSubMod.DEFAULT_COLOR;

	public GroupHighlightRule() {}

	public GroupHighlightRule(boolean enabled, int color) {
		this.enabled = enabled;
		this.color = color & 0xFFFFFF;
	}

	public GroupHighlightRule copy() {
		return new GroupHighlightRule(enabled, color);
	}
}

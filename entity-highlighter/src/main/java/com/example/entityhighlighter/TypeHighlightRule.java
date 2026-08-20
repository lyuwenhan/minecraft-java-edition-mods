package com.example.entityhighlighter;

public final class TypeHighlightRule {
	public String entityId;
	public int color;

	public TypeHighlightRule() {}

	public TypeHighlightRule(String entityId, int color) {
		this.entityId = entityId;
		this.color = color;
	}
}

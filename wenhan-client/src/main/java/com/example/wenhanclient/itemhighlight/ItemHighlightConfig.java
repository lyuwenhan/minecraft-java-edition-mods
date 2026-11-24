package com.example.wenhanclient.itemhighlight;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ItemHighlightConfig {
	public static final boolean DEFAULT_ENABLED = false;
	public static final int DEFAULT_COLOR = 0xFFFFFF;
	public boolean enabled = DEFAULT_ENABLED;
	public int color = DEFAULT_COLOR;
	public Set<String> blacklist = new LinkedHashSet<>();

	public ItemHighlightConfig copy() {
		ItemHighlightConfig copy = new ItemHighlightConfig();
		copy.enabled = this.enabled;
		copy.color = this.color;
		copy.blacklist = blacklist == null ? new LinkedHashSet<>() : new LinkedHashSet<>(blacklist);
		copy.fillMissing();
		return copy;
	}

	public void fillMissing() {
		color &= 0xFFFFFF;
		Set<String> normalized = new LinkedHashSet<>();
		if (blacklist != null) {
			for (String value : blacklist) {
				Identifier id = value == null ? null : Identifier.tryParse(value);
				if (id != null) {
					normalized.add(id.toString());
				}
			}
		}
		blacklist = normalized;
	}
}

package com.example.wenhanclient.entityhighlighter;

import com.example.wenhanclient.WenhanClientMod;

import java.util.ArrayList;
import java.util.List;

public final class EntityHighlighterConfig {
	public List<TypeHighlightRule> typeRules = new ArrayList<>();
	public List<GroupHighlightRule> groupRules = new ArrayList<>();

	public static EntityHighlighterConfig load() {
		EntityHighlighterConfig config = WenhanClientMod.CONFIG.entityHighlighter;
		if (config == null) {
			config = new EntityHighlighterConfig();
			WenhanClientMod.CONFIG.entityHighlighter = config;
		}
		config.fillMissing();
		return config;
	}

	public void save() {
		fillMissing();
		WenhanClientMod.CONFIG.entityHighlighter = this;
		WenhanClientMod.CONFIG.save();
	}

	public void fillMissing() {
		if (typeRules == null) {
			typeRules = new ArrayList<>();
		}
		if (groupRules == null) {
			groupRules = new ArrayList<>();
		}
	}
}

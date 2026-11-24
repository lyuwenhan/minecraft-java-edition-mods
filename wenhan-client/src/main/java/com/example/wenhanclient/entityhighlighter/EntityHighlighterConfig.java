package com.example.wenhanclient.entityhighlighter;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityHighlighterConfig {
	public static final String ATTACKABLE_GROUP = "attackable";
	private static final String DROPPED_ITEM_TYPE = "minecraft:item";

	public Map<String, TypeHighlightRule> typeRules = createTypeRules();
	public Map<String, GroupHighlightRule> groupRules = createGroupRules();

	public EntityHighlighterConfig copy() {
		EntityHighlighterConfig copy = new EntityHighlighterConfig();
		copy.typeRules.clear();
		if (typeRules != null) {
			typeRules.forEach(
					(key, rule) -> {
						if (key != null && rule != null) {
							copy.typeRules.put(key, rule.copy());
						}
					});
		}
		copy.groupRules.clear();
		if (groupRules != null) {
			groupRules.forEach(
					(key, rule) -> {
						if (key != null && rule != null) {
							copy.groupRules.put(key, rule.copy());
						}
					});
		}
		return copy;
	}

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
		Map<String, TypeHighlightRule> loaded = typeRules;
		Map<String, TypeHighlightRule> ordered = createTypeRules();
		if (loaded != null) {
			ordered.replaceAll(
					(key, defaultRule) -> {
						TypeHighlightRule loadedRule = loaded.get(key);
						return loadedRule == null ? defaultRule : loadedRule.copy();
					});
		}
		typeRules = ordered;
		Map<String, GroupHighlightRule> loadedGroups = groupRules;
		Map<String, GroupHighlightRule> orderedGroups = createGroupRules();
		if (loadedGroups != null) {
			orderedGroups.replaceAll(
					(key, defaultRule) -> {
						GroupHighlightRule loadedRule = loadedGroups.get(key);
						return loadedRule == null ? defaultRule : loadedRule.copy();
					});
		}
		groupRules = orderedGroups;
	}

	private static Map<String, TypeHighlightRule> createTypeRules() {
		Map<String, TypeHighlightRule> rules = new LinkedHashMap<>();
		BuiltInRegistries.ENTITY_TYPE.keySet().stream()
				.map(Object::toString)
				.filter(id -> !DROPPED_ITEM_TYPE.equals(id))
				.sorted(Comparator.naturalOrder())
				.forEach(id -> rules.put(id, new TypeHighlightRule()));
		return rules;
	}

	private static Map<String, GroupHighlightRule> createGroupRules() {
		Map<String, GroupHighlightRule> rules = new LinkedHashMap<>();
		rules.put(ATTACKABLE_GROUP, new GroupHighlightRule());
		rules.put("animal", new GroupHighlightRule());
		rules.put("monster", new GroupHighlightRule());
		rules.put("everything", new GroupHighlightRule());
		return rules;
	}
}

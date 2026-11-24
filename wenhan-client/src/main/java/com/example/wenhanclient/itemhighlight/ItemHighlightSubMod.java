package com.example.wenhanclient.itemhighlight;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public final class ItemHighlightSubMod {
	private static ItemHighlightConfig config = new ItemHighlightConfig();

	private ItemHighlightSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.itemHighlight;
		config.fillMissing();
	}

	public static ItemHighlightConfig config() {
		return config.copy();
	}

	public static void setConfig(ItemHighlightConfig newConfig) {
		config = newConfig == null ? new ItemHighlightConfig() : newConfig.copy();
		save();
	}

	private static void save() {
		WenhanClientMod.CONFIG.itemHighlight = config;
		WenhanClientMod.CONFIG.save();
	}

	public static Integer getHighlightColor(Entity entity) {
		if (!config.enabled || !(entity instanceof ItemEntity item) || item.getItem().isEmpty()) {
			return null;
		}
		String id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
		return config.blacklist.contains(id) ? null : config.color;
	}
}

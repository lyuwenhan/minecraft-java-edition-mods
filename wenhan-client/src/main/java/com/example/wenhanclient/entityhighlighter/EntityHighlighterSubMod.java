package com.example.wenhanclient.entityhighlighter;

import com.example.wenhanclient.itemhighlight.ItemHighlightSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;

public final class EntityHighlighterSubMod {
	public static final String MOD_ID = "entity-highlighter";
	public static final int DEFAULT_COLOR = 0xFFFFFF;

	public static EntityHighlighterConfig config;
	private static final ThreadLocal<Entity> GLOW_OVERRIDE_BYPASS = new ThreadLocal<>();

	private EntityHighlighterSubMod() {}

	public static void init() {
		config = EntityHighlighterConfig.load();
	}

	public static EntityHighlighterConfig config() {
		return config == null ? EntityHighlighterConfig.load() : config;
	}

	public static void setConfig(EntityHighlighterConfig newConfig) {
		config = newConfig == null ? new EntityHighlighterConfig() : newConfig.copy();
		config.save();
	}

	public static boolean isGlowOverrideBypassed(Entity entity) {
		return GLOW_OVERRIDE_BYPASS.get() == entity;
	}

	public static boolean isGlowingWithoutEntityHighlighter(Entity entity) {
		Entity previous = GLOW_OVERRIDE_BYPASS.get();
		GLOW_OVERRIDE_BYPASS.set(entity);
		try {
			return entity.isCurrentlyGlowing();
		} finally {
			if (previous == null) {
				GLOW_OVERRIDE_BYPASS.remove();
			} else {
				GLOW_OVERRIDE_BYPASS.set(previous);
			}
		}
	}

	public static Integer getHighlightColor(Entity entity) {
		if (entity instanceof ItemEntity) {
			return ItemHighlightSubMod.getHighlightColor(entity);
		}
		if (getAttackableHighlightColor(entity) != null) {
			return null;
		}
		EntityHighlighterConfig currentConfig = config;
		if (currentConfig == null) {
			return null;
		}

		boolean hasTypeRules =
				currentConfig.typeRules != null
						&& currentConfig.typeRules.values().stream().anyMatch(rule -> rule.enabled);
		boolean hasGroupRules =
				currentConfig.groupRules != null
						&& currentConfig.groupRules.values().stream()
								.anyMatch(rule -> rule.enabled);
		if (!hasTypeRules && !hasGroupRules) {
			return null;
		}

		Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		if (entityId != null && currentConfig.typeRules != null) {
			TypeHighlightRule rule = currentConfig.typeRules.get(entityId.toString());
			if (rule != null && rule.enabled) {
				return rule.color & 0xFFFFFF;
			}
		}

		if (currentConfig.groupRules != null) {
			for (var entry : currentConfig.groupRules.entrySet()) {
				GroupHighlightRule rule = entry.getValue();
				if (rule != null && rule.enabled && matchesGroupRule(entity, entry.getKey())) {
					return rule.color & 0xFFFFFF;
				}
			}
		}

		return null;
	}

	public static Integer getAttackableHighlightColor(Entity entity) {
		GroupHighlightRule rule = config().groupRules.get(EntityHighlighterConfig.ATTACKABLE_GROUP);
		if (rule == null || !rule.enabled || !isWithinAttackRange(entity)) {
			return null;
		}
		return rule.color & 0xFFFFFF;
	}

	private static boolean isWithinAttackRange(Entity entity) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null
				|| client.player == null
				|| client.gameMode == null
				|| client.gameMode.isSpectator()
				|| !(entity instanceof LivingEntity living)
				|| living == client.player
				|| living.level() != client.level
				|| !living.isAlive()
				|| !living.isAttackable()
				|| !living.isPickable()) {
			return false;
		}

		return client.player.isWithinAttackRange(
				client.player.getMainHandItem(), living.getBoundingBox(), 0.0D);
	}

	private static boolean matchesGroupRule(Entity entity, String groupName) {
		MobCategory category = entity.getType().getCategory();
		return switch (groupName) {
			case "animal" -> isAnimalCategory(category);
			case "monster" -> category == MobCategory.MONSTER;
			case "everything" -> true;
			default -> false;
		};
	}

	private static boolean isAnimalCategory(MobCategory category) {
		return category == MobCategory.CREATURE
				|| category == MobCategory.AMBIENT
				|| category == MobCategory.AXOLOTLS
				|| category == MobCategory.UNDERGROUND_WATER_CREATURE
				|| category == MobCategory.WATER_AMBIENT
				|| category == MobCategory.WATER_CREATURE;
	}
}

package com.example.wenhanclient.attackablehighlighter;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class AttackableEntityHighlighterSubMod {
	private static AttackableEntityHighlighterConfig config =
			new AttackableEntityHighlighterConfig();

	private AttackableEntityHighlighterSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.attackableEntityHighlighter.copy();
	}

	public static AttackableEntityHighlighterConfig config() {
		return config.copy();
	}

	public static void setConfig(AttackableEntityHighlighterConfig newConfig) {
		config = newConfig == null ? new AttackableEntityHighlighterConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.attackableEntityHighlighter = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static Integer getHighlightColor(Entity entity) {
		return isWithinAttackRange(entity) ? config.color : null;
	}

	private static boolean isWithinAttackRange(Entity entity) {
		Minecraft client = Minecraft.getInstance();
		if (!config.enabled
				|| client.level == null
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
}

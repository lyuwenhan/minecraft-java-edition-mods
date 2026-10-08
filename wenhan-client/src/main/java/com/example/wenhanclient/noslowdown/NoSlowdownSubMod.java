package com.example.wenhanclient.noslowdown;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

public final class NoSlowdownSubMod {
	private static NoSlowdownConfig config = new NoSlowdownConfig();

	private NoSlowdownSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noSlowdown.copy();
	}

	public static NoSlowdownConfig config() {
		return config.copy();
	}

	public static void setConfig(NoSlowdownConfig newConfig) {
		config = newConfig == null ? new NoSlowdownConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noSlowdown = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static boolean shouldCancelBlockSlowdown(Entity entity) {
		if (!config.enabled || entity == null || Minecraft.getInstance().player == null) {
			return false;
		}
		return entity == Minecraft.getInstance().player || entity instanceof LocalPlayer;
	}

	public static boolean shouldCancel(ItemStack stack) {
		if (!config.enabled || stack == null || stack.isEmpty()) {
			return false;
		}

		ItemUseAnimation animation = stack.getUseAnimation();
		return animation == ItemUseAnimation.BLOCK
				|| animation == ItemUseAnimation.BOW
				|| animation == ItemUseAnimation.CROSSBOW
				|| animation == ItemUseAnimation.TRIDENT
				|| animation == ItemUseAnimation.SPEAR
				|| animation == ItemUseAnimation.SPYGLASS
				|| animation == ItemUseAnimation.TOOT_HORN
				|| animation == ItemUseAnimation.EAT
				|| animation == ItemUseAnimation.DRINK;
	}
}

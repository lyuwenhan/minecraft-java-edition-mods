package com.example.wenhanclient.cancelslowdown;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

public final class CancelSlowdownSubMod {
	private static CancelSlowdownConfig config = new CancelSlowdownConfig();

	private CancelSlowdownSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.cancelSlowdown.copy();
	}

	public static CancelSlowdownConfig config() {
		return config.copy();
	}

	public static void setConfig(CancelSlowdownConfig newConfig) {
		config = newConfig == null ? new CancelSlowdownConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.cancelSlowdown = config.copy();
		WenhanClientMod.CONFIG.save();
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

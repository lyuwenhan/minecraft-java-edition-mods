package com.example.wenhanclient.noslowfalling;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class NoSlowFallingSubMod {
	private static NoSlowFallingConfig config = new NoSlowFallingConfig();

	private NoSlowFallingSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noSlowFalling.copy();
	}

	public static NoSlowFallingConfig config() {
		return config.copy();
	}

	public static void setConfig(NoSlowFallingConfig newConfig) {
		config = newConfig == null ? new NoSlowFallingConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noSlowFalling = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static boolean shouldCancel(LivingEntity entity, Holder<MobEffect> effect) {
		if (!config.enabled || entity == null || effect != MobEffects.SLOW_FALLING) {
			return false;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		return entity == player || entity instanceof LocalPlayer;
	}
}

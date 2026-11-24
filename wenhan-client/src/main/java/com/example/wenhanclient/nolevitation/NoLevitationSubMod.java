package com.example.wenhanclient.nolevitation;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class NoLevitationSubMod {
	private static NoLevitationConfig config = new NoLevitationConfig();

	private NoLevitationSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noLevitation.copy();
	}

	public static NoLevitationConfig config() {
		return config.copy();
	}

	public static void setConfig(NoLevitationConfig newConfig) {
		config = newConfig == null ? new NoLevitationConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noLevitation = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static boolean shouldCancel(LivingEntity entity, Holder<MobEffect> effect) {
		if (!config.enabled || entity == null || effect != MobEffects.LEVITATION) {
			return false;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		return entity == player || entity instanceof LocalPlayer;
	}
}

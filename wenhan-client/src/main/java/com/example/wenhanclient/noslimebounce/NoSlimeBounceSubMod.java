package com.example.wenhanclient.noslimebounce;

import com.example.wenhanclient.WenhanClientMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class NoSlimeBounceSubMod {
	private static NoSlimeBounceConfig config = new NoSlimeBounceConfig();

	private NoSlimeBounceSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.noSlimeBounce.copy();
	}

	public static NoSlimeBounceConfig config() {
		return config.copy();
	}

	public static void setConfig(NoSlimeBounceConfig newConfig) {
		config = newConfig == null ? new NoSlimeBounceConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.noSlimeBounce = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	public static boolean shouldCancelBounce(Entity entity) {
		Minecraft minecraft = Minecraft.getInstance();
		return config.enabled
				&& entity != null
				&& minecraft.player != null
				&& entity == minecraft.player;
	}
}

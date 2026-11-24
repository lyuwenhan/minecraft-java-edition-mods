package com.example.wenhanclient.minecartdirection;

import com.example.wenhanclient.WenhanClientMod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public final class MinecartDirectionSubMod {
	private static MinecartDirectionConfig config = new MinecartDirectionConfig();
	private static int lastMinecartId = -1;
	private static float lastMinecartYaw;

	private MinecartDirectionSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.minecartDirection.copy();
		ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client.player));
	}

	public static MinecartDirectionConfig config() {
		return config.copy();
	}

	public static void setConfig(MinecartDirectionConfig newConfig) {
		config = newConfig == null ? new MinecartDirectionConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.minecartDirection = config.copy();
		WenhanClientMod.CONFIG.save();
	}

	private static void tick(LocalPlayer player) {
		if (player == null || !config.keepDirectionInMinecart) {
			resetTracking();
			return;
		}

		Entity vehicle = player.getVehicle();
		if (!(vehicle instanceof AbstractMinecart minecart)) {
			resetTracking();
			return;
		}

		if (lastMinecartId != minecart.getId()) {
			lastMinecartId = minecart.getId();
			lastMinecartYaw = minecart.getYRot();
			return;
		}

		float currentYaw = minecart.getYRot();
		float yawDelta = Mth.wrapDegrees(currentYaw - lastMinecartYaw);
		lastMinecartYaw = currentYaw;
		if (yawDelta == 0.0F) {
			return;
		}

		player.setYRot(player.getYRot() + yawDelta);
		player.yRotO += yawDelta;
		player.setYHeadRot(player.getYHeadRot() + yawDelta);
		player.setYBodyRot(player.getYRot());
	}

	private static void resetTracking() {
		lastMinecartId = -1;
		lastMinecartYaw = 0.0F;
	}
}

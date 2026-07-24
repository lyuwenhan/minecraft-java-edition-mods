package com.example.boatutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public final class BoatUtilsMod implements ClientModInitializer {
	public static final String MOD_ID = "boat-utils";

	@Override
	public void onInitializeClient() {
		BoatUtilsConfig.load();
		ClientTickEvents.START_CLIENT_TICK.register(BoatUtilsMod::applyFollowView);
		ClientTickEvents.END_CLIENT_TICK.register(BoatUtilsMod::applyFollowView);
	}

	private static void applyFollowView(Minecraft client) {
		if (!BoatUtilsConfig.viewDirectionLockEnabled()) {
			return;
		}
		if (client.player == null) {
			return;
		}

		Entity vehicle = client.player.getVehicle();
		if (!(vehicle instanceof AbstractBoat boat)) {
			return;
		}

		if (boat.getControllingPassenger() != client.player) {
			return;
		}

		float viewYaw = client.player.getYRot();
		boat.setYRot(viewYaw);
		boat.setYBodyRot(viewYaw);
	}
}

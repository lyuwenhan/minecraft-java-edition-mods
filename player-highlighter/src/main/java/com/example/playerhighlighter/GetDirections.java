package com.example.playerhighlighter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class GetDirections {
	private GetDirections() {
	}

	public record ScreenResult(float x, float y) {
	}

	public static List<ScreenResult> projectAllPlayersHud(int screenWidth, int screenHeight) {
		List<ScreenResult> result = new ArrayList<>();

		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return result;
		}

		Entity camera = client.getCameraEntity();
		if (camera == null) {
			return result;
		}

		GameRenderer renderer = client.gameRenderer;
		Vec3 camPos = camera.position();
		Vec3 camForward = camera.getViewVector(1.0F);

		float screenHalfWidth = screenWidth * 0.5F;
		float screenHalfHeight = screenHeight * 0.5F;

		for (var player : client.level.players()) {
			if (player == camera) {
				continue;
			}

			double px = player.getX();
			double py = player.getEyeY();
			double pz = player.getZ();

			double dx = px - camPos.x;
			double dy = py - camPos.y;
			double dz = pz - camPos.z;

			if (camForward.x * dx + camForward.y * dy + camForward.z * dz <= 0.0D) {
				continue;
			}

			Vec3 projected = renderer.projectPointToScreen(new Vec3(px, py, pz));
			if (projected == null) {
				continue;
			}

			if (!Double.isFinite(projected.x) || !Double.isFinite(projected.y)) {
				continue;
			}

			float x = (float) ((projected.x + 1.0D) * screenHalfWidth);
			float y = (float) ((1.0D - projected.y) * screenHalfHeight);

			if (x < 0.0F || x > screenWidth || y < 0.0F || y > screenHeight) {
				continue;
			}

			result.add(new ScreenResult(x, y));
		}

		return result;
	}
}

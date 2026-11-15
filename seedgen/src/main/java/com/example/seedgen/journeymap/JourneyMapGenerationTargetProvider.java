package com.example.seedgen.journeymap;

import com.example.seedgen.core.GenerationTarget;

import journeymap.client.ui.fullscreen.Fullscreen;

import net.minecraft.client.Minecraft;

final class JourneyMapGenerationTargetProvider {
	private static final int GENERATION_UNIT_BLOCKS = 16 * 16;

	private JourneyMapGenerationTargetProvider() {}

	static GenerationTarget getTarget(Minecraft client, boolean enabled) {
		if (!enabled
				|| client.level == null
				|| !(client.gui.screen() instanceof Fullscreen mapScreen)
				|| mapScreen.getMapType() == null
				|| !client.level.dimension().equals(mapScreen.getMapType().dimension)) {
			return GenerationTarget.inactive();
		}
		return new GenerationTarget(
				Math.floorDiv(
						(int) Math.floor(mapScreen.getCenterBlockX(true)), GENERATION_UNIT_BLOCKS),
				Math.floorDiv(
						(int) Math.floor(mapScreen.getCenterBlockZ(true)), GENERATION_UNIT_BLOCKS),
				1);
	}
}

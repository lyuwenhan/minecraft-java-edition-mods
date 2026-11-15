package com.example.seedgen.xaero;

import com.example.seedgen.core.GenerationTarget;

import net.minecraft.client.Minecraft;

public final class XaeroWorldMapGenerationTargetProvider {
	private static final int GENERATION_UNIT_BLOCKS = 16 * 16;

	private XaeroWorldMapGenerationTargetProvider() {}

	public static GenerationTarget getTarget(Minecraft client, boolean enabled) {
		if (!enabled
				|| client.level == null
				|| !(client.gui.screen() instanceof XaeroMapScreenAccess mapScreen)
				|| !client.level.dimension().equals(mapScreen.seedgen$getViewedDimension())) {
			return GenerationTarget.inactive();
		}
		return new GenerationTarget(
				Math.floorDiv(
						(int) Math.floor(mapScreen.seedgen$getCameraX()), GENERATION_UNIT_BLOCKS),
				Math.floorDiv(
						(int) Math.floor(mapScreen.seedgen$getCameraZ()), GENERATION_UNIT_BLOCKS),
				1);
	}
}

package com.example.seedgen.freecam;

import com.example.seedgen.core.GenerationTarget;

import net.minecraft.client.Minecraft;

public final class FreecamGenerationTargetProvider {
	private static final int GENERATION_UNIT_BLOCKS = 16 * 16;

	private FreecamGenerationTargetProvider() {}

	public static GenerationTarget getTarget(Minecraft client, boolean enabled) {
		if (!enabled || !client.gameRenderer.mainCamera().isInitialized()) {
			return GenerationTarget.inactive();
		}
		var position = client.gameRenderer.mainCamera().position();
		return new GenerationTarget(
				Math.floorDiv((int) Math.floor(position.x()), GENERATION_UNIT_BLOCKS),
				Math.floorDiv((int) Math.floor(position.z()), GENERATION_UNIT_BLOCKS),
				1);
	}
}

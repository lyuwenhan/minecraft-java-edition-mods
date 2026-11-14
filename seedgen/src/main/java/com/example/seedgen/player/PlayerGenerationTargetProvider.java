package com.example.seedgen.player;

import com.example.seedgen.core.GenerationTarget;

import net.minecraft.client.Minecraft;

public final class PlayerGenerationTargetProvider {
	private static final int GENERATION_UNIT_CHUNKS = 16;

	private PlayerGenerationTargetProvider() {}

	public static GenerationTarget getTarget(Minecraft client, int radiusInChunks) {
		if (client.player == null) {
			return GenerationTarget.inactive();
		}
		int unitX = Math.floorDiv(client.player.chunkPosition().x(), GENERATION_UNIT_CHUNKS);
		int unitZ = Math.floorDiv(client.player.chunkPosition().z(), GENERATION_UNIT_CHUNKS);
		int unitRadius =
				Math.floorDiv(
						Math.max(0, radiusInChunks) + GENERATION_UNIT_CHUNKS - 1,
						GENERATION_UNIT_CHUNKS);
		return new GenerationTarget(unitX, unitZ, unitRadius);
	}
}

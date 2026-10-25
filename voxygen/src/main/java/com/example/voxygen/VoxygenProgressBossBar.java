package com.example.voxygen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

import java.util.UUID;

/** Maintains a client-only boss bar through the vanilla boss overlay packet API. */
final class VoxygenProgressBossBar {
	private static final long COMPLETION_DISPLAY_NANOS = 2_000_000_000L;
	private static final UUID EVENT_ID = UUID.fromString("f884cc78-3a7c-4d4d-b823-10bfafefbb34");
	private static final ServerBossEvent EVENT =
			new ServerBossEvent(
					EVENT_ID,
					Component.literal("Voxygen"),
					BossEvent.BossBarColor.YELLOW,
					BossEvent.BossBarOverlay.PROGRESS);
	private static boolean visible;
	private static float displayedProgress = -1.0F;
	private static Object completedSession;
	private static long hideAtNanos;

	private VoxygenProgressBossBar() {}

	static void tick(Minecraft client, boolean enabled) {
		VoxygenGenerationService.ProgressSnapshot snapshot =
				VoxygenGenerationService.progressSnapshot();
		if (!enabled || client.level == null || snapshot == null) {
			hide(client);
			completedSession = null;
			hideAtNanos = 0L;
			return;
		}

		long now = System.nanoTime();
		if (!snapshot.complete()) {
			completedSession = null;
			hideAtNanos = 0L;
		} else if (completedSession != snapshot.sessionKey()) {
			completedSession = snapshot.sessionKey();
			hideAtNanos = now + COMPLETION_DISPLAY_NANOS;
		} else if (now >= hideAtNanos) {
			hide(client);
			return;
		}

		float progress = Math.max(0.0F, Math.min(1.0F, snapshot.progress()));
		EVENT.setProgress(progress);
		if (!visible) {
			client.getConnection()
					.handleBossUpdate(ClientboundBossEventPacket.createAddPacket(EVENT));
			visible = true;
			displayedProgress = progress;
		} else if (Float.compare(displayedProgress, progress) != 0) {
			client.getConnection()
					.handleBossUpdate(ClientboundBossEventPacket.createUpdateProgressPacket(EVENT));
			displayedProgress = progress;
		}
	}

	static void hide(Minecraft client) {
		if (!visible) {
			return;
		}
		if (client.getConnection() != null) {
			client.getConnection()
					.handleBossUpdate(ClientboundBossEventPacket.createRemovePacket(EVENT_ID));
		}
		visible = false;
		displayedProgress = -1.0F;
	}
}

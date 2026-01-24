package com.example.autogreeting;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AutoGreetingDelay {
	private static PendingSelfGreeting selfPending = null;
	private static final Map<String, PendingGreeting> pending = new HashMap<>();
	private static boolean registered = false;

	public static void greetAfter1Second(String playerName, String uuid) {
		if (pending.containsKey(uuid)) {
			return;
		}

		pending.put(uuid, new PendingGreeting(playerName, uuid, 20));
		registerIfNeeded();
	}

	private static void registerIfNeeded() {
		if (registered) {
			return;
		}
		registered = true;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}

			if (selfPending != null) {
				selfPending.ticksLeft--;
				if (selfPending.ticksLeft <= 0) {
					sendSelfGreeting(client);
					selfPending = null;
				}
			}

			if (pending.isEmpty()) {
				return;
			}

			Iterator<PendingGreeting> it = pending.values().iterator();
			while (it.hasNext()) {
				PendingGreeting p = it.next();
				p.ticksLeft--;

				if (p.ticksLeft > 0) {
					continue;
				}

				if (!AutoGreetingMod.CONFIG.otherEnabled) {
					it.remove();
					continue;
				}

				for (String msg : AutoGreetingMod.CONFIG.otherGreetings) {
					if (msg == null || msg.isBlank()) {
						continue;
					}

					msg = msg.trim();

					String finalMsg = msg
						.replace("@player", p.playerName)
						.replace("@UUID", p.uuid);

					if (msg.startsWith("/")) {
						client.player.networkHandler.sendChatCommand(finalMsg.substring(1));
					} else {
						client.player.networkHandler.sendChatMessage(finalMsg);
					}
				}

				it.remove();
			}
		});
	}

	private static class PendingGreeting {
		final String playerName;
		final String uuid;
		int ticksLeft;

		PendingGreeting(String playerName, String uuid, int ticksLeft) {
			this.playerName = playerName;
			this.uuid = uuid;
			this.ticksLeft = ticksLeft;
		}
	}

	public static void greetSelfAfter1Second() {
		selfPending = new PendingSelfGreeting(20);
		registerIfNeeded();
	}
	private static class PendingSelfGreeting {
		int ticksLeft;
		PendingSelfGreeting(int ticksLeft) {
			this.ticksLeft = ticksLeft;
		}
	}

	private static String fmt(double v) {
		return BigDecimal.valueOf(v)
			.setScale(3, RoundingMode.HALF_UP)
			.stripTrailingZeros()
			.toPlainString();
	}

	private static void sendSelfGreeting(MinecraftClient client) {
		if (!AutoGreetingMod.CONFIG.selfEnabled) return;
		if (client.player == null) return;

		String playerName = client.player.getName().getString();
		String playerUUID = client.player.getUuid().toString();

		String playerX = fmt(client.player.getX());
		String playerY = fmt(client.player.getY());
		String playerZ = fmt(client.player.getZ());

		String health = fmt(client.player.getHealth());
		String level = Integer.toString(client.player.experienceLevel);

		for (String msg : AutoGreetingMod.CONFIG.selfGreetings) {
			if (msg == null || msg.isBlank()) continue;

			msg = msg.trim();
			String finalMsg = msg
				.replace("@player", playerName)
				.replace("@UUID", playerUUID)
				.replace("@X", playerX)
				.replace("@Y", playerY)
				.replace("@Z", playerZ)
				.replace("@health", health)
				.replace("@level", level);

			if (msg.startsWith("/")) {
				client.player.networkHandler.sendChatCommand(finalMsg.substring(1));
			} else {
				client.player.networkHandler.sendChatMessage(finalMsg);
			}
		}
	}

}

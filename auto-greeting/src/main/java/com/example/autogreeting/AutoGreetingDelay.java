package com.example.autogreeting;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoGreetingDelay {

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
}

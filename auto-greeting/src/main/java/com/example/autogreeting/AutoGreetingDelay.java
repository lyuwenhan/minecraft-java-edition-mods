package com.example.autogreeting;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoGreetingDelay {
	private static final Map<String, Integer> pending = new HashMap<>();
	private static boolean registered = false;

	public static void greetAfter1Second(String playerName) {
		pending.put(playerName, 20);
		registerIfNeeded();
	}

	private static void registerIfNeeded() {
		if (registered) return;
		registered = true;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			Iterator<Map.Entry<String, Integer>> it = pending.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Integer> e = it.next();
				int left = e.getValue() - 1;

				if (left <= 0) {
					if (!AutoGreetingMod.CONFIG.otherEnabled) {
						it.remove();
						continue;
					}

					for (String msg : AutoGreetingMod.CONFIG.otherGreetings) {
						if (msg == null || msg.isBlank()) continue;

						msg = msg.trim();

						String finalMsg = msg.replace("@player", e.getKey());

						if (msg.startsWith("/")) {
							client.player.networkHandler.sendChatCommand(finalMsg.substring(1));
						} else {
							client.player.networkHandler.sendChatMessage(finalMsg);
						}
					}
					it.remove();
				} else {
					e.setValue(left);
				}
			}
		});
	}
}

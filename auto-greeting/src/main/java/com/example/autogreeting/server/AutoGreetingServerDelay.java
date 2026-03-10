package com.example.autogreeting.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoGreetingServerDelay {
	private static final Map<String, PendingGreeting> pending = new HashMap<>();
	private static boolean registered = false;

	public static void greetAfter1Second(ServerPlayerEntity player) {
		String uuid = player.getUuidAsString();
		if (pending.containsKey(uuid)) {
			return;
		}

		pending.put(uuid, new PendingGreeting(
			player.getName().getString(),
			uuid,
			20
		));
		registerIfNeeded();
	}

	private static void registerIfNeeded() {
		if (registered) {
			return;
		}
		registered = true;

		ServerTickEvents.END_SERVER_TICK.register(server -> {
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

				if (!AutoGreetingServerMod.CONFIG.enabled) {
					it.remove();
					continue;
				}

				ServerPlayerEntity player = server.getPlayerManager().getPlayer(p.uuid);
				if (player == null) {
					it.remove();
					continue;
				}

				sendConfiguredGreetings(server, player, p.playerName, p.uuid);
				it.remove();
			}
		});
	}

	private static void sendConfiguredGreetings(
		MinecraftServer server,
		ServerPlayerEntity player,
		String playerName,
		String uuid
	) {
		for (String msg : AutoGreetingServerMod.CONFIG.greetings) {
			if (msg == null || msg.isBlank()) {
				continue;
			}

			msg = msg.trim();

			String finalMsg = msg
				.replace("@player", playerName)
				.replace("@UUID", uuid);

			if (msg.startsWith("/")) {
				ServerCommandSource source = player.getCommandSource();
				server.getCommandManager().parseAndExecute(source, finalMsg);
			} else {
				server.getPlayerManager().broadcast(
					Text.translatable("chat.type.text", player.getDisplayName(), Text.literal(finalMsg)),
					false
				);
			}
		}
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
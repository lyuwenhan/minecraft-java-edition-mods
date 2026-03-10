package com.example.autogreeting.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AutoGreetingServerDelay {
	private static final Map<String, PendingGreeting> pending = new HashMap<>();
	private static boolean registered = false;

	public static void greetAfter1Second(ServerPlayerEntity player) {
		String name = player.getName().getString();
		String uuid = player.getUuidAsString();
		String x = fmt(player.getX());
		String y = fmt(player.getY());
		String z = fmt(player.getZ());
		String health = fmt(player.getHealth());
		String level = Integer.toString(player.experienceLevel);
		if (pending.containsKey(uuid)) {
			return;
		}

		pending.put(uuid, new PendingGreeting(name, uuid, x, y, z, health, level, 20));
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

				if (!AutoGreetingServerMod.CONFIG.serverEnabled) {
					it.remove();
					continue;
				}

				sendConfiguredGreetings(server, p.playerName, p.uuid, p.x, p.y, p.z, p.health, p.level);
				it.remove();
			}
		});
	}
	private static String fmt(double v) {
		return BigDecimal.valueOf(v).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}

	private static void sendConfiguredGreetings(
		MinecraftServer server,
		final String playerName,
		final String playerUUID,
		final String playerX,
		final String playerY,
		final String playerZ,
		final String health,
		final String level
	) {
		for (String msg : AutoGreetingServerMod.CONFIG.serverGreetings) {
			if (msg == null || msg.isBlank()) {
				continue;
			}

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
				ServerCommandSource source = server.getCommandSource().withLevel(0);
				server.getCommandManager().parseAndExecute(source, finalMsg);
			} else {
				server.getPlayerManager().broadcast(Text.literal("[Server] " + finalMsg), false);
			}
		}
	}

	private static class PendingGreeting {
		final String playerName;
		final String uuid;
		final String x;
		final String y;
		final String z;
		final String health;
		final String level;
		int ticksLeft;

		PendingGreeting(String playerName, String uuid, String x, String y, String z, String health, String level, int ticksLeft) {
			this.playerName = playerName;
			this.uuid = uuid;
			this.x = x;
			this.y = y;
			this.z = z;
			this.health = health;
			this.level = level;
			this.ticksLeft = ticksLeft;
		}
	}
}
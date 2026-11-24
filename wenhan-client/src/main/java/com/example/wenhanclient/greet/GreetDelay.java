package com.example.wenhanclient.greet;

import com.mojang.authlib.GameProfile;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GreetDelay {
	private static PendingSelfGreeting selfPending = null;
	private static final Map<String, PendingGreeting> pending = new HashMap<>();
	private static final Set<UUID> knownPlayerUuids = new HashSet<>();
	private static boolean registered = false;

	private GreetDelay() {}

	public static void init() {
		registerIfNeeded();
	}

	public static void resetPlayerTracking() {
		knownPlayerUuids.clear();
		pending.clear();
	}

	public static void greetAfter1Second(String playerName, String uuid) {
		if (pending.containsKey(uuid)) {
			return;
		}
		pending.put(uuid, new PendingGreeting(playerName, uuid, 20));
		registerIfNeeded();
	}

	public static void greetSelfAfter1Second() {
		selfPending = new PendingSelfGreeting(20);
		registerIfNeeded();
	}

	private static void registerIfNeeded() {
		if (registered) {
			return;
		}
		registered = true;
		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					updatePlayerTracking(client);
					processSelfGreeting(client);
					processPendingGreetings(client);
				});
	}

	private static void updatePlayerTracking(Minecraft client) {
		if (client.player == null || client.getConnection() == null) {
			knownPlayerUuids.clear();
			return;
		}
		boolean warmup = System.currentTimeMillis() - GreetSubMod.joinWorldAt < 1000L;
		Set<UUID> currentPlayerUuids = new HashSet<>();
		for (PlayerInfo playerInfo : client.getConnection().getOnlinePlayers()) {
			GameProfile profile = playerInfo.getProfile();
			UUID uuid = getProfileUuid(profile);
			if (uuid == null) {
				continue;
			}
			currentPlayerUuids.add(uuid);
			if (uuid.equals(client.player.getUUID()) || warmup || knownPlayerUuids.contains(uuid)) {
				continue;
			}
			String name = getProfileName(profile);
			if (name == null || name.isBlank()) {
				name = uuid.toString();
			}
			if (shouldGreetOtherPlayer(name)) {
				greetAfter1Second(name, uuid.toString());
			}
		}
		knownPlayerUuids.clear();
		knownPlayerUuids.addAll(currentPlayerUuids);
	}

	private static boolean shouldGreetOtherPlayer(String name) {
		GreetConfig config = GreetSubMod.config();
		if (!config.otherEnabled) {
			return false;
		}
		if (config.otherBlacklist.match(name) && !config.otherBlacklistExcept.match(name)) {
			return false;
		}
		return config.otherWhitelist.isEmpty()
				|| (config.otherWhitelist.match(name) && !config.otherWhitelistExcept.match(name));
	}

	private static UUID getProfileUuid(GameProfile profile) {
		if (profile == null) {
			return null;
		}
		UUID uuid = invokeUuidMethod(profile, "id");
		return uuid != null ? uuid : invokeUuidMethod(profile, "getId");
	}

	private static String getProfileName(GameProfile profile) {
		if (profile == null) {
			return null;
		}
		String name = invokeStringMethod(profile, "name");
		return name != null && !name.isBlank() ? name : invokeStringMethod(profile, "getName");
	}

	private static UUID invokeUuidMethod(Object object, String methodName) {
		try {
			Method method = object.getClass().getMethod(methodName);
			Object value = method.invoke(object);
			if (value instanceof UUID uuid) {
				return uuid;
			}
		} catch (IllegalAccessException
				| InvocationTargetException
				| NoSuchMethodException
				| SecurityException ignored) {
		}
		return null;
	}

	private static String invokeStringMethod(Object object, String methodName) {
		try {
			Method method = object.getClass().getMethod(methodName);
			Object value = method.invoke(object);
			if (value instanceof String stringValue) {
				return stringValue;
			}
		} catch (IllegalAccessException
				| InvocationTargetException
				| NoSuchMethodException
				| SecurityException ignored) {
		}
		return null;
	}

	private static void processSelfGreeting(Minecraft client) {
		if (client.player == null || selfPending == null) {
			return;
		}
		selfPending.ticksLeft--;
		if (selfPending.ticksLeft > 0) {
			return;
		}
		sendSelfGreeting(client);
		selfPending = null;
	}

	private static void processPendingGreetings(Minecraft client) {
		if (client.player == null || pending.isEmpty()) {
			return;
		}
		Iterator<PendingGreeting> it = pending.values().iterator();
		while (it.hasNext()) {
			PendingGreeting p = it.next();
			p.ticksLeft--;
			if (p.ticksLeft > 0) {
				continue;
			}
			GreetConfig config = GreetSubMod.config();
			if (config.otherEnabled) {
				for (String msg : config.otherGreetings) {
					if (msg == null || msg.isBlank()) {
						continue;
					}
					msg = msg.trim();
					String finalMsg = msg.replace("@player", p.playerName).replace("@UUID", p.uuid);
					sendChatOrCommand(client, msg, finalMsg);
				}
			}
			it.remove();
		}
	}

	private static String fmt(double v) {
		return BigDecimal.valueOf(v)
				.setScale(3, RoundingMode.HALF_UP)
				.stripTrailingZeros()
				.toPlainString();
	}

	private static void sendSelfGreeting(Minecraft client) {
		GreetConfig config = GreetSubMod.config();
		if (!config.selfEnabled || client.player == null) {
			return;
		}
		String playerName = client.player.getName().getString();
		String playerUUID = client.player.getUUID().toString();
		String playerX = fmt(client.player.getX());
		String playerY = fmt(client.player.getY());
		String playerZ = fmt(client.player.getZ());
		String health = fmt(client.player.getHealth());
		String level = Integer.toString(client.player.experienceLevel);
		for (String msg : config.selfGreetings) {
			if (msg == null || msg.isBlank()) {
				continue;
			}
			msg = msg.trim();
			String finalMsg =
					msg.replace("@player", playerName)
							.replace("@UUID", playerUUID)
							.replace("@X", playerX)
							.replace("@Y", playerY)
							.replace("@Z", playerZ)
							.replace("@health", health)
							.replace("@level", level);
			sendChatOrCommand(client, msg, finalMsg);
		}
	}

	private static void sendChatOrCommand(
			Minecraft client, String templateMessage, String finalMessage) {
		if (client.getConnection() == null) {
			return;
		}
		if (templateMessage.startsWith("/")) {
			client.getConnection().sendCommand(finalMessage.substring(1));
			return;
		}
		client.getConnection().sendChat(finalMessage);
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

	private static class PendingSelfGreeting {
		int ticksLeft;

		PendingSelfGreeting(int ticksLeft) {
			this.ticksLeft = ticksLeft;
		}
	}
}

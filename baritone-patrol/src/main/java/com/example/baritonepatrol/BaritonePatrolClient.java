package com.example.baritonepatrol;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class BaritonePatrolClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("baritone-patrol");

	private PatrolConfig config;
	private boolean running;
	private int targetIndex;
	private String activeRouteId;

	@Override
	public void onInitializeClient() {
		config = PatrolConfig.load();
		registerCommands();
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) ->
						dispatcher.register(
								literal("patrol")
										.then(
												literal("add")
														.executes(context -> addCurrentPosition())
														.then(
																argument(
																				"x",
																				IntegerArgumentType
																						.integer())
																		.then(
																				argument(
																								"y",
																								IntegerArgumentType
																										.integer())
																						.then(
																								argument(
																												"z",
																												IntegerArgumentType
																														.integer())
																										.executes(
																												context ->
																														addPoint(
																																IntegerArgumentType
																																		.getInteger(
																																				context,
																																				"x"),
																																IntegerArgumentType
																																		.getInteger(
																																				context,
																																				"y"),
																																IntegerArgumentType
																																		.getInteger(
																																				context,
																																				"z")))))))
										.then(literal("start").executes(context -> start()))
										.then(literal("stop").executes(context -> stop(true)))
										.then(literal("clear").executes(context -> clear()))
										.then(literal("list").executes(context -> list()))));
	}

	private int addCurrentPosition() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return fail("message.baritone-patrol.no_world");
		}
		BlockPos position = client.player.blockPosition();
		return addPoint(position.getX(), position.getY(), position.getZ());
	}

	private int addPoint(int x, int y, int z) {
		Route route = currentRoute();
		if (route == null) {
			return fail("message.baritone-patrol.no_world");
		}
		PatrolPoint point = new PatrolPoint(x, y, z);
		route.points().add(point);
		config.save();
		feedback(
				"message.baritone-patrol.added",
				route.points().size(),
				point.x(),
				point.y(),
				point.z());
		return 1;
	}

	private int start() {
		Route route = currentRoute();
		if (route == null) {
			return fail("message.baritone-patrol.no_world");
		}
		if (route.points().isEmpty()) {
			return fail("message.baritone-patrol.no_waypoints");
		}
		running = true;
		targetIndex = 0;
		activeRouteId = route.id();
		if (!pathTo(route.points().get(targetIndex))) {
			return 0;
		}
		feedback("message.baritone-patrol.started", route.points().size());
		announceTarget(route.points().get(targetIndex), targetIndex);
		return 1;
	}

	private int stop(boolean announce) {
		boolean wasRunning = running;
		running = false;
		activeRouteId = null;
		if (wasRunning) {
			try {
				baritone().getPathingBehavior().cancelEverything();
			} catch (RuntimeException | LinkageError exception) {
				LOGGER.warn("Unable to cancel Baritone", exception);
			}
		}
		if (announce) {
			feedback(
					wasRunning
							? "message.baritone-patrol.stopped"
							: "message.baritone-patrol.not_running");
		}
		return 1;
	}

	private int clear() {
		Route route = currentRoute();
		if (route == null) {
			return fail("message.baritone-patrol.no_world");
		}
		if (running && route.id().equals(activeRouteId)) {
			stop(false);
		}
		config.clear(route.multiplayer(), route.key());
		feedback("message.baritone-patrol.cleared");
		return 1;
	}

	private int list() {
		Route route = currentRoute();
		if (route == null) {
			return fail("message.baritone-patrol.no_world");
		}
		if (route.points().isEmpty()) {
			feedback("message.baritone-patrol.empty");
			return 1;
		}
		feedback("message.baritone-patrol.list_header", route.key(), route.points().size());
		for (int i = 0; i < route.points().size(); i++) {
			PatrolPoint point = route.points().get(i);
			feedback("message.baritone-patrol.list_item", i + 1, point.x(), point.y(), point.z());
		}
		return 1;
	}

	private void tick(Minecraft client) {
		if (!running || client.player == null || client.level == null) {
			return;
		}
		Route route = currentRoute();
		if (route == null || !route.id().equals(activeRouteId) || route.points().isEmpty()) {
			stop(false);
			return;
		}
		PatrolPoint target = route.points().get(targetIndex % route.points().size());
		BlockPos position = client.player.blockPosition();
		if (position.getX() == target.x()
				&& position.getY() == target.y()
				&& position.getZ() == target.z()) {
			targetIndex = (targetIndex + 1) % route.points().size();
			PatrolPoint nextTarget = route.points().get(targetIndex);
			if (pathTo(nextTarget)) {
				announceTarget(nextTarget, targetIndex);
			}
		}
	}

	private boolean pathTo(PatrolPoint point) {
		try {
			baritone()
					.getCustomGoalProcess()
					.setGoalAndPath(new GoalBlock(point.x(), point.y(), point.z()));
			return true;
		} catch (RuntimeException | LinkageError exception) {
			running = false;
			activeRouteId = null;
			LOGGER.error("Unable to start Baritone pathing", exception);
			fail("message.baritone-patrol.baritone_unavailable");
			return false;
		}
	}

	private static void announceTarget(PatrolPoint point, int index) {
		feedback("message.baritone-patrol.going_to", index + 1, point.x(), point.y(), point.z());
	}

	private IBaritone baritone() {
		return BaritoneAPI.getProvider().getPrimaryBaritone();
	}

	private Route currentRoute() {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return null;
		}
		ServerData server = client.getCurrentServer();
		boolean multiplayer = server != null;
		String key;
		if (multiplayer) {
			key = server.ip;
		} else if (client.getSingleplayerServer() != null) {
			key = client.getSingleplayerServer().getWorldData().getLevelName();
		} else {
			return null;
		}
		return new Route(multiplayer, key, config.points(multiplayer, key));
	}

	private static void feedback(String translationKey, Object... arguments) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null) {
			client.player.sendSystemMessage(Component.translatable(translationKey, arguments));
		}
	}

	private static int fail(String translationKey, Object... arguments) {
		feedback(translationKey, arguments);
		return 0;
	}

	private record Route(boolean multiplayer, String key, List<PatrolPoint> points) {
		String id() {
			return (multiplayer ? "multiplayer:" : "singleplayer:") + key;
		}
	}
}

package com.example.maplocator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MapLocatorClient implements ClientModInitializer {
	private static final Map<UUID, TrackedWaypoint> ACTIVE_WAYPOINTS = new HashMap<>();

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(MapLocatorClient::updateHeldMapWaypoints);
	}

	private static void updateHeldMapWaypoints(Minecraft minecraft) {
		if (minecraft.player == null || minecraft.player.connection == null) {
			clearWaypoints(minecraft);
			return;
		}

		ClientWaypointManager waypointManager = minecraft.player.connection.getWaypointManager();
		Map<UUID, TrackedWaypoint> next = new HashMap<>();

		collectWaypoints(minecraft.player.getMainHandItem(), minecraft.player.getBlockY(), next);
		collectWaypoints(minecraft.player.getOffhandItem(), minecraft.player.getBlockY(), next);

		Set<UUID> removed = new HashSet<>(ACTIVE_WAYPOINTS.keySet());
		removed.removeAll(next.keySet());
		for (UUID id : removed) {
			TrackedWaypoint waypoint = ACTIVE_WAYPOINTS.remove(id);
			if (waypoint != null) {
				waypointManager.untrackWaypoint(waypoint);
			}
		}

		for (Map.Entry<UUID, TrackedWaypoint> entry : next.entrySet()) {
			UUID id = entry.getKey();
			TrackedWaypoint waypoint = entry.getValue();
			if (ACTIVE_WAYPOINTS.containsKey(id)) {
				waypointManager.updateWaypoint(waypoint);
			} else {
				waypointManager.trackWaypoint(waypoint);
			}
			ACTIVE_WAYPOINTS.put(id, waypoint);
		}
	}

	private static void collectWaypoints(
			ItemStack stack, int playerY, Map<UUID, TrackedWaypoint> output) {
		MapId mapId = stack.get(DataComponents.MAP_ID);
		MapDecorations decorations = stack.get(DataComponents.MAP_DECORATIONS);
		if (mapId == null || decorations == null || decorations.decorations().isEmpty()) {
			return;
		}

		for (Map.Entry<String, MapDecorations.Entry> mapEntry :
				decorations.decorations().entrySet()) {
			String markerKey = mapEntry.getKey();
			MapDecorations.Entry decoration = mapEntry.getValue();
			MapDecorationType type = decoration.type().value();
			Identifier assetId = type.assetId();

			UUID id =
					UUID.nameUUIDFromBytes(
							("map-locator:"
											+ mapId.id()
											+ ":"
											+ markerKey
											+ ":"
											+ decoration.x()
											+ ":"
											+ decoration.z()
											+ ":"
											+ decoration.type().getRegisteredName())
									.getBytes(StandardCharsets.UTF_8));

			Waypoint.Icon icon = new Waypoint.Icon();
			icon.style = styleKey(assetId);

			TrackedWaypoint waypoint =
					TrackedWaypoint.setPosition(
							id,
							icon,
							new Vec3i(
									(int) Math.floor(decoration.x()),
									playerY,
									(int) Math.floor(decoration.z())));
			output.put(id, waypoint);
		}
	}

	private static ResourceKey<WaypointStyleAsset> styleKey(Identifier assetId) {
		Identifier styleId =
				Identifier.fromNamespaceAndPath(
						"map-locator",
						"map_marker/" + assetId.getNamespace() + "/" + assetId.getPath());
		return ResourceKey.create(WaypointStyleAssets.ROOT_ID, styleId);
	}

	private static void clearWaypoints(Minecraft minecraft) {
		if (ACTIVE_WAYPOINTS.isEmpty()) {
			return;
		}
		if (minecraft.player != null && minecraft.player.connection != null) {
			ClientWaypointManager waypointManager =
					minecraft.player.connection.getWaypointManager();
			for (TrackedWaypoint waypoint : ACTIVE_WAYPOINTS.values()) {
				waypointManager.untrackWaypoint(waypoint);
			}
		}
		ACTIVE_WAYPOINTS.clear();
	}
}

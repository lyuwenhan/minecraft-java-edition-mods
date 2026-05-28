package com.example.playerfinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWaypointHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.waypoint.TrackedWaypoint;

public class PlayerFinderHud {
	public static void render(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;
		Entity camera = client.getCameraEntity();

		if (camera == null || world == null) {
			return;
		}

		if (client.options.hudHidden) {
			return;
		}

		int baseX = 8;
		int lineHeight = client.textRenderer.fontHeight + 2;
		int screenHeight = context.getScaledWindowHeight();
		int y = screenHeight - 22 - 8;

		Set<UUID> renderedPlayerUuids = new HashSet<>();

		Vec3d cameraPos = new Vec3d(camera.getX(), camera.getY(), camera.getZ());

		for (var player : world.getPlayers()) {
			if (player == camera) {
				continue;
			}

			Vec3d targetPos = new Vec3d(
				player.getX(),
				player.getY(),
				player.getZ()
			);

			double dx = targetPos.x - cameraPos.x;
			double dz = targetPos.z - cameraPos.z;

			int distance = (int) Math.sqrt(dx * dx + dz * dz);
			String arrow = getArrow(camera, targetPos);
			float health = player.getHealth();

			String name = player.getName().getString();
			String distText = distance + "m";
			String healthText = "❤ " + String.format("%.1f", health);
			String posText = String.format(
				"(%d, %d, %d)",
				(int) targetPos.x,
				(int) targetPos.y,
				(int) targetPos.z
			);

			drawLine(context, client, baseX, y, name, arrow, distText, healthText, posText);

			renderedPlayerUuids.add(player.getUuid());

			y -= lineHeight;

			if (y < 8) {
				break;
			}
		}

		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

		if (networkHandler == null) {
			return;
		}

		if (y < 8) {
			return;
		}

		ClientWaypointHandler waypointHandler = networkHandler.getWaypointHandler();

		if (!waypointHandler.hasWaypoint()) {
			return;
		}

		int[] yRef = new int[] { y };

		waypointHandler.forEachWaypoint(camera, waypoint -> {
			if (yRef[0] < 8) {
				return;
			}

			if (isAlreadyRenderedWaypoint(camera, waypoint, renderedPlayerUuids)) {
				return;
			}

			WaypointDisplay display = getWaypointDisplay(client, world, camera, waypoint);

			if (display == null) {
				return;
			}

			drawLine(
				context,
				client,
				baseX,
				yRef[0],
				display.name,
				display.arrow,
				display.distanceText,
				display.healthText,
				display.posText
			);

			yRef[0] -= lineHeight;
		});
	}

	private static void drawLine(
		DrawContext context,
		MinecraftClient client,
		int baseX,
		int y,
		String name,
		String arrow,
		String distText,
		String healthText,
		String posText
	) {
		int x = baseX;

		x = drawOptionalText(context, client, name, x, y, 0xFFF0F0F0);
		x = drawOptionalText(context, client, arrow, x, y, 0xFF55FFFF);
		x = drawOptionalText(context, client, distText, x, y, 0xFFB0B0B0);
		x = drawOptionalText(context, client, healthText, x, y, 0xFF55FFFF);
		drawOptionalText(context, client, posText, x, y, 0xFF909090);
	}

	private static int drawOptionalText(
		DrawContext context,
		MinecraftClient client,
		String text,
		int x,
		int y,
		int color
	) {
		if (text == null || text.isBlank()) {
			return x;
		}

		context.drawText(client.textRenderer, text, x, y, color, false);

		return x + client.textRenderer.getWidth(text + " ");
	}

	private static WaypointDisplay getWaypointDisplay(
		MinecraftClient client,
		ClientWorld world,
		Entity camera,
		TrackedWaypoint waypoint
	) {
		Entity sourceEntity = findSourceEntity(world, waypoint);
		TargetPosition targetPosition = getWaypointTargetPosition(world, camera, waypoint, sourceEntity);

		String healthText = getWaypointHealthText(sourceEntity);
		String posText = targetPosition.posText;

		if (healthText == null && posText == null) {
			return null;
		}

		String name = getWaypointName(client, world, waypoint, sourceEntity);
		String arrow = getWaypointArrow(world, camera, waypoint, targetPosition);
		String distanceText = getWaypointDistanceText(camera, waypoint, targetPosition);

		return new WaypointDisplay(name, arrow, distanceText, healthText, posText);
	}

	private static boolean isAlreadyRenderedWaypoint(
		Entity camera,
		TrackedWaypoint waypoint,
		Set<UUID> renderedPlayerUuids
	) {
		UUID sourceUuid = getWaypointSourceUuid(waypoint);

		if (sourceUuid == null) {
			return false;
		}

		if (sourceUuid.equals(camera.getUuid())) {
			return true;
		}

		return renderedPlayerUuids.contains(sourceUuid);
	}

	private static String getWaypointName(
		MinecraftClient client,
		ClientWorld world,
		TrackedWaypoint waypoint,
		Entity sourceEntity
	) {
		if (sourceEntity != null) {
			return sourceEntity.getName().getString();
		}

		return waypoint.getSource().map(
			uuid -> resolveUuidName(client, world, uuid),
			id -> resolveStringIdName(client, world, id)
		);
	}

	private static String resolveUuidName(MinecraftClient client, ClientWorld world, UUID uuid) {
		Entity entity = findEntityByUuid(world, uuid);

		if (entity != null) {
			return entity.getName().getString();
		}

		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

		if (networkHandler != null) {
			PlayerListEntry entry = networkHandler.getPlayerListEntry(uuid);

			if (entry != null && entry.getProfile() != null) {
				String profileName = getGameProfileName(entry.getProfile());

				if (profileName != null && !profileName.isBlank()) {
					return profileName;
				}
			}
		}

		return uuid.toString();
	}

	private static String getGameProfileName(Object profile) {
		if (profile == null) {
			return null;
		}

		String name = invokeStringMethod(profile, "name");

		if (name != null && !name.isBlank()) {
			return name;
		}

		name = invokeStringMethod(profile, "getName");

		if (name != null && !name.isBlank()) {
			return name;
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
		} catch (ReflectiveOperationException | SecurityException ignored) {
		}

		return null;
	}

	private static String resolveStringIdName(MinecraftClient client, ClientWorld world, String id) {
		UUID uuid = parseUuid(id);

		if (uuid != null) {
			String uuidName = resolveUuidName(client, world, uuid);

			if (!uuidName.equals(uuid.toString())) {
				return uuidName;
			}
		}

		String translated = tryTranslateIdentifier(id);

		if (translated != null) {
			return translated;
		}

		return prettifyIdentifier(id);
	}

	private static String tryTranslateIdentifier(String id) {
		if (id == null || id.isBlank()) {
			return "Waypoint";
		}

		String direct = Text.translatable(id).getString();

		if (!direct.equals(id)) {
			return direct;
		}

		int colonIndex = id.indexOf(':');

		if (colonIndex > 0 && colonIndex + 1 < id.length()) {
			String namespace = id.substring(0, colonIndex);
			String path = id.substring(colonIndex + 1).replace('/', '.');
			String entityKey = "entity." + namespace + "." + path;
			String entityName = Text.translatable(entityKey).getString();

			if (!entityName.equals(entityKey)) {
				return entityName;
			}
		}

		return null;
	}

	private static String prettifyIdentifier(String id) {
		if (id == null || id.isBlank()) {
			return "Waypoint";
		}

		String value = id;

		int colonIndex = value.indexOf(':');

		if (colonIndex >= 0 && colonIndex + 1 < value.length()) {
			value = value.substring(colonIndex + 1);
		}

		int slashIndex = value.lastIndexOf('/');

		if (slashIndex >= 0 && slashIndex + 1 < value.length()) {
			value = value.substring(slashIndex + 1);
		}

		value = value.replace('_', ' ');
		value = value.replace('-', ' ');
		value = value.replace('.', ' ');

		StringBuilder builder = new StringBuilder();
		boolean capitalizeNext = true;

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);

			if (Character.isWhitespace(c)) {
				builder.append(c);
				capitalizeNext = true;
				continue;
			}

			if (capitalizeNext) {
				builder.append(Character.toTitleCase(c));
				capitalizeNext = false;
				continue;
			}

			builder.append(c);
		}

		return builder.toString();
	}

	private static UUID parseUuid(String value) {
		if (value == null) {
			return null;
		}

		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException ignored) {
		}

		return null;
	}

	private static Entity findSourceEntity(ClientWorld world, TrackedWaypoint waypoint) {
		UUID sourceUuid = getWaypointSourceUuid(waypoint);

		if (sourceUuid == null) {
			return null;
		}

		return findEntityByUuid(world, sourceUuid);
	}

	private static UUID getWaypointSourceUuid(TrackedWaypoint waypoint) {
		Optional<UUID> uuid = waypoint.getSource().left();

		if (uuid.isPresent()) {
			return uuid.get();
		}

		Optional<String> id = waypoint.getSource().right();

		if (id.isPresent()) {
			return parseUuid(id.get());
		}

		return null;
	}

	private static Entity findEntityByUuid(ClientWorld world, UUID uuid) {
		for (Entity entity : world.getEntities()) {
			if (entity.getUuid().equals(uuid)) {
				return entity;
			}
		}

		return null;
	}

	private static TargetPosition getWaypointTargetPosition(
		ClientWorld world,
		Entity camera,
		TrackedWaypoint waypoint,
		Entity sourceEntity
	) {
		if (sourceEntity != null) {
			Vec3d entityPos = new Vec3d(
				sourceEntity.getX(),
				sourceEntity.getY(),
				sourceEntity.getZ()
			);

			String posText = String.format(
				"(%d, %d, %d)",
				(int) entityPos.x,
				(int) entityPos.y,
				(int) entityPos.z
			);

			return new TargetPosition(entityPos, posText);
		}

		Vec3i pos = getPrivateFieldByType(waypoint, Vec3i.class);

		if (pos != null) {
			Vec3d targetPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

			String posText = String.format(
				"(%d, %d, %d)",
				pos.getX(),
				pos.getY(),
				pos.getZ()
			);

			return new TargetPosition(targetPos, posText);
		}

		ChunkPos chunkPos = getPrivateFieldByType(waypoint, ChunkPos.class);

		if (chunkPos != null) {
			int x = chunkPos.getCenterX();
			int z = chunkPos.getCenterZ();

			Vec3d targetPos = new Vec3d(x, camera.getY(), z);
			String posText = String.format("(%d, ?, %d)", x, z);

			return new TargetPosition(targetPos, posText);
		}

		return new TargetPosition(null, null);
	}

	private static String getWaypointArrow(
		ClientWorld world,
		Entity camera,
		TrackedWaypoint waypoint,
		TargetPosition targetPosition
	) {
		if (targetPosition.pos != null) {
			return getArrow(camera, targetPosition.pos);
		}

		try {
			TrackedWaypoint.YawProvider yawProvider = new TrackedWaypoint.YawProvider() {
				@Override
				public float getCameraYaw() {
					return camera.getYaw();
				}

				@Override
				public Vec3d getCameraPos() {
					return new Vec3d(
						camera.getX(),
						camera.getY(),
						camera.getZ()
					);
				}
			};

			double diff = waypoint.getRelativeYaw(world, yawProvider, entity -> 1.0F);

			if (Double.isFinite(diff)) {
				return getArrowFromDiff(diff);
			}
		} catch (RuntimeException ignored) {
		}

		return "?";
	}

	private static String getWaypointDistanceText(
		Entity camera,
		TrackedWaypoint waypoint,
		TargetPosition targetPosition
	) {
		if (targetPosition.pos != null) {
			double dx = targetPosition.pos.x - camera.getX();
			double dz = targetPosition.pos.z - camera.getZ();
			int distance = (int) Math.sqrt(dx * dx + dz * dz);

			return distance + "m";
		}

		double squaredDistance = waypoint.squaredDistanceTo(camera);

		if (Double.isFinite(squaredDistance) && squaredDistance >= 0.0D) {
			return ((int) Math.sqrt(squaredDistance)) + "m";
		}

		return "?m";
	}

	private static String getWaypointHealthText(Entity sourceEntity) {
		if (sourceEntity instanceof LivingEntity livingEntity) {
			return "❤ " + String.format("%.1f", livingEntity.getHealth());
		}

		return null;
	}

	private static String getArrow(Entity self, Vec3d targetPos) {
		Vec3d selfPos = new Vec3d(self.getX(), self.getY(), self.getZ());

		double dx = targetPos.x - selfPos.x;
		double dz = targetPos.z - selfPos.z;

		double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
		float selfYaw = MathHelper.wrapDegrees(self.getYaw());
		double diff = MathHelper.wrapDegrees(targetYaw - selfYaw);

		return getArrowFromDiff(diff);
	}

	private static String getArrowFromDiff(double diff) {
		diff = MathHelper.wrapDegrees(diff);

		if (diff >= -22.5 && diff < 22.5) {
			return "↑";
		}

		if (diff >= 22.5 && diff < 67.5) {
			return "↗";
		}

		if (diff >= 67.5 && diff < 112.5) {
			return "→";
		}

		if (diff >= 112.5 && diff < 157.5) {
			return "↘";
		}

		if (diff >= -67.5 && diff < -22.5) {
			return "↖";
		}

		if (diff >= -112.5 && diff < -67.5) {
			return "←";
		}

		if (diff >= -157.5 && diff < -112.5) {
			return "↙";
		}

		return "↓";
	}

	@SuppressWarnings("unchecked")
	private static <T> T getPrivateFieldByType(Object object, Class<T> type) {
		Class<?> clazz = object.getClass();

		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();

			for (Field field : fields) {
				if (!type.isAssignableFrom(field.getType())) {
					continue;
				}

				try {
					field.setAccessible(true);
					Object value = field.get(object);

					if (type.isInstance(value)) {
						return (T) value;
					}
				} catch (ReflectiveOperationException | SecurityException ignored) {
				}
			}

			clazz = clazz.getSuperclass();
		}

		return null;
	}

	private record TargetPosition(Vec3d pos, String posText) {
	}

	private record WaypointDisplay(
		String name,
		String arrow,
		String distanceText,
		String healthText,
		String posText
	) {
	}
}
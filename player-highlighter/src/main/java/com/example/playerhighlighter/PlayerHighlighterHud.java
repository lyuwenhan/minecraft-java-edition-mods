package com.example.playerhighlighter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class PlayerHighlighterHud {
	public static void render(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		ClientLevel world = client.level;
		Entity camera = client.getCameraEntity();

		if (camera == null || world == null) {
			return;
		}

		if (client.options.hideGui) {
			return;
		}

		int baseX = 8;
		int lineHeight = client.font.lineHeight + 2;
		int screenHeight = graphics.guiHeight();
		int y = screenHeight - 22 - 8;

		Set<UUID> renderedPlayerUuids = new HashSet<>();

		Vec3 cameraPos = new Vec3(camera.getX(), camera.getY(), camera.getZ());

		for (var player : world.players()) {
			if (player == camera) {
				continue;
			}

			Vec3 targetPos = new Vec3(
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

			drawLine(graphics, client, baseX, y, name, arrow, distText, healthText, posText);

			renderedPlayerUuids.add(player.getUUID());

			y -= lineHeight;

			if (y < 8) {
				break;
			}
		}

		Object networkHandler = client.getConnection();

		if (networkHandler == null) {
			return;
		}

		if (y < 8) {
			return;
		}

		Object waypointManager = findWaypointManager(networkHandler);

		if (waypointManager == null) {
			return;
		}

		if (!hasWaypoints(waypointManager)) {
			return;
		}

		int[] yRef = new int[] { y };

		forEachWaypoint(waypointManager, camera, waypoint -> {
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
				graphics,
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
		GuiGraphicsExtractor graphics,
		Minecraft client,
		int baseX,
		int y,
		String name,
		String arrow,
		String distText,
		String healthText,
		String posText
	) {
		int x = baseX;

		x = drawOptionalText(graphics, client, name, x, y, 0xFFF0F0F0);
		x = drawOptionalText(graphics, client, arrow, x, y, 0xFF55FFFF);
		x = drawOptionalText(graphics, client, distText, x, y, 0xFFB0B0B0);
		x = drawOptionalText(graphics, client, healthText, x, y, 0xFF55FFFF);
		drawOptionalText(graphics, client, posText, x, y, 0xFF909090);
	}

	private static int drawOptionalText(
			GuiGraphicsExtractor graphics,
			Minecraft client,
			String text,
			int x,
			int y,
			int color
	) {
		if (text == null || text.isBlank()) {
			return x;
		}

		graphics.text(client.font, text, x, y, color, false);

		return x + client.font.width(text + " ");
	}

	private static WaypointDisplay getWaypointDisplay(
			Minecraft client,
			ClientLevel world,
			Entity camera,
			Object waypoint
	) {
		Entity sourceEntity = findSourceEntity(world, waypoint);
		TargetPosition targetPosition = getWaypointTargetPosition(camera, waypoint, sourceEntity);

		String healthText = getWaypointHealthText(sourceEntity);
		String posText = targetPosition.posText;

		if (healthText == null && posText == null) {
			return null;
		}

		String name = getWaypointName(client, world, waypoint, sourceEntity);
		String arrow = getWaypointArrow(camera, targetPosition);
		String distanceText = getWaypointDistanceText(camera, waypoint, targetPosition);

		return new WaypointDisplay(name, arrow, distanceText, healthText, posText);
	}

	private static boolean isAlreadyRenderedWaypoint(
			Entity camera,
			Object waypoint,
			Set<UUID> renderedPlayerUuids
	) {
		UUID sourceUuid = getWaypointSourceUuid(waypoint);

		if (sourceUuid == null) {
			return false;
		}

		if (sourceUuid.equals(camera.getUUID())) {
			return true;
		}

		return renderedPlayerUuids.contains(sourceUuid);
	}

	private static String getWaypointName(
			Minecraft client,
			ClientLevel world,
			Object waypoint,
			Entity sourceEntity
	) {
		if (sourceEntity != null) {
			return sourceEntity.getName().getString();
		}

		UUID uuid = getWaypointSourceUuid(waypoint);

		if (uuid != null) {
			return resolveUuidName(client, world, uuid);
		}

		String id = getWaypointSourceString(waypoint);

		if (id != null) {
			return resolveStringIdName(client, world, id);
		}

		return "Waypoint";
	}

	private static String resolveUuidName(Minecraft client, ClientLevel world, UUID uuid) {
		Entity entity = findEntityByUuid(world, uuid);

		if (entity != null) {
			return entity.getName().getString();
		}

		Object networkHandler = client.getConnection();

		if (networkHandler != null) {
			Object entry = invokeMethod(networkHandler, "getPlayerInfo", uuid);

			if (entry == null) {
				entry = invokeMethod(networkHandler, "getPlayerListEntry", uuid);
			}

			Object profile = null;

			if (entry != null) {
				profile = invokeNoArg(entry, "getProfile", "profile");
			}

			String profileName = getGameProfileName(profile);

			if (profileName != null && !profileName.isBlank()) {
				return profileName;
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
		Object value = invokeNoArg(object, methodName);

		if (value instanceof String stringValue) {
			return stringValue;
		}

		return null;
	}

	private static String resolveStringIdName(Minecraft client, ClientLevel world, String id) {
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

		String direct = Component.translatable(id).getString();

		if (!direct.equals(id)) {
			return direct;
		}

		int colonIndex = id.indexOf(':');

		if (colonIndex > 0 && colonIndex + 1 < id.length()) {
			String namespace = id.substring(0, colonIndex);
			String path = id.substring(colonIndex + 1).replace('/', '.');
			String entityKey = "entity." + namespace + "." + path;
			String entityName = Component.translatable(entityKey).getString();

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

	private static Entity findSourceEntity(ClientLevel world, Object waypoint) {
		UUID sourceUuid = getWaypointSourceUuid(waypoint);

		if (sourceUuid == null) {
			return null;
		}

		return findEntityByUuid(world, sourceUuid);
	}

	private static Object getWaypointSource(Object waypoint) {
		Object source = invokeNoArg(waypoint, "id", "source", "getSource");

		if (source != null) {
			return source;
		}

		return getPrivateFieldByClassName(waypoint, "Either");
	}

	private static UUID getWaypointSourceUuid(Object waypoint) {
		Object source = getWaypointSource(waypoint);

		if (source instanceof UUID uuid) {
			return uuid;
		}

		Object value = getEitherLeftValue(source);

		if (value instanceof UUID uuid) {
			return uuid;
		}

		if (value instanceof String stringValue) {
			return parseUuid(stringValue);
		}

		Object rightValue = getEitherRightValue(source);

		if (rightValue instanceof String stringValue) {
			return parseUuid(stringValue);
		}

		return null;
	}

	private static String getWaypointSourceString(Object waypoint) {
		Object source = getWaypointSource(waypoint);

		if (source instanceof String stringValue) {
			return stringValue;
		}

		Object rightValue = getEitherRightValue(source);

		if (rightValue instanceof String stringValue) {
			return stringValue;
		}

		return null;
	}

	private static Object getEitherLeftValue(Object either) {
		if (either == null) {
			return null;
		}

		Object value = invokeNoArg(either, "left");

		if (value instanceof Optional<?> optional) {
			if (optional.isPresent()) {
				return optional.get();
			}

			return null;
		}

		return value;
	}

	private static Object getEitherRightValue(Object either) {
		if (either == null) {
			return null;
		}

		Object value = invokeNoArg(either, "right");

		if (value instanceof Optional<?> optional) {
			if (optional.isPresent()) {
				return optional.get();
			}

			return null;
		}

		return value;
	}

	private static Entity findEntityByUuid(ClientLevel world, UUID uuid) {
		for (Entity entity : world.entitiesForRendering()) {
			if (entity.getUUID().equals(uuid)) {
				return entity;
			}
		}

		return null;
	}

	private static TargetPosition getWaypointTargetPosition(
			Entity camera,
			Object waypoint,
			Entity sourceEntity
	) {
		if (sourceEntity != null) {
			Vec3 entityPos = new Vec3(
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
			Vec3 targetPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());

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
			Integer x = getChunkCenterX(chunkPos);
			Integer z = getChunkCenterZ(chunkPos);

			if (x != null && z != null) {
				Vec3 targetPos = new Vec3(x, camera.getY(), z);
				String posText = String.format("(%d, ?, %d)", x, z);

				return new TargetPosition(targetPos, posText);
			}
		}

		return new TargetPosition(null, null);
	}

	private static Integer getChunkCenterX(ChunkPos chunkPos) {
		Integer x = invokeIntegerMethod(chunkPos, "getMiddleBlockX");

		if (x != null) {
			return x;
		}

		x = invokeIntegerMethod(chunkPos, "getCenterX");

		if (x != null) {
			return x;
		}

		Integer chunkX = getIntegerField(chunkPos, "x");

		if (chunkX != null) {
			return chunkX * 16 + 8;
		}

		return null;
	}

	private static Integer getChunkCenterZ(ChunkPos chunkPos) {
		Integer z = invokeIntegerMethod(chunkPos, "getMiddleBlockZ");

		if (z != null) {
			return z;
		}

		z = invokeIntegerMethod(chunkPos, "getCenterZ");

		if (z != null) {
			return z;
		}

		Integer chunkZ = getIntegerField(chunkPos, "z");

		if (chunkZ != null) {
			return chunkZ * 16 + 8;
		}

		return null;
	}

	private static String getWaypointArrow(Entity camera, TargetPosition targetPosition) {
		if (targetPosition.pos != null) {
			return getArrow(camera, targetPosition.pos);
		}

		return "?";
	}

	private static String getWaypointDistanceText(
			Entity camera,
			Object waypoint,
			TargetPosition targetPosition
	) {
		if (targetPosition.pos != null) {
			double dx = targetPosition.pos.x - camera.getX();
			double dz = targetPosition.pos.z - camera.getZ();
			int distance = (int) Math.sqrt(dx * dx + dz * dz);

			return distance + "m";
		}

		Double squaredDistance = invokeDoubleMethod(waypoint, "distanceSquared", camera);

		if (squaredDistance == null) {
			squaredDistance = invokeDoubleMethod(waypoint, "squaredDistanceTo", camera);
		}

		if (squaredDistance != null && Double.isFinite(squaredDistance) && squaredDistance >= 0.0D) {
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

	private static String getArrow(Entity self, Vec3 targetPos) {
		Vec3 selfPos = new Vec3(self.getX(), self.getY(), self.getZ());

		double dx = targetPos.x - selfPos.x;
		double dz = targetPos.z - selfPos.z;

		double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
		float selfYaw = Mth.wrapDegrees(self.getYRot());
		double diff = Mth.wrapDegrees(targetYaw - selfYaw);

		return getArrowFromDiff(diff);
	}

	private static String getArrowFromDiff(double diff) {
		diff = Mth.wrapDegrees(diff);

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

	private static Object findWaypointManager(Object networkHandler) {
		Object manager = invokeNoArg(networkHandler, "getWaypointManager", "getWaypointHandler");

		if (manager != null) {
			return manager;
		}

		return getPrivateFieldByClassName(networkHandler, "Waypoint");
	}

	private static boolean hasWaypoints(Object waypointManager) {
		Object value = invokeNoArg(waypointManager, "hasWaypoints", "hasWaypoint");

		if (value instanceof Boolean booleanValue) {
			return booleanValue;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private static void forEachWaypoint(Object waypointManager, Entity camera, Consumer<Object> consumer) {
		Method method = findCompatibleMethod(
				waypointManager.getClass(),
				"forEachWaypoint",
				camera,
				consumer
		);

		if (method == null) {
			return;
		}

		try {
			method.setAccessible(true);
			method.invoke(waypointManager, camera, consumer);
		} catch (ReflectiveOperationException | SecurityException ignored) {
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getPrivateFieldByType(Object object, Class<T> type) {
		if (object == null) {
			return null;
		}

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

	private static Object getPrivateFieldByClassName(Object object, String classNamePart) {
		if (object == null) {
			return null;
		}

		Class<?> clazz = object.getClass();

		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();

			for (Field field : fields) {
				if (!field.getType().getSimpleName().contains(classNamePart)) {
					continue;
				}

				try {
					field.setAccessible(true);
					Object value = field.get(object);

					if (value != null) {
						return value;
					}
				} catch (ReflectiveOperationException | SecurityException ignored) {
				}
			}

			clazz = clazz.getSuperclass();
		}

		return null;
	}

	private static Integer invokeIntegerMethod(Object object, String methodName) {
		Object value = invokeNoArg(object, methodName);

		if (value instanceof Number number) {
			return number.intValue();
		}

		return null;
	}

	private static Integer getIntegerField(Object object, String fieldName) {
		if (object == null) {
			return null;
		}

		Class<?> clazz = object.getClass();

		while (clazz != null) {
			try {
				Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				Object value = field.get(object);

				if (value instanceof Number number) {
					return number.intValue();
				}
			} catch (ReflectiveOperationException | SecurityException ignored) {
			}

			clazz = clazz.getSuperclass();
		}

		return null;
	}

	private static Double invokeDoubleMethod(Object object, String methodName, Object argument) {
		Object value = invokeMethod(object, methodName, argument);

		if (value instanceof Number number) {
			return number.doubleValue();
		}

		return null;
	}

	private static Object invokeNoArg(Object object, String... methodNames) {
		if (object == null) {
			return null;
		}

		for (String methodName : methodNames) {
			Object value = invokeMethod(object, methodName);

			if (value != null) {
				return value;
			}
		}

		return null;
	}

	private static Object invokeMethod(Object object, String methodName, Object... arguments) {
		if (object == null) {
			return null;
		}

		Method method = findCompatibleMethod(object.getClass(), methodName, arguments);

		if (method == null) {
			return null;
		}

		try {
			method.setAccessible(true);
			return method.invoke(object, arguments);
		} catch (ReflectiveOperationException | SecurityException ignored) {
		}

		return null;
	}

	private static Method findCompatibleMethod(Class<?> type, String methodName, Object... arguments) {
		Class<?> clazz = type;

		while (clazz != null) {
			Method[] methods = clazz.getDeclaredMethods();

			for (Method method : methods) {
				if (!method.getName().equals(methodName)) {
					continue;
				}

				Class<?>[] parameterTypes = method.getParameterTypes();

				if (parameterTypes.length != arguments.length) {
					continue;
				}

				if (parametersMatch(parameterTypes, arguments)) {
					return method;
				}
			}

			clazz = clazz.getSuperclass();
		}

		return null;
	}

	private static boolean parametersMatch(Class<?>[] parameterTypes, Object[] arguments) {
		for (int i = 0; i < parameterTypes.length; i++) {
			Object argument = arguments[i];

			if (argument == null) {
				continue;
			}

			Class<?> parameterType = wrapPrimitive(parameterTypes[i]);

			if (!parameterType.isAssignableFrom(argument.getClass())) {
				return false;
			}
		}

		return true;
	}

	private static Class<?> wrapPrimitive(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}

		if (type == int.class) {
			return Integer.class;
		}

		if (type == long.class) {
			return Long.class;
		}

		if (type == float.class) {
			return Float.class;
		}

		if (type == double.class) {
			return Double.class;
		}

		if (type == boolean.class) {
			return Boolean.class;
		}

		if (type == byte.class) {
			return Byte.class;
		}

		if (type == short.class) {
			return Short.class;
		}

		if (type == char.class) {
			return Character.class;
		}

		return type;
	}

	private record TargetPosition(Vec3 pos, String posText) {
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

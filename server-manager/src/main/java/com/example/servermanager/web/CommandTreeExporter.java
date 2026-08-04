package com.example.servermanager.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Exports the Brigadier command graph in a JSON shape that preserves shared nodes and redirects.
 */
final class CommandTreeExporter {
	private CommandTreeExporter() {}

	static String packet(MinecraftServer server) {
		ExportResult result = export(server);
		JsonObject root = new JsonObject();
		root.addProperty("type", "command-tree");
		root.addProperty("root", 0);
		root.add("values", result.values());
		root.add("nodes", result.nodes());
		return root.toString();
	}

	private static ExportResult export(MinecraftServer server) {
		RootCommandNode<?> rootNode = server.getCommands().getDispatcher().getRoot();
		List<CommandNode<?>> sourceNodes = collectNodes(rootNode);
		IdentityHashMap<CommandNode<?>, int[]> exportIndexBySource = new IdentityHashMap<>();
		List<ExportNode> exported = new ArrayList<>();

		for (CommandNode<?> sourceNode : sourceNodes) {
			List<ExportNode> replacement = replacementNodes(server, sourceNode);
			int[] indexes = new int[replacement.size()];
			for (int i = 0; i < replacement.size(); i++) {
				indexes[i] = exported.size();
				exported.add(replacement.get(i));
			}
			exportIndexBySource.put(sourceNode, indexes);
		}

		for (CommandNode<?> sourceNode : sourceNodes) {
			int[] ownerIndexes = exportIndexBySource.get(sourceNode);
			if (ownerIndexes == null || ownerIndexes.length == 0) continue;

			for (int i = 0; i < ownerIndexes.length - 1; i++) {
				exported.get(ownerIndexes[i]).requiredNext.add(ownerIndexes[i + 1]);
			}

			ExportNode edgeOwner = exported.get(ownerIndexes[ownerIndexes.length - 1]);
			List<Integer> targetIndexes = new ArrayList<>();
			for (CommandNode<?> child : orderedChildren(sourceNode)) {
				int[] target = exportIndexBySource.get(child);
				if (target != null && target.length != 0) targetIndexes.add(target[0]);
			}
			CommandNode<?> redirect = sourceNode.getRedirect();
			if (redirect != null) {
				int[] target = exportIndexBySource.get(redirect);
				if (target != null && target.length != 0) targetIndexes.add(target[0]);
			}

			if (sourceNode.getCommand() == null) edgeOwner.requiredNext.addAll(targetIndexes);
			else edgeOwner.next.addAll(targetIndexes);
		}

		ValueTable valueTable = new ValueTable();
		JsonArray nodes = new JsonArray();
		for (ExportNode node : exported) nodes.add(node.toJson(valueTable));
		return new ExportResult(nodes, valueTable.toJson());
	}

	private static List<CommandNode<?>> collectNodes(RootCommandNode<?> rootNode) {
		List<CommandNode<?>> nodes = new ArrayList<>();
		Set<CommandNode<?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		ArrayDeque<CommandNode<?>> queue = new ArrayDeque<>();
		queue.add(rootNode);
		seen.add(rootNode);
		while (!queue.isEmpty()) {
			CommandNode<?> node = queue.removeFirst();
			nodes.add(node);
			for (CommandNode<?> child : orderedChildren(node)) {
				if (seen.add(child)) queue.addLast(child);
			}
			CommandNode<?> redirect = node.getRedirect();
			if (redirect != null && seen.add(redirect)) queue.addLast(redirect);
		}
		return nodes;
	}

	private static List<CommandNode<?>> orderedChildren(CommandNode<?> node) {
		List<CommandNode<?>> children = new ArrayList<>(node.getChildren());
		children.sort(Comparator.comparing(CommandNode::getName));
		return children;
	}

	private static List<ExportNode> replacementNodes(
			MinecraftServer server, CommandNode<?> sourceNode) {
		if (sourceNode instanceof RootCommandNode<?>) return List.of(new ExportNode("root", ""));
		if (sourceNode instanceof LiteralCommandNode<?>)
			return List.of(new ExportNode("literal", sourceNode.getName()));
		if (sourceNode instanceof ArgumentCommandNode<?, ?> argumentNode) {
			ArgumentType<?> argumentType = argumentNode.getType();
			String argumentTypeName = argumentTypeName(argumentType);
			argumentTypeName = refineResourceArgumentTypeName(argumentType, argumentTypeName);
			String[] components = splitComponents(argumentTypeName);
			if (components.length == 0) {
				ExportNode node = new ExportNode("argument", sourceNode.getName());
				node.argumentType = argumentTypeName;
				addValues(server, node, argumentType);
				return List.of(node);
			}

			List<ExportNode> replacement = new ArrayList<>(components.length);
			for (String component : components) {
				ExportNode node = new ExportNode("argument", sourceNode.getName() + component);
				node.argumentType = "coordinate";
				replacement.add(node);
			}
			return replacement;
		}
		return List.of(new ExportNode("unknown", sourceNode.getName()));
	}

	private static String[] splitComponents(String argumentType) {
		return switch (argumentType) {
			case "vec2", "columnPos" -> new String[] {"X", "Z"};
			case "vec3", "blockPos" -> new String[] {"X", "Y", "Z"};
			default -> new String[0];
		};
	}

	private static String argumentTypeName(ArgumentType<?> argumentType) {
		if (argumentType instanceof BoolArgumentType) return "boolean";
		if (argumentType instanceof IntegerArgumentType) return "integer";
		if (argumentType instanceof LongArgumentType) return "long";
		if (argumentType instanceof FloatArgumentType) return "float";
		if (argumentType instanceof DoubleArgumentType) return "double";
		if (argumentType instanceof StringArgumentType stringArgumentType)
			return stringTypeName(stringArgumentType);

		String simpleName = argumentType.getClass().getSimpleName();
		return switch (simpleName) {
			case "EntityArgument" -> "target";
			case "Vec2Argument" -> "vec2";
			case "Vec3Argument" -> "vec3";
			case "BlockPosArgument" -> "blockPos";
			case "ColumnPosArgument" -> "columnPos";
			case "ParticleArgument" -> "particle";
			case "MobEffectArgument" -> "effect";
			case "ResourceLocationArgument" -> "resourceLocation";
			case "ResourceKeyArgument" -> "resourceKey";
			case "ResourceArgument" -> "resource";
			case "DimensionArgument" -> "dimension";
			case "GameModeArgument" -> "gameMode";
			case "ColorArgument" -> "color";
			case "TimeArgument" -> "time";
			case "AngleArgument" -> "angle";
			case "ItemArgument" -> "item";
			case "BlockStateArgument" -> "block";
			case "EntitySummonArgument" -> "entityType";
			default -> decapitalize(removeArgumentSuffix(simpleName));
		};
	}

	private static String stringTypeName(StringArgumentType argumentType) {
		String text = String.valueOf(argumentType);
		if (text.endsWith("word()")) return "word";
		if (text.endsWith("string()")) return "string";
		if (text.endsWith("greedyString()")) return "greedyString";
		return "string";
	}

	private static String removeArgumentSuffix(String simpleName) {
		if (simpleName.endsWith("ArgumentType"))
			return simpleName.substring(0, simpleName.length() - "ArgumentType".length());
		if (simpleName.endsWith("Argument"))
			return simpleName.substring(0, simpleName.length() - "Argument".length());
		return simpleName;
	}

	private static String decapitalize(String value) {
		if (value == null || value.isEmpty()) return "unknown";
		if (value.length() == 1) return value.toLowerCase(Locale.ROOT);
		return value.substring(0, 1).toLowerCase(Locale.ROOT) + value.substring(1);
	}

	private static void addValues(
			MinecraftServer server, ExportNode node, ArgumentType<?> argumentType) {
		Set<String> values = new LinkedHashSet<>();
		collectFixedValues(node.argumentType, values);
		collectKnownRegistryValues(server, node.argumentType, values);
		collectArgumentRegistryValues(server, argumentType, values);
		collectKnownExamples(argumentType, values);
		if (values.isEmpty()) return;
		List<String> sorted = new ArrayList<>(values);
		sorted.sort(Comparator.naturalOrder());
		for (String value : sorted) node.values.add(value);
	}

	private static void collectFixedValues(String argumentType, Set<String> values) {
		switch (argumentType) {
			case "boolean" -> {
				values.add("false");
				values.add("true");
			}
			case "gameMode" -> {
				values.add("adventure");
				values.add("creative");
				values.add("spectator");
				values.add("survival");
			}
			default -> {}
		}
	}

	private static void collectKnownRegistryValues(
			MinecraftServer server, String argumentType, Set<String> values) {
		try {
			switch (argumentType) {
				case "attribute" -> collectRegistryKeys(server, Registries.ATTRIBUTE, values);
				case "biome" -> collectRegistryKeys(server, Registries.BIOME, values);
				case "block" -> collectRegistryKeys(server, Registries.BLOCK, values);
				case "blockEntityType" ->
						collectRegistryKeys(server, Registries.BLOCK_ENTITY_TYPE, values);
				case "configuredFeature" ->
						collectRegistryKeys(server, Registries.CONFIGURED_FEATURE, values);
				case "damageType" -> collectRegistryKeys(server, Registries.DAMAGE_TYPE, values);
				case "dialog" -> collectRegistryKeys(server, Registries.DIALOG, values);
				case "dimension" -> collectDimensionKeys(server, values);
				case "dimensionType" ->
						collectRegistryKeys(server, Registries.DIMENSION_TYPE, values);
				case "effect" -> collectRegistryKeys(server, Registries.MOB_EFFECT, values);
				case "enchantment" -> collectRegistryKeys(server, Registries.ENCHANTMENT, values);
				case "entityType" -> collectRegistryKeys(server, Registries.ENTITY_TYPE, values);
				case "gameEvent" -> collectRegistryKeys(server, Registries.GAME_EVENT, values);
				case "item" -> collectRegistryKeys(server, Registries.ITEM, values);
				case "itemModifier" ->
						collectRegistryKeys(server, Registries.ITEM_MODIFIER, values);
				case "lootTable" -> collectRegistryKeys(server, Registries.LOOT_TABLE, values);
				case "particle" -> collectRegistryKeys(server, Registries.PARTICLE_TYPE, values);
				case "placedFeature" ->
						collectRegistryKeys(server, Registries.PLACED_FEATURE, values);
				case "poi" ->
						collectRegistryKeys(server, Registries.POINT_OF_INTEREST_TYPE, values);
				case "potion" -> collectRegistryKeys(server, Registries.POTION, values);
				case "predicate" -> collectRegistryKeys(server, Registries.PREDICATE, values);
				case "recipe" -> collectRegistryKeys(server, Registries.RECIPE, values);
				case "sound" -> collectRegistryKeys(server, Registries.SOUND_EVENT, values);
				case "structure" -> collectRegistryKeys(server, Registries.STRUCTURE, values);
				case "structureSet" ->
						collectRegistryKeys(server, Registries.STRUCTURE_SET, values);
				case "templatePool" ->
						collectRegistryKeys(server, Registries.TEMPLATE_POOL, values);
				case "testFunction" ->
						collectRegistryKeys(server, Registries.TEST_FUNCTION, values);
				case "testInstance" ->
						collectRegistryKeys(server, Registries.TEST_INSTANCE, values);
				case "timeline" -> collectRegistryKeys(server, Registries.TIMELINE, values);
				default -> {}
			}
		} catch (RuntimeException ignored) {
		}
	}

	private static void collectArgumentRegistryValues(
			MinecraftServer server, ArgumentType<?> argumentType, Set<String> values) {
		ResourceKey<?> registryKey = argumentRegistryKey(argumentType);
		if (registryKey == null) return;
		collectRegistryKeys(server, registryKey, values);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void collectRegistryKeys(
			MinecraftServer server, ResourceKey<?> registryKey, Set<String> values) {
		try {
			Registry registry = server.registryAccess().lookupOrThrow((ResourceKey) registryKey);
			collectRegistryKeys(registry, values);
		} catch (RuntimeException ignored) {
		}
	}

	private static void collectRegistryKeys(Registry<?> registry, Set<String> values) {
		for (Object key : registry.keySet()) values.add(String.valueOf(key));
	}

	private static void collectDimensionKeys(MinecraftServer server, Set<String> values) {
		for (ResourceKey<Level> key : server.levelKeys()) values.add(resourceKeyValue(key));
	}

	private static void collectKnownExamples(ArgumentType<?> argumentType, Set<String> values) {
		String simpleName = argumentType.getClass().getSimpleName();
		if (!Set.of(
						"ColorArgument",
						"EntityAnchorArgument",
						"GameModeArgument",
						"HeightmapTypeArgument",
						"TemplateMirrorArgument",
						"TemplateRotationArgument")
				.contains(simpleName)) return;
		try {
			Collection<String> examples = argumentType.getExamples();
			if (examples != null && examples.size() > 1 && examples.size() <= 512)
				values.addAll(examples);
		} catch (RuntimeException ignored) {
		}
	}

	private static String refineResourceArgumentTypeName(
			ArgumentType<?> argumentType, String fallback) {
		ResourceKey<?> registryKey = argumentRegistryKey(argumentType);
		if (registryKey == null) return fallback;
		String typeName = argumentTypeNameForRegistry(registryKey);
		return typeName == null ? fallback : typeName;
	}

	private static ResourceKey<?> argumentRegistryKey(ArgumentType<?> argumentType) {
		String simpleName = argumentType.getClass().getSimpleName();
		if (!Set.of(
						"ResourceArgument",
						"ResourceKeyArgument",
						"ResourceOrTagArgument",
						"ResourceOrTagKeyArgument",
						"ResourceSelectorArgument")
				.contains(simpleName)) return null;

		try {
			Field field = argumentType.getClass().getDeclaredField("registryKey");
			field.setAccessible(true);
			Object value = field.get(argumentType);
			if (value instanceof ResourceKey<?> key) return key;
		} catch (ReflectiveOperationException | RuntimeException ignored) {
		}
		return null;
	}

	private static String resourceKeyValue(ResourceKey<?> key) {
		if (key == null) return "";
		String reflected = invokeResourceKeyLocationMethod(key, "location");
		if (!reflected.isEmpty()) return reflected;
		reflected = invokeResourceKeyLocationMethod(key, "value");
		if (!reflected.isEmpty()) return reflected;
		String text = String.valueOf(key);
		int slash = text.indexOf(" / ");
		int end = text.lastIndexOf(']');
		if (slash >= 0 && end > slash + 3) return text.substring(slash + 3, end);
		return text;
	}

	private static String invokeResourceKeyLocationMethod(ResourceKey<?> key, String methodName) {
		try {
			Object value = key.getClass().getMethod(methodName).invoke(key);
			return value == null ? "" : String.valueOf(value);
		} catch (ReflectiveOperationException | RuntimeException ignored) {
			return "";
		}
	}

	private static String argumentTypeNameForRegistry(ResourceKey<?> registryKey) {
		String location = resourceKeyValue(registryKey);
		return switch (location) {
			case "minecraft:advancement" -> "advancement";
			case "minecraft:attribute" -> "attribute";
			case "minecraft:biome" -> "biome";
			case "minecraft:block" -> "block";
			case "minecraft:block_entity_type" -> "blockEntityType";
			case "minecraft:configured_feature" -> "configuredFeature";
			case "minecraft:damage_type" -> "damageType";
			case "minecraft:dialog" -> "dialog";
			case "minecraft:dimension" -> "dimension";
			case "minecraft:dimension_type" -> "dimensionType";
			case "minecraft:enchantment" -> "enchantment";
			case "minecraft:entity_type" -> "entityType";
			case "minecraft:game_event" -> "gameEvent";
			case "minecraft:item" -> "item";
			case "minecraft:item_modifier" -> "itemModifier";
			case "minecraft:loot_table" -> "lootTable";
			case "minecraft:mob_effect" -> "effect";
			case "minecraft:particle_type" -> "particle";
			case "minecraft:placed_feature" -> "placedFeature";
			case "minecraft:point_of_interest_type" -> "poi";
			case "minecraft:potion" -> "potion";
			case "minecraft:predicate" -> "predicate";
			case "minecraft:recipe" -> "recipe";
			case "minecraft:sound_event" -> "sound";
			case "minecraft:structure" -> "structure";
			case "minecraft:structure_set" -> "structureSet";
			case "minecraft:template_pool" -> "templatePool";
			case "minecraft:test_function" -> "testFunction";
			case "minecraft:test_instance" -> "testInstance";
			case "minecraft:timeline" -> "timeline";
			default -> null;
		};
	}

	private static void collectKnownFiniteValues(Object value, Set<String> values) {
		if (value == null) return;
		Class<?> valueClass = value.getClass();
		if (valueClass.isEnum()) {
			for (Object constant : valueClass.getEnumConstants()) values.add(enumName(constant));
			return;
		}
		if (valueClass.isArray() && valueClass.getComponentType().isEnum()) {
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) values.add(enumName(Array.get(value, i)));
			return;
		}
		if (value instanceof Iterable<?> iterable) {
			List<Object> collected = new ArrayList<>();
			for (Object item : iterable) {
				if (item == null || !item.getClass().isEnum()) return;
				collected.add(item);
				if (collected.size() > 512) return;
			}
			for (Object item : collected) values.add(enumName(item));
		}
	}

	private static String enumName(Object constant) {
		for (String methodName :
				List.of("getSerializedName", "serializedName", "getName", "getId")) {
			try {
				Method method = constant.getClass().getMethod(methodName);
				Object result = method.invoke(constant);
				if (result != null) return String.valueOf(result);
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}
		return String.valueOf(constant).toLowerCase(Locale.ROOT);
	}

	private static final class ExportNode {
		final String type;
		final String name;
		String argumentType;
		final List<Integer> requiredNext = new ArrayList<>();
		final List<Integer> next = new ArrayList<>();
		final List<String> values = new ArrayList<>();

		ExportNode(String type, String name) {
			this.type = type;
			this.name = name;
		}

		JsonObject toJson(ValueTable valueTable) {
			JsonObject object = new JsonObject();
			object.addProperty("type", type);
			object.addProperty("name", name);
			if (argumentType != null) object.addProperty("argumentType", argumentType);
			object.add("requiredNext", integerArray(requiredNext));
			object.add("next", integerArray(next));
			if (!values.isEmpty()) object.addProperty("values", valueTable.index(values));
			return object;
		}
	}

	private record ExportResult(JsonArray nodes, JsonArray values) {}

	private static final class ValueTable {
		private final Map<List<String>, Integer> indexes = new LinkedHashMap<>();
		private final List<List<String>> values = new ArrayList<>();

		int index(List<String> sourceValues) {
			List<String> key = List.copyOf(sourceValues);
			Integer existing = indexes.get(key);
			if (existing != null) return existing;
			int index = values.size();
			indexes.put(key, index);
			values.add(key);
			return index;
		}

		JsonArray toJson() {
			JsonArray array = new JsonArray();
			for (List<String> valueList : values) array.add(stringArray(valueList));
			return array;
		}
	}

	private static JsonArray integerArray(List<Integer> values) {
		JsonArray array = new JsonArray();
		Set<Integer> emitted = new LinkedHashSet<>(values);
		for (Integer value : emitted) array.add(value);
		return array;
	}

	private static JsonArray stringArray(List<String> values) {
		JsonArray array = new JsonArray();
		for (String value : values) array.add(value);
		return array;
	}
}

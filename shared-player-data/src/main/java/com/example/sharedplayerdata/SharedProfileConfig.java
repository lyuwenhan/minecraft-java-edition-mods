package com.example.sharedplayerdata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.players.NameAndId;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SharedProfileConfig {
	private static final Gson GSON =
			new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Pattern SAFE_GROUP_ID = Pattern.compile("[A-Za-z0-9._-]+");
	private static final String CONFIG_FILE_NAME = "shared-player-data.json";
	private final Path path;
	private final Map<UUID, String> knownNames;
	private final List<Group> groups;
	private final Map<UUID, Group> groupByUuid;

	private SharedProfileConfig(Path path, Map<UUID, String> knownNames, List<Group> groups) {
		this.path = path;
		this.groups = List.copyOf(groups);
		this.groupByUuid = buildGroupByUuid(this.groups);
		Map<UUID, String> boundKnownNames = new LinkedHashMap<>();
		for (Map.Entry<UUID, String> entry : knownNames.entrySet()) {
			if (this.groupByUuid.containsKey(entry.getKey())) {
				boundKnownNames.put(entry.getKey(), entry.getValue());
			}
		}
		this.knownNames = Collections.unmodifiableMap(boundKnownNames);
	}

	public static SharedProfileConfig empty() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
		return new SharedProfileConfig(path, Map.of(), List.of());
	}

	public static SharedProfileConfig loadOrCreate(Logger logger) throws IOException {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			writeDefault(path);
			logger.warn(
					"Created default Shared Player Data config at {}. Edit groups before using the"
							+ " mod.",
					path);
		}
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			Map<UUID, String> knownNames = readKnownNames(root);
			List<Group> groups = readGroups(root, logger);
			SharedProfileConfig loadedConfig = new SharedProfileConfig(path, knownNames, groups);
			loadedConfig.save();
			return loadedConfig;
		} catch (IllegalStateException | JsonParseException exception) {
			throw new IOException("Invalid JSON in " + path, exception);
		}
	}

	public Path path() {
		return path;
	}

	public Optional<String> knownName(UUID uuid) {
		return Optional.ofNullable(knownNames.get(uuid));
	}

	public List<String> knownPlayerNames() {
		return List.copyOf(knownNames.values());
	}

	public Optional<KnownPlayer> knownPlayerByName(String name) {
		for (Map.Entry<UUID, String> entry : knownNames.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(name)) {
				return Optional.of(new KnownPlayer(entry.getKey(), entry.getValue()));
			}
		}
		return Optional.empty();
	}

	public Optional<Group> groupFor(UUID uuid) {
		return Optional.ofNullable(groupByUuid.get(uuid));
	}

	public OptionalInt groupNumberFor(UUID uuid) {
		Optional<Group> optionalGroup = groupFor(uuid);
		if (optionalGroup.isEmpty()) {
			return OptionalInt.empty();
		}
		return groupNumberFor(optionalGroup.get().id());
	}

	public OptionalInt groupNumberFor(String groupId) {
		for (int index = 0; index < groups.size(); index++) {
			if (groups.get(index).id().equals(groupId)) {
				return OptionalInt.of(index + 1);
			}
		}
		return OptionalInt.empty();
	}

	public Optional<Group> groupByNumber(int groupNumber) {
		int index = groupNumber - 1;
		if (index < 0) {
			return Optional.empty();
		}
		if (index >= groups.size()) {
			return Optional.empty();
		}
		return Optional.of(groups.get(index));
	}

	public int groupCount() {
		return groups.size();
	}

	public List<Group> groups() {
		return groups;
	}

	public void save() throws IOException {
		Files.createDirectories(path.getParent());
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			GSON.toJson(toJsonObject(), writer);
		}
	}

	public SharedProfileConfig withRememberedName(UUID uuid, String name) {
		if (!groupByUuid.containsKey(uuid)) {
			return this;
		}
		String previousName = knownNames.get(uuid);
		if (name.equals(previousName)) {
			return this;
		}
		Map<UUID, String> updatedKnownNames = new LinkedHashMap<>(knownNames);
		updatedKnownNames.put(uuid, name);
		return new SharedProfileConfig(path, updatedKnownNames, groups);
	}

	public SharedProfileConfig withRememberedNames(NameAndId... nameAndIds) {
		Map<UUID, String> updatedKnownNames = new LinkedHashMap<>(knownNames);
		boolean changed = false;
		for (NameAndId nameAndId : nameAndIds) {
			if (!groupByUuid.containsKey(nameAndId.id())) {
				continue;
			}
			String previousName = updatedKnownNames.put(nameAndId.id(), nameAndId.name());
			if (!nameAndId.name().equals(previousName)) {
				changed = true;
			}
		}
		if (!changed) {
			return this;
		}
		return new SharedProfileConfig(path, updatedKnownNames, groups);
	}

	public SharedProfileConfig withCreatedGroup() {
		List<Group> updatedGroups = new ArrayList<>(groups);
		updatedGroups.add(new Group(chooseNewGroupId(), Set.of()));
		return new SharedProfileConfig(path, knownNames, updatedGroups);
	}

	public SharedProfileConfig withPlayerAddedToGroup(int groupNumber, UUID uuid)
			throws IOException {
		Group targetGroup =
				groupByNumber(groupNumber)
						.orElseThrow(
								() ->
										new IOException(
												"Playerbind group does not exist: " + groupNumber));
		Optional<Group> optionalExistingGroup = groupFor(uuid);
		if (optionalExistingGroup.isPresent()) {
			Group existingGroup = optionalExistingGroup.get();
			if (existingGroup.id().equals(targetGroup.id())) {
				return this;
			}
		}
		Set<UUID> mergedMembers = new LinkedHashSet<>(targetGroup.members());
		optionalExistingGroup.ifPresent(
				existingGroup -> mergedMembers.addAll(existingGroup.members()));
		mergedMembers.add(uuid);
		List<Group> updatedGroups = new ArrayList<>();
		for (Group group : groups) {
			if (optionalExistingGroup.isPresent()) {
				if (group.id().equals(optionalExistingGroup.get().id())) {
					continue;
				}
			}
			if (group.id().equals(targetGroup.id())) {
				updatedGroups.add(new Group(targetGroup.id(), mergedMembers));
			} else {
				updatedGroups.add(group);
			}
		}
		return new SharedProfileConfig(path, knownNames, updatedGroups);
	}

	public SharedProfileConfig withGroupRemoved(int groupNumber) throws IOException {
		Group removedGroup =
				groupByNumber(groupNumber)
						.orElseThrow(
								() ->
										new IOException(
												"Playerbind group does not exist: " + groupNumber));
		List<Group> updatedGroups = new ArrayList<>();
		for (Group group : groups) {
			if (group.id().equals(removedGroup.id())) {
				continue;
			}
			updatedGroups.add(group);
		}
		return new SharedProfileConfig(path, knownNames, updatedGroups);
	}

	public SharedProfileConfig withPlayerRemovedFromGroup(int groupNumber, UUID uuid)
			throws IOException {
		Group targetGroup =
				groupByNumber(groupNumber)
						.orElseThrow(
								() ->
										new IOException(
												"Playerbind group does not exist: " + groupNumber));
		if (!targetGroup.members().contains(uuid)) {
			throw new IOException("UUID " + uuid + " is not in playerbind group " + groupNumber);
		}
		Set<UUID> updatedMembers = new LinkedHashSet<>(targetGroup.members());
		updatedMembers.remove(uuid);
		List<Group> updatedGroups = new ArrayList<>();
		for (Group group : groups) {
			if (group.id().equals(targetGroup.id())) {
				updatedGroups.add(new Group(targetGroup.id(), updatedMembers));
			} else {
				updatedGroups.add(group);
			}
		}
		return new SharedProfileConfig(path, knownNames, updatedGroups);
	}

	public SharedProfileConfig withBoundPlayers(UUID firstUuid, UUID secondUuid) {
		Optional<Group> optionalFirstGroup = groupFor(firstUuid);
		Optional<Group> optionalSecondGroup = groupFor(secondUuid);
		if (optionalFirstGroup.isPresent() && optionalSecondGroup.isPresent()) {
			Group firstGroup = optionalFirstGroup.get();
			Group secondGroup = optionalSecondGroup.get();
			if (firstGroup.id().equals(secondGroup.id())) {
				return this;
			}
		}
		String mergedGroupId =
				chooseMergedGroupId(optionalFirstGroup, optionalSecondGroup, firstUuid);
		Set<UUID> mergedMembers = new LinkedHashSet<>();
		optionalFirstGroup.ifPresent(group -> mergedMembers.addAll(group.members()));
		optionalSecondGroup.ifPresent(group -> mergedMembers.addAll(group.members()));
		mergedMembers.add(firstUuid);
		mergedMembers.add(secondUuid);
		List<Group> updatedGroups = new ArrayList<>();
		for (Group group : groups) {
			if (optionalFirstGroup.isPresent()) {
				if (group.id().equals(optionalFirstGroup.get().id())) {
					continue;
				}
			}
			if (optionalSecondGroup.isPresent()) {
				if (group.id().equals(optionalSecondGroup.get().id())) {
					continue;
				}
			}
			updatedGroups.add(group);
		}
		updatedGroups.add(new Group(mergedGroupId, mergedMembers));
		return new SharedProfileConfig(path, knownNames, updatedGroups);
	}

	public static void validateGroupId(String id) throws IOException {
		if (!SAFE_GROUP_ID.matcher(id).matches()) {
			throw new IOException(
					"Invalid group id: " + id + ". Allowed characters: A-Z a-z 0-9 . _ -");
		}
	}

	private JsonObject toJsonObject() {
		JsonObject root = new JsonObject();
		JsonObject knownNamesObject = new JsonObject();
		for (Map.Entry<UUID, String> entry : knownNames.entrySet()) {
			knownNamesObject.addProperty(entry.getKey().toString(), entry.getValue());
		}
		root.add("knownNames", knownNamesObject);
		JsonArray groupsArray = new JsonArray();
		for (Group group : groups) {
			JsonObject groupObject = new JsonObject();
			groupObject.addProperty("id", group.id());
			JsonArray membersArray = new JsonArray();
			for (UUID member : group.members()) {
				membersArray.add(member.toString());
			}
			groupObject.add("members", membersArray);
			groupsArray.add(groupObject);
		}
		root.add("groups", groupsArray);
		return root;
	}

	private String chooseMergedGroupId(
			Optional<Group> optionalFirstGroup,
			Optional<Group> optionalSecondGroup,
			UUID firstUuid) {
		if (optionalFirstGroup.isPresent()) {
			return optionalFirstGroup.get().id();
		}
		if (optionalSecondGroup.isPresent()) {
			return optionalSecondGroup.get().id();
		}
		String baseId = "playerbind-" + firstUuid.toString().replace("-", "");
		String id = baseId;
		int suffix = 2;
		while (hasGroupId(id)) {
			id = baseId + "-" + suffix;
			suffix++;
		}
		return id;
	}

	private String chooseNewGroupId() {
		int number = groups.size() + 1;
		String id = "group-" + number;
		while (hasGroupId(id)) {
			number++;
			id = "group-" + number;
		}
		return id;
	}

	private boolean hasGroupId(String id) {
		for (Group group : groups) {
			if (group.id().equals(id)) {
				return true;
			}
		}
		return false;
	}

	private static void writeDefault(Path path) throws IOException {
		JsonObject root = new JsonObject();
		root.add("knownNames", new JsonObject());
		root.add("groups", new JsonArray());
		try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			GSON.toJson(root, writer);
		}
	}

	private static Map<UUID, String> readKnownNames(JsonObject root) throws IOException {
		JsonElement knownNamesElement = root.get("knownNames");
		if (knownNamesElement == null || knownNamesElement.isJsonNull()) {
			return Map.of();
		}
		if (!knownNamesElement.isJsonObject()) {
			throw new IOException("config field 'knownNames' must be an object");
		}
		Map<UUID, String> result = new LinkedHashMap<>();
		JsonObject knownNamesObject = knownNamesElement.getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : knownNamesObject.entrySet()) {
			JsonElement nameElement = entry.getValue();
			if (!nameElement.isJsonPrimitive()) {
				throw new IOException(
						"config field 'knownNames' contains a non-string name for UUID: "
								+ entry.getKey());
			}
			try {
				UUID uuid = UUID.fromString(entry.getKey());
				result.put(uuid, nameElement.getAsString());
			} catch (IllegalArgumentException exception) {
				throw new IOException(
						"config field 'knownNames' contains invalid UUID: " + entry.getKey(),
						exception);
			}
		}
		return result;
	}

	private static List<Group> readGroups(JsonObject root, Logger logger) throws IOException {
		JsonElement groupsElement = root.get("groups");
		if (groupsElement == null || groupsElement.isJsonNull()) {
			return List.of();
		}
		if (!groupsElement.isJsonArray()) {
			throw new IOException("config field 'groups' must be an array");
		}
		List<Group> result = new ArrayList<>();
		Set<String> seenGroupIds = new LinkedHashSet<>();
		for (JsonElement element : groupsElement.getAsJsonArray()) {
			if (!element.isJsonObject()) {
				logger.warn("Ignoring non-object group entry in Shared Player Data config.");
				continue;
			}
			JsonObject groupObject = element.getAsJsonObject();
			String id = getRequiredString(groupObject, "id");
			validateGroupId(id);
			if (!seenGroupIds.add(id)) {
				throw new IOException("Duplicate group id in Shared Player Data config: " + id);
			}
			JsonElement membersElement = groupObject.get("members");
			if (membersElement == null || !membersElement.isJsonArray()) {
				throw new IOException("group '" + id + "' field 'members' must be an array");
			}
			Set<UUID> members = new LinkedHashSet<>();
			for (JsonElement memberElement : membersElement.getAsJsonArray()) {
				if (!memberElement.isJsonPrimitive()) {
					throw new IOException("group '" + id + "' contains a non-string UUID entry");
				}
				String uuidText = memberElement.getAsString();
				try {
					members.add(UUID.fromString(uuidText));
				} catch (IllegalArgumentException exception) {
					throw new IOException(
							"group '" + id + "' contains invalid UUID: " + uuidText, exception);
				}
			}
			if (members.size() < 2) {
				logger.warn(
						"Group '{}' has fewer than 2 members; it will still load but has no sharing"
								+ " effect.",
						id);
			}
			result.add(new Group(id, members));
		}
		return result;
	}

	private static Map<UUID, Group> buildGroupByUuid(List<Group> groups) {
		Map<UUID, Group> result = new HashMap<>();
		for (Group group : groups) {
			for (UUID uuid : group.members()) {
				Group existing = result.put(uuid, group);
				if (existing != null) {
					throw new IllegalArgumentException(
							"UUID "
									+ uuid
									+ " is assigned to both group '"
									+ existing.id()
									+ "' and group '"
									+ group.id()
									+ "'");
				}
			}
		}
		return Collections.unmodifiableMap(result);
	}

	private static String getRequiredString(JsonObject object, String field) throws IOException {
		JsonElement element = object.get(field);
		if (element == null || !element.isJsonPrimitive()) {
			throw new IOException("missing or invalid string field '" + field + "'");
		}
		return element.getAsString();
	}

	public record Group(String id, Set<UUID> members) {
		public Group {
			Objects.requireNonNull(id, "id");
			Objects.requireNonNull(members, "members");
			members = Collections.unmodifiableSet(new LinkedHashSet<>(members));
		}
	}

	public record KnownPlayer(UUID uuid, String name) {}
}

package com.example.sharedplayerdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import org.slf4j.Logger;

public final class SharedProfileManager {
	private final Logger logger;
	private final PlayerDataFileMirror mirror;
	private final ThreadLocal<ServerLoginPacketListenerImpl> currentLoginListener =
			new ThreadLocal<>();
	private final IdentityHashMap<ServerLoginPacketListenerImpl, UUID> pendingUuidByLoginListener =
			new IdentityHashMap<>();
	private final Map<UUID, SharedProfileConfig.Group> activeGroupByUuid = new HashMap<>();
	private final Set<UUID> joinedUuids = new HashSet<>();
	private final Set<UUID> pendingDataResetUuids = new HashSet<>();
	private final Set<ServerPlayer> forcedDisconnectPlayers =
			Collections.newSetFromMap(new IdentityHashMap<ServerPlayer, Boolean>());
	private volatile SharedProfileConfig config = SharedProfileConfig.empty();
	private boolean syncingOperatorStatus;

	public SharedProfileManager(Logger logger) {
		this.logger = logger;
		this.mirror = new PlayerDataFileMirror(logger);
	}

	public void loadConfig() {
		clearTransientState();
		try {
			SharedProfileConfig loadedConfig = SharedProfileConfig.loadOrCreate(logger);
			this.config = loadedConfig;
			logger.info(
					"Loaded Shared Player Data config from {} with {} group(s).",
					loadedConfig.path(),
					loadedConfig.groups().size());
		} catch (IOException | RuntimeException exception) {
			logger.error(
					"Failed to load Shared Player Data config. No UUID groups will be active until"
							+ " the config is fixed and the server is restarted.",
					exception);
			this.config = SharedProfileConfig.empty();
		}
	}

	public void enterLogin(ServerLoginPacketListenerImpl listener) {
		currentLoginListener.set(listener);
	}

	public void exitLogin(ServerLoginPacketListenerImpl listener) {
		ServerLoginPacketListenerImpl current = currentLoginListener.get();
		if (current == listener) {
			currentLoginListener.remove();
		}
	}

	public LoginDecision afterVanillaCanPlayerLogin(MinecraftServer server, UUID uuid, String name) {
		SharedProfileConfig currentConfig = config;
		Optional<SharedProfileConfig.Group> optionalGroup = currentConfig.groupFor(uuid);
		if (optionalGroup.isEmpty()) {
			return LoginDecision.allow();
		}
		currentConfig = rememberName(uuid, name);
		optionalGroup = currentConfig.groupFor(uuid);
		if (optionalGroup.isEmpty()) {
			return LoginDecision.allow();
		}
		SharedProfileConfig.Group group = optionalGroup.get();
		ServerLoginPacketListenerImpl listener = currentLoginListener.get();
		List<ServerPlayer> onlineGroupPlayers = findOnlineGroupPlayers(server, group);
		List<ServerPlayer> carpetFakePlayersToEvict = new ArrayList<>();
		for (ServerPlayer onlinePlayer : onlineGroupPlayers) {
			if (FakePlayerDetector.isCarpetFakePlayer(onlinePlayer)) {
				carpetFakePlayersToEvict.add(onlinePlayer);
				continue;
			}
			logger.info(
					"Rejected login for {} ({}) because shared group '{}' currently has online"
							+ " player {} ({}).",
					name,
					uuid,
					group.id(),
					onlinePlayer.nameAndId().name(),
					onlinePlayer.getUUID());
			return LoginDecision.rejected(Component.translatable(currentConfig.rejectReasonKey()));
		}
		for (ServerPlayer fakePlayer : carpetFakePlayersToEvict) {
			evictCarpetFakePlayerForRealLogin(server, currentConfig, group, fakePlayer, uuid, name);
		}
		try {
			mirror.stageForLogin(server, currentConfig, group, uuid);
			synchronized (this) {
				activeGroupByUuid.put(uuid, group);
				if (listener != null) {
					pendingUuidByLoginListener.put(listener, uuid);
				}
			}
			logger.info("Prepared shared profile group '{}' for {} ({}).", group.id(), name, uuid);
			return LoginDecision.allow();
		} catch (IOException exception) {
			releaseReservation(uuid);
			logger.error(
					"Failed to stage shared profile group '{}' for {} ({}).",
					group.id(),
					name,
					uuid,
					exception);
			return LoginDecision.rejected(
					Component.literal("Shared player data failed to load. Check the server log."));
		}
	}

	public void markPlayJoined(MinecraftServer server, ServerPlayer player) {
		UUID uuid = player.getUUID();
		SharedProfileConfig currentConfig = config;
		Optional<SharedProfileConfig.Group> optionalGroup = currentConfig.groupFor(uuid);
		if (optionalGroup.isPresent()) {
			NameAndId playerNameAndId = player.nameAndId();
			currentConfig = rememberName(playerNameAndId.id(), playerNameAndId.name());
			optionalGroup = currentConfig.groupFor(uuid);
		}
		if (optionalGroup.isEmpty()) {
			return;
		}
		SharedProfileConfig.Group group = optionalGroup.get();
		List<ServerPlayer> otherOnlineGroupPlayers =
				findOnlineGroupPlayersExcept(server, group, player);
		boolean currentPlayerIsFake = FakePlayerDetector.isCarpetFakePlayer(player);
		for (ServerPlayer otherPlayer : otherOnlineGroupPlayers) {
			if (!FakePlayerDetector.isCarpetFakePlayer(otherPlayer)) {
				boolean shouldDisconnect;
				synchronized (this) {
					shouldDisconnect = forcedDisconnectPlayers.add(player);
				}
				if (shouldDisconnect) {
					logger.warn(
							"Disconnected {} ({}) from shared group '{}' because another real group"
									+ " member is already online: {} ({}).",
							player.nameAndId().name(),
							uuid,
							group.id(),
							otherPlayer.nameAndId().name(),
							otherPlayer.getUUID());
					player.connection.disconnect(Component.translatable(currentConfig.rejectReasonKey()));
				}
				return;
			}
		}
		if (currentPlayerIsFake && !otherOnlineGroupPlayers.isEmpty()) {
			boolean shouldDisconnect;
			synchronized (this) {
				shouldDisconnect = forcedDisconnectPlayers.add(player);
			}
			if (shouldDisconnect) {
				logger.warn(
						"Disconnected Carpet fake player {} ({}) from shared group '{}' because"
								+ " another fake group member is already online.",
						player.nameAndId().name(),
						uuid,
						group.id());
				player.connection.disconnect(Component.translatable(currentConfig.rejectReasonKey()));
			}
			return;
		}
		if (!currentPlayerIsFake) {
			for (ServerPlayer otherPlayer : otherOnlineGroupPlayers) {
				syncActivePlayer(server, otherPlayer);
				synchronized (this) {
					forcedDisconnectPlayers.add(otherPlayer);
				}
				logger.info(
						"Disconnected Carpet fake player {} ({}) from shared group '{}' because"
								+ " real player {} ({}) joined.",
						otherPlayer.nameAndId().name(),
						otherPlayer.getUUID(),
						group.id(),
						player.nameAndId().name(),
						uuid);
				otherPlayer.connection.disconnect(Component.translatable(currentConfig.rejectReasonKey()));
			}
		}
		synchronized (this) {
			activeGroupByUuid.put(uuid, group);
			joinedUuids.add(uuid);
			forcedDisconnectPlayers.remove(player);
		}
		syncGroupOperatorStatusToCurrentState(server, currentConfig, group);
	}

	public void releaseLoginListener(ServerLoginPacketListenerImpl listener) {
		exitLogin(listener);
		UUID uuid;
		synchronized (this) {
			uuid = pendingUuidByLoginListener.remove(listener);
			if (uuid == null) {
				return;
			}
			if (joinedUuids.contains(uuid)) {
				return;
			}
		}
		releaseReservation(uuid);
	}

	public void afterPlayerRemoved(MinecraftServer server, ServerPlayer player) {
		UUID uuid = player.getUUID();
		boolean resetPending;
		boolean forcedDisconnect;
		synchronized (this) {
			resetPending = pendingDataResetUuids.remove(uuid);
			forcedDisconnect = forcedDisconnectPlayers.remove(player);
		}
		boolean uuidStillOnline = isUuidOnline(server, uuid);
		boolean uuidHasPendingLogin;
		synchronized (this) {
			uuidHasPendingLogin = pendingUuidByLoginListener.containsValue(uuid);
		}
		if (!resetPending && !forcedDisconnect) {
			syncActivePlayer(server, player);
		}
		if (!uuidStillOnline && !uuidHasPendingLogin) {
			releaseReservation(uuid);
		}
		if (forcedDisconnect && !resetPending && !uuidStillOnline) {
			try {
				mirror.clearRealPlayerFiles(server, uuid);
				logger.info(
						"Discarded saved real player files for unauthorized shared-group join {}"
								+ " after disconnect.",
						uuid);
			} catch (IOException exception) {
				logger.error(
						"Failed to discard saved real player files for unauthorized shared-group"
								+ " join {} after disconnect.",
						uuid,
						exception);
			}
		}
		if (resetPending) {
			try {
				resetPlayerDataAndOperatorStatus(server, config, player.nameAndId());
				logger.info(
						"Reset player data after disconnect for removed player {} ({}).",
						player.nameAndId().name(),
						uuid);
			} catch (IOException exception) {
				logger.error(
						"Failed to reset player data after disconnect for removed player {} ({}).",
						player.nameAndId().name(),
						uuid,
						exception);
			}
		}
	}

	public void enforceExclusiveOnlinePlayers(MinecraftServer server) {
		SharedProfileConfig currentConfig = config;
		Map<String, List<ServerPlayer>> onlinePlayersByGroupId = new HashMap<>();
		Map<String, SharedProfileConfig.Group> groupById = new HashMap<>();
		for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
			UUID uuid = player.getUUID();
			Optional<SharedProfileConfig.Group> optionalGroup = currentConfig.groupFor(uuid);
			if (optionalGroup.isEmpty()) {
				synchronized (this) {
					forcedDisconnectPlayers.remove(player);
				}
				continue;
			}
			NameAndId nameAndId = player.nameAndId();
			currentConfig = rememberName(nameAndId.id(), nameAndId.name());
			optionalGroup = currentConfig.groupFor(uuid);
			if (optionalGroup.isEmpty()) {
				continue;
			}
			SharedProfileConfig.Group group = optionalGroup.get();
			onlinePlayersByGroupId.computeIfAbsent(group.id(), ignored -> new ArrayList<>()).add(player);
			groupById.put(group.id(), group);
		}
		for (Map.Entry<String, List<ServerPlayer>> entry : onlinePlayersByGroupId.entrySet()) {
			String groupId = entry.getKey();
			SharedProfileConfig.Group group = groupById.get(groupId);
			List<ServerPlayer> onlinePlayers = entry.getValue();
			ServerPlayer preferredPlayer = selectPreferredOnlinePlayer(onlinePlayers, null);
			if (preferredPlayer == null) {
				continue;
			}
			UUID preferredUuid = preferredPlayer.getUUID();
			synchronized (this) {
				activeGroupByUuid.put(preferredUuid, group);
				joinedUuids.add(preferredUuid);
				forcedDisconnectPlayers.remove(preferredPlayer);
			}
			for (ServerPlayer player : onlinePlayers) {
				if (player == preferredPlayer) {
					continue;
				}
				if (FakePlayerDetector.isCarpetFakePlayer(player)
						&& !FakePlayerDetector.isCarpetFakePlayer(preferredPlayer)) {
					syncActivePlayer(server, player);
				}
				boolean shouldDisconnect;
				synchronized (this) {
					shouldDisconnect = forcedDisconnectPlayers.add(player);
				}
				if (shouldDisconnect) {
					logger.warn(
							"Disconnected duplicate online player {} ({}) from shared group '{}'"
									+ " during real-time tick enforcement. Preferred UUID: {}.",
							player.nameAndId().name(),
							player.getUUID(),
							groupId,
							preferredUuid);
					player.connection.disconnect(Component.translatable(currentConfig.rejectReasonKey()));
				}
			}
		}
	}

	public void onServerStopping(MinecraftServer server) {
		try {
			server.getPlayerList().saveAll();
		} catch (RuntimeException exception) {
			logger.error(
					"Failed to call PlayerList.saveAll during Shared Player Data server stopping" + " sync.",
					exception);
		}
		for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
			syncActivePlayer(server, player);
		}
	}

	public int groupCount() {
		return config.groupCount();
	}

	public List<String> knownPlayerNames() {
		return config.knownPlayerNames();
	}

	public GroupList listGroups() {
		SharedProfileConfig currentConfig = config;
		List<GroupSummary> summaries = new ArrayList<>();
		int groupNumber = 1;
		for (SharedProfileConfig.Group group : currentConfig.groups()) {
			summaries.add(
					new GroupSummary(groupNumber, group.members().size(), memberNames(currentConfig, group)));
			groupNumber++;
		}
		return new GroupList(summaries);
	}

	public Optional<GroupDetails> groupDetails(int groupNumber) {
		SharedProfileConfig currentConfig = config;
		Optional<SharedProfileConfig.Group> optionalGroup = currentConfig.groupByNumber(groupNumber);
		if (optionalGroup.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(
				new GroupDetails(groupNumber, memberDetails(currentConfig, optionalGroup.get())));
	}

	public Optional<FindPlayerResult> findPlayer(MinecraftServer server, String name) {
		ServerPlayer onlinePlayer = findOnlinePlayerByName(server, name);
		SharedProfileConfig currentConfig = config;
		UUID uuid;
		String resolvedName;
		if (onlinePlayer != null) {
			NameAndId nameAndId = onlinePlayer.nameAndId();
			currentConfig = rememberName(nameAndId.id(), nameAndId.name());
			uuid = nameAndId.id();
			resolvedName = nameAndId.name();
		} else {
			Optional<SharedProfileConfig.KnownPlayer> optionalKnownPlayer =
					currentConfig.knownPlayerByName(name);
			if (optionalKnownPlayer.isEmpty()) {
				return Optional.empty();
			}
			SharedProfileConfig.KnownPlayer knownPlayer = optionalKnownPlayer.get();
			uuid = knownPlayer.uuid();
			resolvedName = knownPlayer.name();
		}
		return Optional.of(
				new FindPlayerResult(resolvedName, uuid, currentConfig.groupNumberFor(uuid)));
	}

	public boolean isKnownBoundPlayerName(MinecraftServer server, String name) {
		SharedProfileConfig currentConfig = config;
		ServerPlayer onlinePlayer = findOnlinePlayerByName(server, name);
		if (onlinePlayer != null) {
			NameAndId nameAndId = onlinePlayer.nameAndId();
			currentConfig = rememberName(nameAndId.id(), nameAndId.name());
			return currentConfig.groupFor(nameAndId.id()).isPresent();
		}
		Optional<SharedProfileConfig.KnownPlayer> optionalKnownPlayer =
				currentConfig.knownPlayerByName(name);
		if (optionalKnownPlayer.isEmpty()) {
			return false;
		}
		SharedProfileConfig.KnownPlayer knownPlayer = optionalKnownPlayer.get();
		return currentConfig.groupFor(knownPlayer.uuid()).isPresent();
	}

	public boolean isKnownBoundPlayerGroupOccupied(MinecraftServer server, String name) {
		SharedProfileConfig currentConfig = config;
		Optional<ResolvedPlayer> optionalResolvedPlayer = resolvePlayer(server, currentConfig, name);
		if (optionalResolvedPlayer.isEmpty()) {
			return false;
		}
		ResolvedPlayer resolvedPlayer = optionalResolvedPlayer.get();
		Optional<SharedProfileConfig.Group> optionalGroup =
				currentConfig.groupFor(resolvedPlayer.uuid());
		if (optionalGroup.isEmpty()) {
			return false;
		}
		return !findOnlineGroupPlayers(server, optionalGroup.get()).isEmpty();
	}

	public CarpetFakeSpawnDecision prepareCarpetFakeSpawn(MinecraftServer server, String name) {
		SharedProfileConfig currentConfig = config;
		Optional<ResolvedPlayer> optionalResolvedPlayer = resolvePlayer(server, currentConfig, name);
		if (optionalResolvedPlayer.isEmpty()) {
			return CarpetFakeSpawnDecision.allowedWithoutReservation();
		}
		ResolvedPlayer resolvedPlayer = optionalResolvedPlayer.get();
		Optional<SharedProfileConfig.Group> optionalGroup =
				currentConfig.groupFor(resolvedPlayer.uuid());
		if (optionalGroup.isEmpty()) {
			return CarpetFakeSpawnDecision.allowedWithoutReservation();
		}
		SharedProfileConfig.Group group = optionalGroup.get();
		if (!findOnlineGroupPlayers(server, group).isEmpty()) {
			return CarpetFakeSpawnDecision.rejected(
					Component.translatable(currentConfig.rejectReasonKey()));
		}
		try {
			mirror.stageForLogin(server, currentConfig, group, resolvedPlayer.uuid());
			synchronized (this) {
				activeGroupByUuid.put(resolvedPlayer.uuid(), group);
			}
			logger.info(
					"Prepared shared profile group '{}' for Carpet fake spawn {} ({}).",
					group.id(),
					resolvedPlayer.name(),
					resolvedPlayer.uuid());
			return CarpetFakeSpawnDecision.allowedWithReservation(resolvedPlayer.uuid());
		} catch (IOException exception) {
			releaseReservation(resolvedPlayer.uuid());
			logger.error(
					"Failed to stage shared profile group '{}' for Carpet fake spawn {} ({}).",
					group.id(),
					resolvedPlayer.name(),
					resolvedPlayer.uuid(),
					exception);
			return CarpetFakeSpawnDecision.rejected(
					Component.literal("Shared player data failed to load. Check the server log."));
		}
	}

	public void releaseExternalReservation(UUID uuid) {
		releaseReservation(uuid);
	}

	public CreateGroupResult createGroup() throws IOException {
		SharedProfileConfig updatedConfig = config.withCreatedGroup();
		updatedConfig.save();
		synchronized (this) {
			installConfigAndReconcileLocksLocked(updatedConfig);
		}
		return new CreateGroupResult(updatedConfig.groupCount());
	}

	public AddPlayerToGroupResult addOnlinePlayerToGroup(
			MinecraftServer server, int groupNumber, ServerPlayer player, ServerPlayer executor)
			throws IOException {
		UUID uuid = player.getUUID();
		NameAndId nameAndId = player.nameAndId();
		SharedProfileConfig currentConfig = config.withRememberedName(nameAndId.id(), nameAndId.name());
		OptionalInt previousGroupNumber = currentConfig.groupNumberFor(uuid);
		boolean alreadyInRequestedGroup =
				previousGroupNumber.isPresent() && previousGroupNumber.getAsInt() == groupNumber;
		SharedProfileConfig updatedConfig = currentConfig.withPlayerAddedToGroup(groupNumber, uuid);
		OptionalInt updatedGroupNumber = updatedConfig.groupNumberFor(uuid);
		SharedProfileConfig.Group updatedGroup =
				updatedConfig
						.groupFor(uuid)
						.orElseThrow(
								() -> new IOException("Player was not assigned to a group after add: " + uuid));
		if (alreadyInRequestedGroup) {
			updatedConfig.save();
			synchronized (this) {
				installConfigAndReconcileLocksLocked(updatedConfig);
			}
			return new AddPlayerToGroupResult(
					updatedGroupNumber.orElse(groupNumber),
					nameAndId.name(),
					uuid,
					updatedGroup.members().size(),
					false,
					List.of());
		}
		try {
			server.getPlayerList().saveAll();
		} catch (RuntimeException exception) {
			logger.error(
					"Failed to call PlayerList.saveAll before /playerbind group add sync.", exception);
			throw new IOException(
					"Failed to save online players before adding a player to a group.", exception);
		}
		updatedConfig.save();
		synchronized (this) {
			releaseReservationLocked(uuid);
			installConfigAndReconcileLocksLocked(updatedConfig);
			activeGroupByUuid.put(uuid, updatedGroup);
			joinedUuids.add(uuid);
		}
		syncGroupOperatorStatusToCurrentState(server, updatedConfig, updatedGroup);
		List<String> disconnectedPlayerNames =
				enforceGroupAddOnlineConflict(server, updatedConfig, updatedGroup, player, executor);
		logger.info(
				"Added {} ({}) to shared profile group '{}' using /playerbind group add."
						+ " Disconnected {} conflicting online player(s).",
				nameAndId.name(),
				uuid,
				updatedGroup.id(),
				disconnectedPlayerNames.size());
		return new AddPlayerToGroupResult(
				updatedGroupNumber.orElse(groupNumber),
				nameAndId.name(),
				uuid,
				updatedGroup.members().size(),
				true,
				disconnectedPlayerNames);
	}

	private List<String> enforceGroupAddOnlineConflict(
			MinecraftServer server,
			SharedProfileConfig currentConfig,
			SharedProfileConfig.Group group,
			ServerPlayer addedPlayer,
			ServerPlayer executor)
			throws IOException {
		List<ServerPlayer> otherOnlineGroupPlayers =
				findOnlineGroupPlayersExcept(server, group, addedPlayer);
		if (otherOnlineGroupPlayers.isEmpty()) {
			return List.of();
		}
		List<String> disconnectedPlayerNames = new ArrayList<>();
		boolean addedPlayerIsExecutor =
				executor != null && executor.getUUID().equals(addedPlayer.getUUID());
		if (addedPlayerIsExecutor) {
			mirror.syncFromPlayer(server, currentConfig, group, addedPlayer.getUUID());
			synchronized (this) {
				activeGroupByUuid.put(addedPlayer.getUUID(), group);
				joinedUuids.add(addedPlayer.getUUID());
				forcedDisconnectPlayers.remove(addedPlayer);
			}
			for (ServerPlayer originalPlayer : otherOnlineGroupPlayers) {
				disconnectGroupAddConflictPlayer(
						currentConfig,
						originalPlayer,
						disconnectedPlayerNames,
						"newly added command executor stayed online");
			}
		} else {
			disconnectGroupAddConflictPlayer(
					currentConfig,
					addedPlayer,
					disconnectedPlayerNames,
					"newly added player was not the command executor");
		}
		return List.copyOf(disconnectedPlayerNames);
	}

	private void disconnectGroupAddConflictPlayer(
			SharedProfileConfig currentConfig,
			ServerPlayer player,
			List<String> disconnectedPlayerNames,
			String reason) {
		boolean shouldDisconnect;
		synchronized (this) {
			shouldDisconnect = forcedDisconnectPlayers.add(player);
		}
		if (!shouldDisconnect) {
			return;
		}
		disconnectedPlayerNames.add(player.nameAndId().name());
		logger.info(
				"Disconnected {} ({}) after /playerbind group add conflict: {}.",
				player.nameAndId().name(),
				player.getUUID(),
				reason);
		player.connection.disconnect(Component.translatable(currentConfig.rejectReasonKey()));
	}

	public RemoveGroupResult removeGroup(MinecraftServer server, int groupNumber) throws IOException {
		SharedProfileConfig currentConfig = config;
		SharedProfileConfig.Group removedGroup =
				currentConfig
						.groupByNumber(groupNumber)
						.orElseThrow(() -> new IOException("Playerbind group does not exist: " + groupNumber));
		SharedProfileConfig updatedConfig = currentConfig.withGroupRemoved(groupNumber);
		updatedConfig.save();
		synchronized (this) {
			for (UUID member : removedGroup.members()) {
				releaseReservationLocked(member);
			}
			installConfigAndReconcileLocksLocked(updatedConfig);
		}
		logger.info(
				"Removed shared profile group '{}' with {} member(s). Player data, advancements,"
						+ " stats, OP status, and shared group files were left unchanged.",
				removedGroup.id(),
				removedGroup.members().size());
		return new RemoveGroupResult(
				groupNumber, removedGroup.members().size(), updatedConfig.groupCount());
	}

	public RemovePlayerFromGroupResult removePlayerFromGroup(
			MinecraftServer server, int groupNumber, String name) throws IOException {
		SharedProfileConfig currentConfig = config;
		ResolvedPlayer resolvedPlayer =
				resolvePlayer(server, currentConfig, name)
						.orElseThrow(() -> new IOException("Unknown player name: " + name));
		currentConfig = config.withRememberedName(resolvedPlayer.uuid(), resolvedPlayer.name());
		SharedProfileConfig.Group group =
				currentConfig
						.groupByNumber(groupNumber)
						.orElseThrow(() -> new IOException("Playerbind group does not exist: " + groupNumber));
		if (!group.members().contains(resolvedPlayer.uuid())) {
			throw new IOException(
					"Player " + resolvedPlayer.name() + " is not in playerbind group " + groupNumber + ".");
		}
		SharedProfileConfig updatedConfig =
				currentConfig.withPlayerRemovedFromGroup(groupNumber, resolvedPlayer.uuid());
		updatedConfig.save();
		synchronized (this) {
			releaseReservationLocked(resolvedPlayer.uuid());
			installConfigAndReconcileLocksLocked(updatedConfig);
		}
		ResetSummary resetSummary = resetRemovedMember(server, updatedConfig, resolvedPlayer.uuid());
		logger.info(
				"Removed {} ({}) from shared profile group '{}'. Reset {} offline member(s),"
						+ " scheduled {} online member reset(s).",
				resolvedPlayer.name(),
				resolvedPlayer.uuid(),
				group.id(),
				resetSummary.immediateResetCount(),
				resetSummary.pendingResetCount());
		return new RemovePlayerFromGroupResult(
				groupNumber,
				resolvedPlayer.name(),
				resolvedPlayer.uuid(),
				group.members().size() - 1,
				resetSummary.immediateResetCount(),
				resetSummary.pendingResetCount());
	}

	public BindResult bindOnlinePlayers(
			MinecraftServer server, ServerPlayer firstPlayer, ServerPlayer secondPlayer)
			throws IOException {
		UUID firstUuid = firstPlayer.getUUID();
		UUID secondUuid = secondPlayer.getUUID();
		NameAndId firstNameAndId = firstPlayer.nameAndId();
		NameAndId secondNameAndId = secondPlayer.nameAndId();
		String firstName = firstNameAndId.name();
		String secondName = secondNameAndId.name();
		if (firstUuid.equals(secondUuid)) {
			throw new IOException("Cannot bind a player to themselves.");
		}
		SharedProfileConfig updatedConfig =
				config
						.withRememberedNames(firstNameAndId, secondNameAndId)
						.withBoundPlayers(firstUuid, secondUuid);
		SharedProfileConfig.Group mergedGroup = updatedConfig.groupFor(firstUuid).orElseThrow();
		try {
			server.getPlayerList().saveAll();
		} catch (RuntimeException exception) {
			logger.error("Failed to call PlayerList.saveAll before /playerbind sync.", exception);
			throw new IOException("Failed to save online players before binding.", exception);
		}
		mirror.syncFromPlayer(server, updatedConfig, mergedGroup, firstUuid);
		updatedConfig.save();
		synchronized (this) {
			this.config = updatedConfig;
			releaseReservationLocked(secondUuid);
			releaseReplacedFirstGroupLock(firstUuid, mergedGroup);
			activeGroupByUuid.put(firstUuid, mergedGroup);
			joinedUuids.add(firstUuid);
		}
		syncGroupOperatorStatusToCurrentState(server, updatedConfig, mergedGroup);
		secondPlayer.connection.disconnect(Component.translatable(updatedConfig.rejectReasonKey()));
		logger.info(
				"Bound {} ({}) and {} ({}) into group '{}'; kicked the second player.",
				firstName,
				firstUuid,
				secondName,
				secondUuid,
				mergedGroup.id());
		return new BindResult(
				mergedGroup.id(),
				firstName,
				firstUuid,
				secondName,
				secondUuid,
				mergedGroup.members().size());
	}

	public void afterOperatorStatusChanged(
			MinecraftServer server, NameAndId nameAndId, boolean operator) {
		if (syncingOperatorStatus) {
			return;
		}
		SharedProfileConfig currentConfig = config;
		Optional<SharedProfileConfig.Group> optionalGroup = currentConfig.groupFor(nameAndId.id());
		if (optionalGroup.isEmpty()) {
			return;
		}
		currentConfig = rememberName(nameAndId.id(), nameAndId.name());
		optionalGroup = currentConfig.groupFor(nameAndId.id());
		if (optionalGroup.isEmpty()) {
			return;
		}
		OperatorTemplate template = null;
		if (operator) {
			template = findOperatorTemplate(server.getPlayerList(), nameAndId.id());
		}
		syncGroupOperatorStatus(server, currentConfig, optionalGroup.get(), operator, template);
	}

	private Optional<ServerPlayer> optionalOnlinePlayerByName(MinecraftServer server, String name) {
		return Optional.ofNullable(findOnlinePlayerByName(server, name));
	}

	private ServerPlayer findOnlinePlayerByName(MinecraftServer server, String name) {
		ServerPlayer exactNamePlayer = server.getPlayerList().getPlayer(name);
		if (exactNamePlayer != null) {
			return exactNamePlayer;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.nameAndId().name().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	private ServerPlayer findOnlinePlayerByUuid(MinecraftServer server, UUID uuid) {
		if (uuid == null) {
			return null;
		}
		return server.getPlayerList().getPlayer(uuid);
	}

	private ServerPlayer findPlayerInListByUuid(List<ServerPlayer> players, UUID uuid) {
		if (uuid == null) {
			return null;
		}
		for (ServerPlayer player : players) {
			if (player.getUUID().equals(uuid)) {
				return player;
			}
		}
		return null;
	}

	private ServerPlayer selectPreferredOnlinePlayer(List<ServerPlayer> players, UUID activeUuid) {
		ServerPlayer activePlayer = findPlayerInListByUuid(players, activeUuid);
		if (activePlayer != null && !FakePlayerDetector.isCarpetFakePlayer(activePlayer)) {
			return activePlayer;
		}
		for (ServerPlayer player : players) {
			if (!FakePlayerDetector.isCarpetFakePlayer(player)) {
				return player;
			}
		}
		if (activePlayer != null) {
			return activePlayer;
		}
		if (players.isEmpty()) {
			return null;
		}
		return players.get(0);
	}

	private void evictCarpetFakePlayerForRealLogin(
			MinecraftServer server,
			SharedProfileConfig currentConfig,
			SharedProfileConfig.Group group,
			ServerPlayer fakePlayer,
			UUID incomingUuid,
			String incomingName) {
		UUID fakeUuid = fakePlayer.getUUID();
		String fakeName = fakePlayer.nameAndId().name();
		syncActivePlayer(server, fakePlayer);
		synchronized (this) {
			releaseReservationLocked(fakeUuid);
		}
		logger.info(
				"Disconnected Carpet fake player {} ({}) from shared group '{}' to allow real"
						+ " player {} ({}) to join.",
				fakeName,
				fakeUuid,
				group.id(),
				incomingName,
				incomingUuid);
		fakePlayer.connection.disconnect(Component.translatable(currentConfig.rejectReasonKey()));
	}

	private List<String> memberNames(
			SharedProfileConfig currentConfig, SharedProfileConfig.Group group) {
		List<String> names = new ArrayList<>();
		for (UUID member : group.members()) {
			names.add(displayName(currentConfig, member));
		}
		return names;
	}

	private List<MemberDetails> memberDetails(
			SharedProfileConfig currentConfig, SharedProfileConfig.Group group) {
		List<MemberDetails> members = new ArrayList<>();
		for (UUID member : group.members()) {
			members.add(new MemberDetails(displayName(currentConfig, member), member));
		}
		return members;
	}

	private String displayName(SharedProfileConfig currentConfig, UUID uuid) {
		Optional<String> optionalName = currentConfig.knownName(uuid);
		if (optionalName.isPresent()) {
			return optionalName.get();
		}
		return uuid.toString();
	}

	private List<ServerPlayer> findOnlineGroupPlayers(
			MinecraftServer server, SharedProfileConfig.Group group) {
		List<ServerPlayer> players = new ArrayList<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (group.members().contains(player.getUUID())) {
				players.add(player);
			}
		}
		return players;
	}

	private List<ServerPlayer> findOnlineGroupPlayersExcept(
			MinecraftServer server, SharedProfileConfig.Group group, ServerPlayer excludedPlayer) {
		List<ServerPlayer> players = new ArrayList<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player == excludedPlayer) {
				continue;
			}
			if (group.members().contains(player.getUUID())) {
				players.add(player);
			}
		}
		return players;
	}

	private boolean isUuidOnline(MinecraftServer server, UUID uuid) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.getUUID().equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	private void clearTransientState() {
		currentLoginListener.remove();
		synchronized (this) {
			pendingUuidByLoginListener.clear();
			activeGroupByUuid.clear();
			joinedUuids.clear();
			pendingDataResetUuids.clear();
			forcedDisconnectPlayers.clear();
		}
	}

	private SharedProfileConfig rememberName(UUID uuid, String name) {
		SharedProfileConfig currentConfig = config;
		SharedProfileConfig updatedConfig = currentConfig.withRememberedName(uuid, name);
		if (updatedConfig == currentConfig) {
			return currentConfig;
		}
		this.config = updatedConfig;
		try {
			updatedConfig.save();
		} catch (IOException exception) {
			logger.warn(
					"Failed to persist remembered name '{}' for UUID {} in Shared Player Data" + " config.",
					name,
					uuid,
					exception);
		}
		return updatedConfig;
	}

	private Optional<ResolvedPlayer> resolvePlayer(
			MinecraftServer server, SharedProfileConfig currentConfig, String name) {
		ServerPlayer onlinePlayer = findOnlinePlayerByName(server, name);
		if (onlinePlayer != null) {
			NameAndId nameAndId = onlinePlayer.nameAndId();
			rememberName(nameAndId.id(), nameAndId.name());
			return Optional.of(new ResolvedPlayer(nameAndId.id(), nameAndId.name()));
		}
		Optional<SharedProfileConfig.KnownPlayer> optionalKnownPlayer =
				currentConfig.knownPlayerByName(name);
		if (optionalKnownPlayer.isPresent()) {
			SharedProfileConfig.KnownPlayer knownPlayer = optionalKnownPlayer.get();
			return Optional.of(new ResolvedPlayer(knownPlayer.uuid(), knownPlayer.name()));
		}
		return Optional.empty();
	}

	private ResetSummary resetRemovedMembers(
			MinecraftServer server, SharedProfileConfig currentConfig, Set<UUID> members)
			throws IOException {
		int immediateResetCount = 0;
		int pendingResetCount = 0;
		for (UUID member : members) {
			ResetSummary summary = resetRemovedMember(server, currentConfig, member);
			immediateResetCount += summary.immediateResetCount();
			pendingResetCount += summary.pendingResetCount();
		}
		return new ResetSummary(immediateResetCount, pendingResetCount);
	}

	private ResetSummary resetRemovedMember(
			MinecraftServer server, SharedProfileConfig currentConfig, UUID uuid) throws IOException {
		ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(uuid);
		if (onlinePlayer != null) {
			NameAndId nameAndId = onlinePlayer.nameAndId();
			clearOperatorStatus(server, currentConfig, nameAndId);
			synchronized (this) {
				pendingDataResetUuids.add(uuid);
			}
			onlinePlayer.connection.disconnect(
					Component.literal("Playerbind membership removed; your player data was reset."));
			return new ResetSummary(0, 1);
		}
		resetPlayerDataAndOperatorStatus(server, currentConfig, uuid);
		return new ResetSummary(1, 0);
	}

	private void resetPlayerDataAndOperatorStatus(
			MinecraftServer server, SharedProfileConfig currentConfig, UUID uuid) throws IOException {
		NameAndId nameAndId = resolveNameAndId(server.getPlayerList(), currentConfig, uuid);
		if (nameAndId != null) {
			resetPlayerDataAndOperatorStatus(server, currentConfig, nameAndId);
		} else {
			logger.warn(
					"Cannot clear OP status for removed UUID {} because no player name is known;"
							+ " clearing saved player files only.",
					uuid);
			mirror.clearRealPlayerFiles(server, uuid);
		}
	}

	private void resetPlayerDataAndOperatorStatus(
			MinecraftServer server, SharedProfileConfig currentConfig, NameAndId nameAndId)
			throws IOException {
		clearOperatorStatus(server, currentConfig, nameAndId);
		mirror.clearRealPlayerFiles(server, nameAndId.id());
	}

	private void clearOperatorStatus(
			MinecraftServer server, SharedProfileConfig currentConfig, NameAndId nameAndId) {
		PlayerList playerList = server.getPlayerList();
		if (!playerList.isOp(nameAndId)) {
			return;
		}
		boolean previousSyncingOperatorStatus = syncingOperatorStatus;
		syncingOperatorStatus = true;
		try {
			playerList.deop(nameAndId);
			logger.info(
					"Cleared OP status for removed player {} ({}).", nameAndId.name(), nameAndId.id());
		} finally {
			syncingOperatorStatus = previousSyncingOperatorStatus;
		}
	}

	private void syncActivePlayer(MinecraftServer server, ServerPlayer player) {
		UUID uuid = player.getUUID();
		SharedProfileConfig currentConfig = config;
		Optional<SharedProfileConfig.Group> optionalGroup = currentConfig.groupFor(uuid);
		if (optionalGroup.isEmpty()) {
			return;
		}
		SharedProfileConfig.Group group = optionalGroup.get();
		try {
			mirror.syncFromPlayer(server, currentConfig, group, uuid);
			logger.info("Synced shared profile group '{}' from {}.", group.id(), uuid);
		} catch (IOException exception) {
			logger.error(
					"Failed to sync shared profile group '{}' from {}.", group.id(), uuid, exception);
		}
	}

	private void syncGroupOperatorStatusToCurrentState(
			MinecraftServer server, SharedProfileConfig currentConfig, SharedProfileConfig.Group group) {
		PlayerList playerList = server.getPlayerList();
		OperatorTemplate template = findGroupOperatorTemplate(playerList, currentConfig, group);
		syncGroupOperatorStatus(server, currentConfig, group, template != null, template);
	}

	private OperatorTemplate findGroupOperatorTemplate(
			PlayerList playerList, SharedProfileConfig currentConfig, SharedProfileConfig.Group group) {
		for (UUID member : group.members()) {
			OperatorTemplate template = findOperatorTemplate(playerList, member);
			if (template != null) {
				return template;
			}
			NameAndId nameAndId = resolveNameAndId(playerList, currentConfig, member);
			if (nameAndId != null && playerList.isOp(nameAndId)) {
				return findOperatorTemplate(playerList, nameAndId.id());
			}
		}
		return null;
	}

	private void syncGroupOperatorStatus(
			MinecraftServer server,
			SharedProfileConfig currentConfig,
			SharedProfileConfig.Group group,
			boolean operator,
			OperatorTemplate template) {
		if (syncingOperatorStatus) {
			return;
		}
		PlayerList playerList = server.getPlayerList();
		syncingOperatorStatus = true;
		try {
			for (UUID member : group.members()) {
				NameAndId nameAndId = resolveNameAndId(playerList, currentConfig, member);
				if (nameAndId == null) {
					logger.warn(
							"Cannot sync OP status for UUID {} in shared group '{}' because no"
									+ " player name is known yet.",
							member,
							group.id());
					continue;
				}
				if (operator) {
					if (!playerList.isOp(nameAndId)) {
						if (template != null) {
							playerList.op(
									nameAndId,
									Optional.of(template.permissions()),
									Optional.of(template.bypassesPlayerLimit()));
						} else {
							playerList.op(nameAndId);
						}
						logger.info(
								"Synced OP status: added {} ({}) because shared group '{}' has OP" + " enabled.",
								nameAndId.name(),
								nameAndId.id(),
								group.id());
					}
				} else {
					if (playerList.isOp(nameAndId)) {
						playerList.deop(nameAndId);
						logger.info(
								"Synced OP status: removed {} ({}) because shared group '{}' has OP" + " disabled.",
								nameAndId.name(),
								nameAndId.id(),
								group.id());
					}
				}
			}
		} finally {
			syncingOperatorStatus = false;
		}
	}

	private NameAndId resolveNameAndId(
			PlayerList playerList, SharedProfileConfig currentConfig, UUID uuid) {
		ServerPlayer onlinePlayer = playerList.getPlayer(uuid);
		if (onlinePlayer != null) {
			return onlinePlayer.nameAndId();
		}
		NameAndId operatorIdentity = findOperatorIdentity(playerList, uuid);
		if (operatorIdentity != null) {
			return operatorIdentity;
		}
		Optional<String> optionalName = currentConfig.knownName(uuid);
		if (optionalName.isPresent()) {
			return new NameAndId(uuid, optionalName.get());
		}
		return null;
	}

	private NameAndId findOperatorIdentity(PlayerList playerList, UUID uuid) {
		ServerOpListEntry entry = findOperatorEntry(playerList, uuid);
		if (entry == null) {
			return null;
		}
		return entry.getUser();
	}

	private OperatorTemplate findOperatorTemplate(PlayerList playerList, UUID uuid) {
		ServerOpListEntry entry = findOperatorEntry(playerList, uuid);
		if (entry == null) {
			return null;
		}
		return new OperatorTemplate(entry.permissions(), entry.getBypassesPlayerLimit());
	}

	private ServerOpListEntry findOperatorEntry(PlayerList playerList, UUID uuid) {
		for (ServerOpListEntry entry : playerList.getOps().getEntries()) {
			NameAndId nameAndId = entry.getUser();
			if (nameAndId == null) {
				continue;
			}
			if (uuid.equals(nameAndId.id())) {
				return entry;
			}
		}
		return null;
	}

	private void releaseReservation(UUID uuid) {
		synchronized (this) {
			releaseReservationLocked(uuid);
		}
	}

	private void releaseReservationLocked(UUID uuid) {
		activeGroupByUuid.remove(uuid);
		joinedUuids.remove(uuid);
		pendingUuidByLoginListener.entrySet().removeIf(entry -> uuid.equals(entry.getValue()));
	}

	private void releaseReplacedFirstGroupLock(
			UUID firstUuid, SharedProfileConfig.Group mergedGroup) {
		activeGroupByUuid.remove(firstUuid);
	}

	private void installConfigAndReconcileLocksLocked(SharedProfileConfig updatedConfig) {
		this.config = updatedConfig;
		reconcileLocksLocked(updatedConfig);
	}

	private void reconcileLocksLocked(SharedProfileConfig updatedConfig) {
		Map<UUID, SharedProfileConfig.Group> updatedActiveGroupByUuid = new HashMap<>();
		for (UUID uuid : new ArrayList<>(activeGroupByUuid.keySet())) {
			Optional<SharedProfileConfig.Group> optionalGroup = updatedConfig.groupFor(uuid);
			if (optionalGroup.isEmpty()) {
				continue;
			}
			updatedActiveGroupByUuid.put(uuid, optionalGroup.get());
		}
		activeGroupByUuid.clear();
		activeGroupByUuid.putAll(updatedActiveGroupByUuid);
		pendingUuidByLoginListener
				.entrySet()
				.removeIf(entry -> updatedConfig.groupFor(entry.getValue()).isEmpty());
		joinedUuids.removeIf(uuid -> !activeGroupByUuid.containsKey(uuid));
	}

	private record OperatorTemplate(
			LevelBasedPermissionSet permissions, boolean bypassesPlayerLimit) {}

	private record ResolvedPlayer(UUID uuid, String name) {}

	private record ResetSummary(int immediateResetCount, int pendingResetCount) {}

	public record CarpetFakeSpawnDecision(
			boolean allowed, Optional<UUID> reservedUuid, Component reason) {
		public static CarpetFakeSpawnDecision allowedWithoutReservation() {
			return new CarpetFakeSpawnDecision(true, Optional.empty(), null);
		}

		public static CarpetFakeSpawnDecision allowedWithReservation(UUID uuid) {
			return new CarpetFakeSpawnDecision(true, Optional.of(uuid), null);
		}

		public static CarpetFakeSpawnDecision rejected(Component reason) {
			return new CarpetFakeSpawnDecision(false, Optional.empty(), reason);
		}
	}

	public record BindResult(
			String groupId,
			String firstName,
			UUID firstUuid,
			String secondName,
			UUID secondUuid,
			int memberCount) {}

	public record CreateGroupResult(int groupNumber) {}

	public record AddPlayerToGroupResult(
			int groupNumber,
			String playerName,
			UUID playerUuid,
			int memberCount,
			boolean changed,
			List<String> disconnectedPlayerNames) {}

	public record RemoveGroupResult(
			int removedGroupNumber, int removedMemberCount, int remainingGroupCount) {}

	public record RemovePlayerFromGroupResult(
			int groupNumber,
			String playerName,
			UUID playerUuid,
			int remainingMemberCount,
			int immediateResetCount,
			int pendingResetCount) {}

	public record GroupList(List<GroupSummary> groups) {}

	public record GroupSummary(int groupNumber, int memberCount, List<String> memberNames) {}

	public record GroupDetails(int groupNumber, List<MemberDetails> members) {}

	public record MemberDetails(String name, UUID uuid) {}

	public record FindPlayerResult(String name, UUID uuid, OptionalInt groupNumber) {}
}

package com.example.sharedplayerdata;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

public final class PlayerDataFileMirror {
	private final Logger logger;

	public PlayerDataFileMirror(Logger logger) {
		this.logger = logger;
	}

	public void stageForLogin(MinecraftServer server, SharedProfileConfig.Group group, UUID uuid)
			throws IOException {
		SharedProfileConfig.validateGroupId(group.id());
		FileSet real = realFiles(server, uuid);
		FileSet shared = sharedFiles(server, group);
		initializeSharedFileIfMissing(real.playerData(), shared.playerData());
		initializeSharedFileIfMissing(real.stats(), shared.stats());
		initializeSharedFileIfMissing(real.advancements(), shared.advancements());
		copySharedToReal(shared.playerData(), real.playerData());
		copySharedToReal(shared.stats(), real.stats());
		copySharedToReal(shared.advancements(), real.advancements());
	}

	public void syncFromPlayer(
			MinecraftServer server, SharedProfileConfig.Group group, UUID sourceUuid)
			throws IOException {
		SharedProfileConfig.validateGroupId(group.id());
		FileSet real = realFiles(server, sourceUuid);
		FileSet shared = sharedFiles(server, group);
		copyRealToShared(
				real.playerData(), shared.playerData(), "playerdata", sourceUuid, group.id());
		copyRealToShared(real.stats(), shared.stats(), "stats", sourceUuid, group.id());
		copyRealToShared(
				real.advancements(), shared.advancements(), "advancements", sourceUuid, group.id());
	}

	public void clearRealPlayerFiles(MinecraftServer server, UUID uuid) throws IOException {
		FileSet real = realFiles(server, uuid);
		deleteIfExists(real.playerData(), "playerdata", uuid);
		deleteIfExists(real.stats(), "stats", uuid);
		deleteIfExists(real.advancements(), "advancements", uuid);
	}

	public void clearSharedGroupFiles(MinecraftServer server, SharedProfileConfig.Group group)
			throws IOException {
		SharedProfileConfig.validateGroupId(group.id());
		Path root = sharedGroupRoot(server, group);
		deleteRecursivelyIfExists(root);
	}

	private void initializeSharedFileIfMissing(Path realFile, Path sharedFile) throws IOException {
		if (Files.exists(sharedFile)) {
			return;
		}
		if (Files.notExists(realFile)) {
			return;
		}
		Files.createDirectories(sharedFile.getParent());
		atomicCopy(realFile, sharedFile);
	}

	private void copySharedToReal(Path sharedFile, Path realFile) throws IOException {
		if (Files.notExists(sharedFile)) {
			return;
		}
		Files.createDirectories(realFile.getParent());
		atomicCopy(sharedFile, realFile);
	}

	private void copyRealToShared(
			Path realFile, Path sharedFile, String label, UUID uuid, String groupId)
			throws IOException {
		if (Files.notExists(realFile)) {
			logger.warn(
					"Skipped syncing {} for {} in group '{}' because the real file does not exist:"
							+ " {}",
					label,
					uuid,
					groupId,
					realFile);
			return;
		}
		Files.createDirectories(sharedFile.getParent());
		atomicCopy(realFile, sharedFile);
	}

	private FileSet realFiles(MinecraftServer server, UUID uuid) {
		String fileName = uuid.toString();
		return new FileSet(
				server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(fileName + ".dat"),
				server.getWorldPath(LevelResource.PLAYER_STATS_DIR).resolve(fileName + ".json"),
				server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR)
						.resolve(fileName + ".json"));
	}

	private FileSet sharedFiles(MinecraftServer server, SharedProfileConfig.Group group) {
		Path root = sharedGroupRoot(server, group);
		return new FileSet(
				root.resolve("playerdata.dat"),
				root.resolve("stats.json"),
				root.resolve("advancements.json"));
	}

	private Path sharedGroupRoot(MinecraftServer server, SharedProfileConfig.Group group) {
		return server.getWorldPath(LevelResource.ROOT)
				.resolve("shared-player-data")
				.resolve("groups")
				.resolve(group.id());
	}

	private void deleteIfExists(Path path, String label, UUID uuid) throws IOException {
		if (Files.notExists(path)) {
			return;
		}
		Files.delete(path);
		logger.info("Deleted {} file for removed player {}: {}", label, uuid, path);
	}

	private void deleteRecursivelyIfExists(Path root) throws IOException {
		if (Files.notExists(root)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(path);
			}
		}
		logger.info("Deleted shared player data group directory: {}", root);
	}

	private void atomicCopy(Path source, Path target) throws IOException {
		Path parent = target.getParent();
		Files.createDirectories(parent);
		Path temp = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
		try {
			Files.copy(
					source,
					temp,
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES);
			try {
				Files.move(
						temp,
						target,
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temp);
		}
	}

	private record FileSet(Path playerData, Path stats, Path advancements) {}
}

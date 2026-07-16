package com.example.sharedplayerdata;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public final class PlayerDataFileMirror {
	private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMATTER =
			DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(ZoneOffset.UTC);

	private final Logger logger;

	public PlayerDataFileMirror(Logger logger) {
		this.logger = logger;
	}

	public void stageForLogin(
			MinecraftServer server,
			SharedProfileConfig config,
			SharedProfileConfig.Group group,
			UUID uuid)
			throws IOException {
		SharedProfileConfig.validateGroupId(group.id());
		FileSet real = realFiles(server, uuid);
		FileSet shared = sharedFiles(server, group);

		initializeSharedFileIfMissing(real.playerData(), shared.playerData());
		initializeSharedFileIfMissing(real.stats(), shared.stats());
		initializeSharedFileIfMissing(real.advancements(), shared.advancements());

		copySharedToReal(config, group, uuid, shared.playerData(), real.playerData(), "playerdata");
		copySharedToReal(config, group, uuid, shared.stats(), real.stats(), "stats");
		copySharedToReal(
				config, group, uuid, shared.advancements(), real.advancements(), "advancements");
	}

	public void syncFromPlayer(
			MinecraftServer server,
			SharedProfileConfig config,
			SharedProfileConfig.Group group,
			UUID sourceUuid)
			throws IOException {
		SharedProfileConfig.validateGroupId(group.id());
		FileSet real = realFiles(server, sourceUuid);
		FileSet shared = sharedFiles(server, group);

		copyRealToShared(real.playerData(), shared.playerData(), "playerdata", sourceUuid, group.id());
		copyRealToShared(real.stats(), shared.stats(), "stats", sourceUuid, group.id());
		copyRealToShared(
				real.advancements(), shared.advancements(), "advancements", sourceUuid, group.id());

		if (config.syncRealUuidFilesOnSave()) {
			for (UUID member : group.members()) {
				FileSet memberReal = realFiles(server, member);
				copyIfExists(shared.playerData(), memberReal.playerData());
				copyIfExists(shared.stats(), memberReal.stats());
				copyIfExists(shared.advancements(), memberReal.advancements());
			}
		}
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

	private void copySharedToReal(
			SharedProfileConfig config,
			SharedProfileConfig.Group group,
			UUID uuid,
			Path sharedFile,
			Path realFile,
			String label)
			throws IOException {
		if (Files.notExists(sharedFile)) {
			return;
		}

		if (config.backupRealPlayerFilesBeforeOverwrite()
				&& Files.exists(realFile)
				&& !sameContent(realFile, sharedFile)) {
			Path backup = backupPath(realFile, group.id(), uuid, label);
			Files.createDirectories(backup.getParent());
			atomicCopy(realFile, backup);
			logger.info("Backed up {} for {} in group '{}' to {}", label, uuid, group.id(), backup);
		}

		Files.createDirectories(realFile.getParent());
		atomicCopy(sharedFile, realFile);
	}

	private void copyRealToShared(
			Path realFile, Path sharedFile, String label, UUID uuid, String groupId) throws IOException {
		if (Files.notExists(realFile)) {
			logger.warn(
					"Skipped syncing {} for {} in group '{}' because the real file does not exist:" + " {}",
					label,
					uuid,
					groupId,
					realFile);
			return;
		}

		Files.createDirectories(sharedFile.getParent());
		atomicCopy(realFile, sharedFile);
	}

	private void copyIfExists(Path source, Path target) throws IOException {
		if (Files.notExists(source)) {
			return;
		}

		Files.createDirectories(target.getParent());
		atomicCopy(source, target);
	}

	private FileSet realFiles(MinecraftServer server, UUID uuid) {
		String fileName = uuid.toString();
		return new FileSet(
				server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(fileName + ".dat"),
				server.getWorldPath(LevelResource.PLAYER_STATS_DIR).resolve(fileName + ".json"),
				server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).resolve(fileName + ".json"));
	}

	private FileSet sharedFiles(MinecraftServer server, SharedProfileConfig.Group group) {
		Path root = sharedGroupRoot(server, group);
		return new FileSet(
				root.resolve("playerdata.dat"),
				root.resolve("stats.json"),
				root.resolve("advancements.json"));
	}

	private Path sharedGroupRoot(MinecraftServer server, SharedProfileConfig.Group group) {
		return server
				.getWorldPath(LevelResource.ROOT)
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

	private Path backupPath(Path realFile, String groupId, UUID uuid, String label) {
		Path worldRoot = realFile.getParent().getParent();
		String timestamp = BACKUP_TIMESTAMP_FORMATTER.format(Instant.now());
		String fileName = label + "-" + uuid + "-" + timestamp + getExtension(realFile);
		return worldRoot
				.resolve("shared-player-data")
				.resolve("backups")
				.resolve(groupId)
				.resolve(fileName);
	}

	private String getExtension(Path path) {
		String fileName = path.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');

		if (dotIndex < 0) {
			return "";
		}

		return fileName.substring(dotIndex);
	}

	private boolean sameContent(Path first, Path second) throws IOException {
		if (Files.size(first) != Files.size(second)) {
			return false;
		}

		byte[] firstBytes = Files.readAllBytes(first);
		byte[] secondBytes = Files.readAllBytes(second);

		if (firstBytes.length != secondBytes.length) {
			return false;
		}

		for (int index = 0; index < firstBytes.length; index++) {
			if (firstBytes[index] != secondBytes[index]) {
				return false;
			}
		}

		return true;
	}

	private void atomicCopy(Path source, Path target) throws IOException {
		Path parent = target.getParent();
		Files.createDirectories(parent);
		Path temp = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");

		try {
			Files.copy(
					source, temp, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			try {
				Files.move(
						temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temp);
		}
	}

	private record FileSet(Path playerData, Path stats, Path advancements) {}
}

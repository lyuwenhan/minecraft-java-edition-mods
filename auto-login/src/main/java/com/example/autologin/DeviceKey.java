package com.example.autologin;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages a random 256-bit device key stored in a separate file.
 * The key is not derivable from any public information (unlike user.name+os.name).
 * Stored outside the main config JSON so that stealing only the config file
 * does not allow decryption.
 */
public final class DeviceKey {

	private static final int KEY_SIZE = 32;
	private static byte[] cachedKey;

	private static Path keyPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("auto-login.key");
	}

	/**
	 * Get the device key, generating and persisting it if needed.
	 * @return 32-byte key, or null if unavailable
	 */
	public static byte[] get() {
		if (cachedKey != null) {
			return cachedKey;
		}
		Path p = keyPath();
		try {
			if (Files.exists(p)) {
				cachedKey = Files.readAllBytes(p);
				if (cachedKey.length == KEY_SIZE) {
					return cachedKey;
				}
			}
			byte[] key = new byte[KEY_SIZE];
			new SecureRandom().nextBytes(key);
			Files.createDirectories(p.getParent());
			Files.write(p, key);
			restrictPermissions(p);
			cachedKey = key;
			return key;
		} catch (IOException e) {
			return null;
		}
	}

	private static void restrictPermissions(Path p) {
		try {
			Set<PosixFilePermission> perms = new HashSet<>();
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_WRITE);
			Files.setPosixFilePermissions(p, perms);
		} catch (UnsupportedOperationException | IOException ignored) {
			// Windows or unsupported - skip
		}
	}
}

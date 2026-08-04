package com.example.servermanager.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/** Persistent web-console accounts. All timestamps are UTC epoch milliseconds. */
public final class AccountStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]{2,15}");
	private final Path file;
	private final Map<String, Account> accounts = new LinkedHashMap<>();

	private AccountStore(Path file) {
		this.file = file;
	}

	public static AccountStore load(
			Path configDirectory, String legacyUsername, String legacySalt, String legacyHash)
			throws IOException {
		AccountStore store = new AccountStore(configDirectory.resolve("accounts.json"));
		if (Files.isRegularFile(store.file)) {
			try {
				AccountFile data =
						GSON.fromJson(
								Files.readString(store.file, StandardCharsets.UTF_8),
								AccountFile.class);
				if (data != null && data.accounts != null) {
					for (Account account : data.accounts) {
						if (account != null
								&& isValidName(account.name)
								&& account.salt != null
								&& account.passwordSha256 != null) {
							store.accounts.put(key(account.name), account);
						}
					}
				}
			} catch (JsonParseException exception) {
				throw new IOException("Invalid account database: " + store.file, exception);
			}
		}
		store.purgeInvalidInternal(Instant.now().toEpochMilli());
		if (store.accounts.isEmpty()
				&& legacyUsername != null
				&& !legacyUsername.isBlank()
				&& legacySalt != null
				&& !legacySalt.isBlank()
				&& legacyHash != null
				&& !legacyHash.isBlank()) {
			store.accounts.put(
					key(legacyUsername),
					new Account(
							legacyUsername,
							legacySalt,
							legacyHash,
							null,
							null,
							Instant.now().toEpochMilli()));
		}
		store.save();
		return store;
	}

	public synchronized LoginResult authenticate(String name, String password) throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		Account account = accounts.get(key(name));
		if (account == null
				|| !PasswordHash.verify(account.salt, account.passwordSha256, password)) {
			save();
			return LoginResult.failure("Username or password does not match");
		}
		if (account.remainingUses != null) {
			if (account.remainingUses <= 0) {
				accounts.remove(key(name));
				save();
				return LoginResult.failure("Account has no remaining uses");
			}
			account.remainingUses--;
		}
		save();
		return LoginResult.success(account.name);
	}

	public synchronized OperationResult create(
			String name, String password, String confirmation, Long uses, Long expiresMinutes)
			throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		if (!isValidName(name))
			return OperationResult.failure(
					"Account name must start with a letter and contain 3-16 letters, numbers, or"
							+ " underscores");
		if (password == null || password.isEmpty())
			return OperationResult.failure("Password cannot be empty");
		if (!Objects.equals(password, confirmation))
			return OperationResult.failure("Password confirmation does not match");
		if (uses != null && uses <= 0)
			return OperationResult.failure("Uses must be an integer greater than 0");
		if (expiresMinutes != null && expiresMinutes <= 0)
			return OperationResult.failure("Expiry must be a number of minutes greater than 0");
		if (accounts.containsKey(key(name)))
			return OperationResult.failure("Account already exists: " + name);
		long now = Instant.now().toEpochMilli();
		Long expiresAt = expiresMinutes == null ? null : safeExpiry(now, expiresMinutes);
		String salt = PasswordHash.newSalt();
		accounts.put(
				key(name),
				new Account(name, salt, PasswordHash.hash(salt, password), uses, expiresAt, now));
		save();
		return OperationResult.success("Created account " + name);
	}

	public synchronized OperationResult delete(String name) throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		Account removed = accounts.remove(key(name));
		save();
		if (removed == null) return OperationResult.failure("Account not found: " + name);
		SessionRegistry.invalidateUsername(removed.name);
		return OperationResult.success("Deleted account " + removed.name);
	}

	public synchronized OperationResult purge() throws IOException {
		int count = accounts.size();
		accounts.clear();
		save();
		SessionRegistry.invalidateAll();
		return OperationResult.success("Purged " + count + " account" + (count == 1 ? "" : "s"));
	}

	public synchronized OperationResult resetPassword(String name, String password)
			throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		if (password == null || password.isEmpty())
			return OperationResult.failure("Password cannot be empty");
		Account account = accounts.get(key(name));
		if (account == null) return OperationResult.failure("Account not found: " + name);
		String salt = PasswordHash.newSalt();
		account.salt = salt;
		account.passwordSha256 = PasswordHash.hash(salt, password);
		save();
		SessionRegistry.invalidateUsername(account.name);
		return OperationResult.success("Reset password for " + account.name);
	}

	public synchronized OperationResult updateUses(String name, Long uses) throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		if (uses != null && uses <= 0)
			return OperationResult.failure("Uses must be an integer greater than 0");
		Account account = accounts.get(key(name));
		if (account == null) return OperationResult.failure("Account not found: " + name);
		account.remainingUses = uses;
		save();
		return OperationResult.success(
				"Updated uses for " + account.name + " to " + (uses == null ? "unlimited" : uses));
	}

	public synchronized OperationResult updateExpiry(String name, Long expiresMinutes)
			throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		if (expiresMinutes != null && expiresMinutes <= 0)
			return OperationResult.failure("Expiry must be a number of minutes greater than 0");
		Account account = accounts.get(key(name));
		if (account == null) return OperationResult.failure("Account not found: " + name);
		account.expiresAtUtcMillis =
				expiresMinutes == null
						? null
						: safeExpiry(Instant.now().toEpochMilli(), expiresMinutes);
		save();
		return OperationResult.success(
				"Updated expiry for "
						+ account.name
						+ " to "
						+ (expiresMinutes == null
								? "unlimited"
								: expiresMinutes + " minute" + (expiresMinutes == 1 ? "" : "s")));
	}

	public synchronized List<String> list() throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		save();
		List<Account> sorted = new ArrayList<>(accounts.values());
		sorted.sort(Comparator.comparing(a -> a.name.toLowerCase(Locale.ROOT)));
		List<String> output = new ArrayList<>();
		for (Account account : sorted) {
			String uses =
					account.remainingUses == null
							? "unlimited"
							: Long.toString(account.remainingUses);
			String expires =
					account.expiresAtUtcMillis == null
							? "unlimited"
							: Instant.ofEpochMilli(account.expiresAtUtcMillis).toString();
			output.add(account.name + " | uses=" + uses + " | expires=" + expires);
		}
		return output;
	}

	public synchronized boolean isSessionAccountValid(String name) {
		try {
			purgeInvalidInternal(Instant.now().toEpochMilli());
			save();
			return name != null && accounts.containsKey(key(name));
		} catch (IOException exception) {
			return false;
		}
	}

	public synchronized void purgeInvalid() throws IOException {
		purgeInvalidInternal(Instant.now().toEpochMilli());
		save();
	}

	private void purgeInvalidInternal(long now) {
		List<String> invalidated = new ArrayList<>();
		accounts.entrySet()
				.removeIf(
						entry -> {
							Account account = entry.getValue();
							boolean invalid =
									account.expiresAtUtcMillis != null
											&& account.expiresAtUtcMillis <= now;
							if (invalid) invalidated.add(account.name);
							return invalid;
						});
		invalidated.forEach(SessionRegistry::invalidateUsername);
	}

	private void save() throws IOException {
		Files.createDirectories(file.getParent());
		Path temp = file.resolveSibling(file.getFileName() + ".tmp");
		Files.writeString(
				temp,
				GSON.toJson(new AccountFile(new ArrayList<>(accounts.values()))),
				StandardCharsets.UTF_8);
		try {
			Files.move(
					temp,
					file,
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException ignored) {
			Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static long safeExpiry(long nowMillis, long minutes) {
		try {
			return Math.addExact(nowMillis, Math.multiplyExact(minutes, 60_000L));
		} catch (ArithmeticException exception) {
			throw new IllegalArgumentException("Expiry is too large", exception);
		}
	}

	private static boolean isValidName(String name) {
		return name != null && NAME_PATTERN.matcher(name).matches();
	}

	private static String key(String name) {
		return name == null ? "" : name.toLowerCase(Locale.ROOT);
	}

	private static final class AccountFile {
		private final int version = 1;
		private List<Account> accounts;

		private AccountFile(List<Account> accounts) {
			this.accounts = accounts;
		}
	}

	private static final class Account {
		private String name;
		private String salt;
		private String passwordSha256;
		private Long remainingUses;
		private Long expiresAtUtcMillis;
		private long createdAtUtcMillis;

		private Account(
				String name,
				String salt,
				String passwordSha256,
				Long remainingUses,
				Long expiresAtUtcMillis,
				long createdAtUtcMillis) {
			this.name = name;
			this.salt = salt;
			this.passwordSha256 = passwordSha256;
			this.remainingUses = remainingUses;
			this.expiresAtUtcMillis = expiresAtUtcMillis;
			this.createdAtUtcMillis = createdAtUtcMillis;
		}
	}

	public record LoginResult(boolean succeed, String username, String reason) {
		static LoginResult success(String username) {
			return new LoginResult(true, username, null);
		}

		static LoginResult failure(String reason) {
			return new LoginResult(false, null, reason);
		}
	}

	public record OperationResult(boolean succeed, String message) {
		static OperationResult success(String message) {
			return new OperationResult(true, message);
		}

		static OperationResult failure(String message) {
			return new OperationResult(false, message);
		}
	}
}

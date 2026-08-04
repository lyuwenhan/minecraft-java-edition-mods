package com.example.servermanager.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHash {
	private static final SecureRandom RANDOM = new SecureRandom();

	private PasswordHash() {}

	public static String newSalt() {
		byte[] salt = new byte[32];
		RANDOM.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	public static String hash(String saltBase64, String password) {
		try {
			byte[] salt = Base64.getDecoder().decode(saltBase64);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(salt);
			digest.update(password.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(digest.digest());
		} catch (Exception e) {
			throw new IllegalStateException("Failed to hash password", e);
		}
	}

	public static boolean verify(String salt, String expectedHash, String password) {
		byte[] expected;
		byte[] actual;
		try {
			expected = Base64.getDecoder().decode(expectedHash);
			actual = Base64.getDecoder().decode(hash(salt, password));
		} catch (IllegalArgumentException e) {
			return false;
		}
		return MessageDigest.isEqual(expected, actual);
	}
}

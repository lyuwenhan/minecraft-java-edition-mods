package com.example.servermanager.web;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record WebServerConfig(
		boolean enabled,
		String host,
		int httpsPort,
		Path certificatePath,
		Path privateKeyPath,
		AccountStore accountStore) {
	private static final String CONFIG_DIRECTORY_NAME = "server-manager-config";
	private static final String KEYS_DIRECTORY_NAME = "keys";
	private static final String CONFIG_FILE_NAME = "web-server.properties";

	public static WebServerConfig load() throws IOException {
		Path configDirectory =
				FabricLoader.getInstance()
						.getGameDir()
						.resolve(CONFIG_DIRECTORY_NAME)
						.normalize()
						.toAbsolutePath();
		Path keysDirectory = configDirectory.resolve(KEYS_DIRECTORY_NAME);
		Files.createDirectories(keysDirectory);
		Path configFile = configDirectory.resolve(CONFIG_FILE_NAME);

		Properties properties = defaultProperties();
		if (Files.isRegularFile(configFile)) {
			try (InputStream input = Files.newInputStream(configFile)) {
				properties.load(input);
			}
		}

		boolean changed = false;
		String legacyUsername = properties.getProperty("username", "").trim();
		String salt = properties.getProperty("password-salt", "").trim();
		String hash = properties.getProperty("password-sha256", "").trim();
		String legacyPassword = properties.getProperty("password", "");

		boolean hasLegacyUsername = !legacyUsername.isEmpty();
		boolean hasLegacyHash = !salt.isEmpty() && !hash.isEmpty();
		boolean hasLegacyPlaintextPassword = !legacyPassword.isEmpty();
		if (hasLegacyUsername && !hasLegacyHash && hasLegacyPlaintextPassword) {
			salt = PasswordHash.newSalt();
			hash = PasswordHash.hash(salt, legacyPassword);
			hasLegacyHash = true;
		}

		AccountStore accountStore =
				AccountStore.load(
						configDirectory,
						hasLegacyUsername && hasLegacyHash ? legacyUsername : null,
						hasLegacyUsername && hasLegacyHash ? salt : null,
						hasLegacyUsername && hasLegacyHash ? hash : null);
		if (properties.remove("username") != null) changed = true;
		if (properties.remove("password") != null) changed = true;
		if (properties.remove("password-salt") != null) changed = true;
		if (properties.remove("password-sha256") != null) changed = true;
		if (Files.notExists(configFile) || changed) {
			try (OutputStream output = Files.newOutputStream(configFile)) {
				properties.store(
						output,
						"Server Manager HTTPS/WSS configuration. Accounts are stored in"
								+ " accounts.json with salted SHA-256 password hashes.");
			}
		}

		TlsFiles tls = selectTlsFiles(keysDirectory);
		return new WebServerConfig(
				parseBoolean(properties, "enabled", true),
				properties.getProperty("host", "0.0.0.0").trim(),
				parsePort(properties, "https-port", properties.getProperty("port", "25585")),
				tls.certificatePath(),
				tls.privateKeyPath(),
				accountStore);
	}

	private static Properties defaultProperties() {
		Properties p = new Properties();
		p.setProperty("enabled", "true");
		p.setProperty("host", "0.0.0.0");
		p.setProperty("https-port", "25585");
		return p;
	}

	private static TlsFiles selectTlsFiles(Path dir) throws IOException {
		Path defaultKey = dir.resolve("key.pem"), defaultCert = dir.resolve("cert.pem");
		Path overrideKey = dir.resolve("key_override.pem"),
				overrideCert = dir.resolve("cert_override.pem");
		boolean ok = Files.isRegularFile(overrideKey), oc = Files.isRegularFile(overrideCert);
		if (ok != oc)
			throw new IOException(
					"TLS override requires both files: " + overrideKey + " and " + overrideCert);
		if (ok) return new TlsFiles(overrideCert, overrideKey);
		boolean dk = Files.isRegularFile(defaultKey), dc = Files.isRegularFile(defaultCert);
		if (dk != dc)
			throw new IOException(
					"Default TLS files are incomplete; delete the remaining file to regenerate"
							+ " both");
		if (!dk) {
			try {
				TlsCertificateGenerator.generate(defaultKey, defaultCert);
			} catch (Exception e) {
				throw new IOException("Failed to generate default EC TLS certificate", e);
			}
		}
		return new TlsFiles(defaultCert, defaultKey);
	}

	private static boolean parseBoolean(Properties p, String key, boolean fallback) {
		String value = p.getProperty(key, Boolean.toString(fallback)).trim();
		if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
			throw new IllegalArgumentException("Property '" + key + "' must be true or false");
		return Boolean.parseBoolean(value);
	}

	private static int parsePort(Properties p, String key, String fallback) {
		try {
			int port = Integer.parseInt(p.getProperty(key, fallback).trim());
			if (port < 1 || port > 65535)
				throw new IllegalArgumentException(
						"Property '" + key + "' must be between 1 and 65535");
			return port;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Property '" + key + "' is not a valid integer", e);
		}
	}

	private record TlsFiles(Path certificatePath, Path privateKeyPath) {}
}

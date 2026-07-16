package com.example.autologin;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

public class AutoLoginMod implements ClientModInitializer {

	private static boolean attempted = false;
	private static boolean pendingLogin = false;

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register(
				(handler, sender, client) -> {
					attempted = false;
					pendingLogin = true;
				});

		ClientTickEvents.END_CLIENT_TICK.register(
				client -> {
					if (!pendingLogin || attempted || client.player == null) {
						return;
					}

					attempted = true;
					pendingLogin = false;

					tryAutoLogin(client);
				});

		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> {
					dispatcher.register(
							ClientCommands.literal("autologin")
									.then(
											ClientCommands.literal("set")
													.then(
															ClientCommands.argument(
																			"password",
																			StringArgumentType
																					.greedyString())
																	.executes(
																			ctx -> {
																				Minecraft client =
																						Minecraft
																								.getInstance();
																				String serverKey =
																						getCurrentServerKey(
																								client);

																				if (serverKey
																						== null) {
																					ctx.getSource()
																							.sendError(
																									Component
																											.translatable(
																													"command.autologin.login.failed.no_server"));
																					return 0;
																				}

																				AutoLoginConfig
																						cfg =
																								AutoLoginConfig
																										.load();
																				AutoLoginConfig
																								.Credential
																						cred =
																								new AutoLoginConfig
																										.Credential();

																				try {
																					byte[]
																							deviceKey =
																									DeviceKey
																											.get();

																					if (deviceKey
																							== null) {
																						ctx.getSource()
																								.sendError(
																										Component
																												.translatable(
																														"command.autologin.login.failed.key"));
																						return 0;
																					}

																					Crypto.Result
																							result =
																									Crypto
																											.encrypt(
																													StringArgumentType
																															.getString(
																																	ctx,
																																	"password"),
																													deviceKey);

																					cred.enc =
																							result.enc;
																					cred.salt =
																							result.salt;
																					cred.iv =
																							result.iv;
																					cred.enabled =
																							true;

																					cfg.servers.put(
																							serverKey,
																							cred);
																					cfg.save();

																					ctx.getSource()
																							.sendFeedback(
																									Component
																											.translatable(
																													"command.autologin.set"));
																				} catch (
																						Exception
																								e) {
																					ctx.getSource()
																							.sendError(
																									Component
																											.literal(
																													"Failed"
																														+ " to save"
																														+ " password."));
																				}

																				return 1;
																			})))
									.then(
											ClientCommands.literal("login")
													.executes(
															ctx -> {
																Minecraft client =
																		Minecraft.getInstance();
																LoginAttemptResult result =
																		tryAutoLogin(client);

																if (result.sent()) {
																	ctx.getSource()
																			.sendFeedback(
																					Component
																							.translatable(
																									result
																											.translationKey()));
																	return 1;
																}

																ctx.getSource()
																		.sendError(
																				Component
																						.translatable(
																								result
																										.translationKey()));
																return 0;
															}))
									.then(
											ClientCommands.literal("clear")
													.executes(
															ctx -> {
																Minecraft client =
																		Minecraft.getInstance();
																String serverKey =
																		getCurrentServerKey(client);

																if (serverKey == null) {
																	ctx.getSource()
																			.sendError(
																					Component
																							.translatable(
																									"command.autologin.login.failed.no_server"));
																	return 0;
																}

																AutoLoginConfig cfg =
																		AutoLoginConfig.load();
																cfg.servers.remove(serverKey);
																cfg.save();

																ctx.getSource()
																		.sendFeedback(
																				Component
																						.translatable(
																								"command.autologin.clear"));
																return 1;
															}))
									.then(
											ClientCommands.literal("on")
													.executes(
															ctx -> {
																if (!toggleForCurrentServer(true)) {
																	ctx.getSource()
																			.sendError(
																					Component
																							.translatable(
																									"command.autologin.login.failed.no_password"));
																	return 0;
																}

																ctx.getSource()
																		.sendFeedback(
																				Component
																						.translatable(
																								"command.autologin.toggle.on"));
																return 1;
															}))
									.then(
											ClientCommands.literal("off")
													.executes(
															ctx -> {
																if (!toggleForCurrentServer(
																		false)) {
																	ctx.getSource()
																			.sendError(
																					Component
																							.translatable(
																									"command.autologin.login.failed.no_password"));
																	return 0;
																}

																ctx.getSource()
																		.sendFeedback(
																				Component
																						.translatable(
																								"command.autologin.toggle.off"));
																return 1;
															}))
									.then(
											ClientCommands.literal("toggle")
													.executes(
															ctx -> {
																AutoLoginConfig.Credential cred =
																		getCurrentServerCredential();

																if (cred == null) {
																	ctx.getSource()
																			.sendError(
																					Component
																							.translatable(
																									"command.autologin.login.failed.no_password"));
																	return 0;
																}

																boolean enabled = !cred.enabled;

																if (!toggleForCurrentServer(
																		enabled)) {
																	ctx.getSource()
																			.sendError(
																					Component
																							.translatable(
																									"command.autologin.login.failed.no_password"));
																	return 0;
																}

																if (enabled) {
																	ctx.getSource()
																			.sendFeedback(
																					Component
																							.translatable(
																									"command.autologin.toggle.on"));
																	return 1;
																}

																ctx.getSource()
																		.sendFeedback(
																				Component
																						.translatable(
																								"command.autologin.toggle.off"));
																return 1;
															})));
				});
	}

	private static LoginAttemptResult tryAutoLogin(Minecraft client) {
		if (client.player == null) {
			return failed("command.autologin.login.failed.no_player");
		}

		String serverKey = getCurrentServerKey(client);

		if (serverKey == null) {
			return failed("command.autologin.login.failed.no_server");
		}

		AutoLoginConfig cfg = AutoLoginConfig.load();
		AutoLoginConfig.Credential cred = cfg.servers.get(serverKey);

		if (cred == null) {
			return failed("command.autologin.login.failed.no_password");
		}

		if (!cred.enabled) {
			return failed("command.autologin.login.failed.disabled");
		}

		byte[] deviceKey = DeviceKey.get();

		if (deviceKey == null) {
			return failed("command.autologin.login.failed.key");
		}

		String password;

		try {
			password = decryptPasswordAndMigrateIfNeeded(cfg, serverKey, cred, deviceKey);
		} catch (Exception e) {
			return failed("command.autologin.login.failed.decrypt");
		}

		var connection = client.getConnection();

		if (connection == null) {
			return failed("command.autologin.login.failed.no_connection");
		}

		try {
			connection.sendCommand("login " + password);
			return sent();
		} catch (RuntimeException e) {
			return failed("command.autologin.login.failed.no_connection");
		}
	}

	private static String decryptPasswordAndMigrateIfNeeded(
			AutoLoginConfig cfg,
			String serverKey,
			AutoLoginConfig.Credential cred,
			byte[] deviceKey)
			throws Exception {
		try {
			return Crypto.decrypt(cred, deviceKey);
		} catch (Exception e) {
			if (cred.salt == null || cred.salt.isEmpty()) {
				throw e;
			}

			char[] legacy =
					(System.getProperty("user.name", "") + System.getProperty("os.name", ""))
							.toCharArray();

			String password = Crypto.decryptLegacy(cred, legacy);

			Crypto.Result result = Crypto.encrypt(password, deviceKey);
			cred.enc = result.enc;
			cred.salt = result.salt;
			cred.iv = result.iv;

			cfg.servers.put(serverKey, cred);
			cfg.save();

			return password;
		}
	}

	private static LoginAttemptResult sent() {
		return new LoginAttemptResult(true, "command.autologin.login.sent");
	}

	private static LoginAttemptResult failed(String translationKey) {
		return new LoginAttemptResult(false, translationKey);
	}

	private static String getCurrentServerKey(Minecraft client) {
		ServerData server = client.getCurrentServer();

		if (server == null) {
			return null;
		}

		if (server.ip == null || server.ip.isBlank()) {
			return null;
		}

		return server.ip;
	}

	private static AutoLoginConfig.Credential getCurrentServerCredential() {
		Minecraft client = Minecraft.getInstance();
		String serverKey = getCurrentServerKey(client);

		if (serverKey == null) {
			return null;
		}

		AutoLoginConfig cfg = AutoLoginConfig.load();
		return cfg.servers.get(serverKey);
	}

	private static boolean toggleForCurrentServer(boolean enabled) {
		Minecraft client = Minecraft.getInstance();
		String serverKey = getCurrentServerKey(client);

		if (serverKey == null) {
			return false;
		}

		AutoLoginConfig cfg = AutoLoginConfig.load();
		AutoLoginConfig.Credential cred = cfg.servers.get(serverKey);

		if (cred == null) {
			return false;
		}

		cred.enabled = enabled;
		cfg.save();
		return true;
	}

	private record LoginAttemptResult(boolean sent, String translationKey) {}
}

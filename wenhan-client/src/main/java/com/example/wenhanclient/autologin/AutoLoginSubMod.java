package com.example.wenhanclient.autologin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

public final class AutoLoginSubMod {
	private static boolean attempted = false;
	private static boolean pendingLogin = false;

	private AutoLoginSubMod() {}

	public static void init() {
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
				(dispatcher, registryAccess) ->
						dispatcher.register(ClientCommands.literal("wc").then(autoLoginNode())));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> autoLoginNode() {
		return ClientCommands.literal("autologin")
				.then(
						ClientCommands.literal("login")
								.executes(
										ctx -> {
											LoginAttemptResult result =
													tryAutoLogin(Minecraft.getInstance());
											if (result.sent()) {
												ctx.getSource()
														.sendFeedback(
																Component.translatable(
																		result.translationKey()));
												return 1;
											}
											ctx.getSource()
													.sendError(
															Component.translatable(
																	result.translationKey()));
											return 0;
										}));
	}

	private static LoginAttemptResult tryAutoLogin(Minecraft client) {
		if (client.player == null) {
			return failed("command.wenhan-client.autologin.login.failed.no_player");
		}
		String serverKey = getCurrentServerKey(client);
		if (serverKey == null) {
			return failed("command.wenhan-client.autologin.login.failed.no_server");
		}
		AutoLoginConfig cfg = AutoLoginConfig.load();
		AutoLoginConfig.Credential cred = cfg.servers.get(serverKey);
		if (cred == null || !cred.hasPassword()) {
			return failed("command.wenhan-client.autologin.login.failed.no_password");
		}
		if (!cred.enabled) {
			return failed("command.wenhan-client.autologin.login.failed.disabled");
		}
		byte[] deviceKey = DeviceKey.get();
		if (deviceKey == null) {
			return failed("command.wenhan-client.autologin.login.failed.key");
		}
		String password;
		try {
			password = decryptPasswordAndMigrateIfNeeded(cfg, serverKey, cred, deviceKey);
		} catch (Exception e) {
			return failed("command.wenhan-client.autologin.login.failed.decrypt");
		}
		var connection = client.getConnection();
		if (connection == null) {
			return failed("command.wenhan-client.autologin.login.failed.no_connection");
		}
		try {
			connection.sendCommand("login " + password);
			return sent();
		} catch (RuntimeException e) {
			return failed("command.wenhan-client.autologin.login.failed.no_connection");
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
		return new LoginAttemptResult(true, "command.wenhan-client.autologin.login.sent");
	}

	private static LoginAttemptResult failed(String translationKey) {
		return new LoginAttemptResult(false, translationKey);
	}

	public static String getCurrentServerKey(Minecraft client) {
		ServerData server = client.getCurrentServer();
		if (server == null || server.ip == null || server.ip.isBlank()) {
			return null;
		}
		return server.ip;
	}

	private record LoginAttemptResult(boolean sent, String translationKey) {}
}

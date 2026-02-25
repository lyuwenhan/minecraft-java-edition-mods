package com.example.autologin;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class AutoLoginMod implements ClientModInitializer {

	private static boolean attempted = false;
	private static boolean pendingLogin = false;

	@Override
	public void onInitializeClient() {

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			attempted = false;
			pendingLogin = true;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!pendingLogin || attempted || client.player == null) {
				return;
			}

			attempted = true;
			pendingLogin = false;

			tryAutoLogin(client);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> {
			dispatcher.register(
				literal("autologin")
					.then(literal("set")
						.then(argument("password", StringArgumentType.greedyString())
							.executes(ctx -> {
								MinecraftClient mc = MinecraftClient.getInstance();
								ServerInfo server = mc.getCurrentServerEntry();
								if (server == null) {
									ctx.getSource().sendFeedback(Text.literal("Not connected to a server."));
									return 0;
								}

								AutoLoginConfig cfg = AutoLoginConfig.load();
								AutoLoginConfig.Credential cred = new AutoLoginConfig.Credential();

								try {
									Crypto.Result r =
										Crypto.encrypt(
											StringArgumentType.getString(ctx, "password"),
											masterKey()
										);
									cred.enc = r.enc;
									cred.salt = r.salt;
									cred.iv = r.iv;
									cred.enabled = true;

									cfg.servers.put(server.address, cred);
									cfg.save();

									ctx.getSource().sendFeedback(Text.translatable("command.autologin.set"));
								} catch (Exception e) {
									ctx.getSource().sendFeedback(Text.literal("Failed to save password."));
								}
								return 1;
							})
						)
					)

					.then(literal("login").executes(ctx -> {
						MinecraftClient mc = MinecraftClient.getInstance();
						mc.execute(() -> tryAutoLogin(mc));
						ctx.getSource().sendFeedback(Text.literal("Auto-login executed."));
						return 1;
					}))

					.then(literal("clear").executes(ctx -> {
						MinecraftClient mc = MinecraftClient.getInstance();
						ServerInfo server = mc.getCurrentServerEntry();
						if (server == null) {
							return 0;
						}

						AutoLoginConfig cfg = AutoLoginConfig.load();
						cfg.servers.remove(server.address);
						cfg.save();

						ctx.getSource().sendFeedback(Text.translatable("command.autologin.clear"));
						return 1;
					}))

					.then(literal("on").executes(ctx -> {
						toggleForCurrentServer(true);
						ctx.getSource().sendFeedback(Text.translatable("command.autologin.toggle.on"));
						return 1;
					}))

					.then(literal("off").executes(ctx -> {
						toggleForCurrentServer(false);
						ctx.getSource().sendFeedback(Text.translatable("command.autologin.toggle.off"));
						return 1;
					}))
			);
		});
	}

	private static void tryAutoLogin(MinecraftClient client) {
		ServerInfo server = client.getCurrentServerEntry();
		if (server == null) {
			return;
		}

		AutoLoginConfig cfg = AutoLoginConfig.load();
		AutoLoginConfig.Credential cred = cfg.servers.get(server.address);

		if (cred == null || !cred.enabled) {
			return;
		}

		try {
			String pwd = Crypto.decrypt(cred, masterKey());
			client.player.networkHandler.sendChatCommand("login " + pwd);
		} catch (Exception ignored) {}
	}

	private static void toggleForCurrentServer(boolean enabled) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ServerInfo server = mc.getCurrentServerEntry();
		if (server == null) {
			return;
		}

		AutoLoginConfig cfg = AutoLoginConfig.load();
		AutoLoginConfig.Credential cred = cfg.servers.get(server.address);
		if (cred == null) {
			return;
		}

		cred.enabled = enabled;
		cfg.save();
	}

	private static char[] masterKey() {
		String s = System.getProperty("user.name") + System.getProperty("os.name");
		return s.toCharArray();
	}
}

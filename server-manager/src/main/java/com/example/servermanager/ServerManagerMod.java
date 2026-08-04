package com.example.servermanager;

import com.example.servermanager.web.*;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerManagerMod implements ModInitializer {
	public static final String MOD_ID = "server_manager";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static WebServer webServer;
	private static BackendService backend;

	@Override
	public void onInitialize() {
		LogBuffer.install();
		LoadedChunkTracker.register();
		ServerManagerCommands.register();
		ServerLifecycleEvents.SERVER_STARTED.register(
				server -> {
					try {
						WebServerConfig config = WebServerConfig.load();
						ServerManagerCommands.setAccountStore(config.accountStore());
						if (!config.enabled()) {
							LOGGER.info("Server Manager web server is disabled");
							return;
						}
						backend = new BackendService(server);
						WebServer instance = new WebServer(LOGGER, config, backend);
						instance.start();
						webServer = instance;
					} catch (Exception exception) {
						LOGGER.error(
								"Failed to start Server Manager web server; aborting Minecraft"
										+ " server startup",
								exception);
						throw new IllegalStateException(
								"Server Manager web server failed to start", exception);
					}
				});
		ServerTickEvents.END_SERVER_TICK.register(
				server -> {
					BackendService service = backend;
					if (service != null) service.tick();
				});
		ServerLifecycleEvents.SERVER_STOPPING.register(
				server -> {
					BackendService service = backend;
					backend = null;
					if (service != null) service.close();
					LoadedChunkTracker.clear();
					ServerManagerCommands.setAccountStore(null);
					WebServer instance = webServer;
					webServer = null;
					if (instance != null) instance.stop();
				});
	}
}

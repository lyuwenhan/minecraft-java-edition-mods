package com.example.autogreetingclient.mixin;

/**
 * The 26.1.2 port no longer uses a packet mixin.
 * Player joins are detected from the client player list during the client tick.
 */
public final class ClientPlayNetworkHandlerMixin {
	private ClientPlayNetworkHandlerMixin() {
	}
}

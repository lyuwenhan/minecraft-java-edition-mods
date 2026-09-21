package com.example.sharedplayerdata;

import carpet.patches.EntityPlayerMPFake;

import net.minecraft.server.level.ServerPlayer;

final class CarpetCompat {
	private CarpetCompat() {}

	static boolean isCarpetFakePlayer(ServerPlayer player) {
		return player instanceof EntityPlayerMPFake;
	}
}

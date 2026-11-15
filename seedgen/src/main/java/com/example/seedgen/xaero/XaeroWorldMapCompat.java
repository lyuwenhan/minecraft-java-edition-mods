package com.example.seedgen.xaero;

import com.example.seedgen.voxy.CompositeGeneratedChunkReceiver;
import com.example.seedgen.voxy.GeneratedChunkReceiver;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;

public final class XaeroWorldMapCompat {
	private XaeroWorldMapCompat() {}

	public static GeneratedChunkReceiver addReceiver(
			GeneratedChunkReceiver current, ClientLevel level) {
		if (!FabricLoader.getInstance().isModLoaded("xaeroworldmap")) {
			XaeroWorldMapWriteBridge.useLevel(null);
			return current;
		}
		XaeroWorldMapWriteBridge.useLevel(level);
		if (!XaeroWorldMapGeneratedChunkReceiver.INSTANCE.isLive()) {
			return current;
		}
		if (current == null) {
			return XaeroWorldMapGeneratedChunkReceiver.INSTANCE;
		}
		return new CompositeGeneratedChunkReceiver(
				current, XaeroWorldMapGeneratedChunkReceiver.INSTANCE);
	}
}

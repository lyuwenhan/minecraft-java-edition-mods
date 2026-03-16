package com.example.autogreetingclient.client.compat;

import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Method;

public final class OptionalClothConfigBridge {
	private static final String PROVIDER_CLASS =
		"com.example.autogreetingclient.client.compat.ClothConfigScreenProvider";

	private OptionalClothConfigBridge() {
	}

	public static Screen create(Screen parent) {
		try {
			Class<?> clazz = Class.forName(PROVIDER_CLASS);
			Method method = clazz.getMethod("create", Screen.class);
			return (Screen) method.invoke(null, parent);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to open Cloth Config screen", e);
		}
	}
}
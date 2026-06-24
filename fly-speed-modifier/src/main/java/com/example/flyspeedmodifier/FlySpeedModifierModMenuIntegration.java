package com.example.flyspeedmodifier;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public final class FlySpeedModifierModMenuIntegration implements ModMenuApi {
	private static final String CLOTH_CONFIG_MOD_ID = "cloth-config";

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (!FabricLoader.getInstance().isModLoaded(CLOTH_CONFIG_MOD_ID)) {
			return parent -> parent;
		}

		return FlySpeedModifierConfigScreen::create;
	}
}

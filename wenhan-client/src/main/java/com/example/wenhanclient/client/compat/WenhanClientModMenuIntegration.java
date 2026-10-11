package com.example.wenhanclient.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.loader.api.FabricLoader;

public final class WenhanClientModMenuIntegration implements ModMenuApi {
	private static final String YACL_MOD_ID = "yet_another_config_lib_v3";

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (!FabricLoader.getInstance().isModLoaded(YACL_MOD_ID)) {
			return null;
		}
		return WenhanClientConfigScreen::create;
	}
}

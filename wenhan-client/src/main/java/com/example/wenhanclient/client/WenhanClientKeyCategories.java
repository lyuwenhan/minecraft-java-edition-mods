package com.example.wenhanclient.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class WenhanClientKeyCategories {
	public static final KeyMapping.Category GENERAL =
			KeyMapping.Category.register(
					Identifier.fromNamespaceAndPath("wenhan-client", "general"));

	private WenhanClientKeyCategories() {}
}

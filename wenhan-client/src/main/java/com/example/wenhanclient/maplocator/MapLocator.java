package com.example.wenhanclient.maplocator;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MapLocator {
	private static final String EXPLORATION_TARGET_KEY = "+";

	private static final Map<String, String> DECORATION_TRANSLATION_KEYS =
			Map.ofEntries(
					Map.entry("minecraft:player", "map_locator.decoration.player"),
					Map.entry("minecraft:frame", "map_locator.decoration.frame"),
					Map.entry("minecraft:red_marker", "map_locator.decoration.red_marker"),
					Map.entry("minecraft:blue_marker", "map_locator.decoration.blue_marker"),
					Map.entry("minecraft:target_x", "map_locator.decoration.target_x"),
					Map.entry("minecraft:target_point", "map_locator.decoration.target_point"),
					Map.entry("minecraft:player_off_map", "map_locator.decoration.player_off_map"),
					Map.entry(
							"minecraft:player_off_limits",
							"map_locator.decoration.player_off_limits"),
					Map.entry("minecraft:mansion", "map_locator.decoration.woodland_mansion"),
					Map.entry("minecraft:monument", "map_locator.decoration.ocean_monument"),
					Map.entry("minecraft:banner_white", "map_locator.decoration.white_banner"),
					Map.entry("minecraft:banner_orange", "map_locator.decoration.orange_banner"),
					Map.entry("minecraft:banner_magenta", "map_locator.decoration.magenta_banner"),
					Map.entry(
							"minecraft:banner_light_blue",
							"map_locator.decoration.light_blue_banner"),
					Map.entry("minecraft:banner_yellow", "map_locator.decoration.yellow_banner"),
					Map.entry("minecraft:banner_lime", "map_locator.decoration.lime_banner"),
					Map.entry("minecraft:banner_pink", "map_locator.decoration.pink_banner"),
					Map.entry("minecraft:banner_gray", "map_locator.decoration.gray_banner"),
					Map.entry(
							"minecraft:banner_light_gray",
							"map_locator.decoration.light_gray_banner"),
					Map.entry("minecraft:banner_cyan", "map_locator.decoration.cyan_banner"),
					Map.entry("minecraft:banner_purple", "map_locator.decoration.purple_banner"),
					Map.entry("minecraft:banner_blue", "map_locator.decoration.blue_banner"),
					Map.entry("minecraft:banner_brown", "map_locator.decoration.brown_banner"),
					Map.entry("minecraft:banner_green", "map_locator.decoration.green_banner"),
					Map.entry("minecraft:banner_red", "map_locator.decoration.red_banner"),
					Map.entry("minecraft:banner_black", "map_locator.decoration.black_banner"),
					Map.entry("minecraft:red_x", "map_locator.decoration.red_x"),
					Map.entry("minecraft:village_desert", "map_locator.decoration.desert_village"),
					Map.entry("minecraft:village_plains", "map_locator.decoration.plains_village"),
					Map.entry(
							"minecraft:village_savanna", "map_locator.decoration.savanna_village"),
					Map.entry("minecraft:village_snowy", "map_locator.decoration.snowy_village"),
					Map.entry("minecraft:village_taiga", "map_locator.decoration.taiga_village"),
					Map.entry("minecraft:jungle_temple", "map_locator.decoration.jungle_temple"),
					Map.entry("minecraft:swamp_hut", "map_locator.decoration.swamp_hut"),
					Map.entry("minecraft:trial_chambers", "map_locator.decoration.trial_chambers"),
					Map.entry("minecraft:abandoned_camp", "map_locator.decoration.abandoned_camp"),
					Map.entry("minecraft:ancient_city", "map_locator.decoration.ancient_city"),
					Map.entry("minecraft:desert_pyramid", "map_locator.decoration.desert_pyramid"),
					Map.entry("minecraft:mineshaft", "map_locator.decoration.mineshaft"),
					Map.entry(
							"minecraft:ocean_ruin_warm",
							"map_locator.decoration.warm_ocean_ruins"));

	private MapLocator() {}

	public static List<Component> appendExactMapInformation(
			ItemStack stack, Item.TooltipContext context, List<Component> original) {
		MapId mapId = stack.get(DataComponents.MAP_ID);
		MapDecorations decorations = stack.get(DataComponents.MAP_DECORATIONS);

		if (mapId == null || decorations == null || decorations.decorations().isEmpty()) {
			return original;
		}

		MapDecorations.Entry explorationTarget =
				decorations.decorations().get(EXPLORATION_TARGET_KEY);
		if (explorationTarget == null) {
			return original;
		}

		MapItemSavedData mapData = context.mapData(mapId);
		if (mapData == null) {
			return original;
		}

		int centerX = calculateMapCenter(explorationTarget.x(), mapData.scale);
		int centerZ = calculateMapCenter(explorationTarget.z(), mapData.scale);

		List<Component> tooltip = new ArrayList<>(original);
		tooltip.add(Component.translatable("tooltip.map-locator.map_center", centerX, centerZ));

		for (MapDecorations.Entry decoration : decorations.decorations().values()) {
			String typeId = decoration.type().getRegisteredName();
			String translationKey = DECORATION_TRANSLATION_KEYS.get(typeId);
			Component typeName =
					translationKey != null
							? Component.translatable(translationKey)
							: Component.literal(typeId);

			tooltip.add(
					Component.translatable(
							"tooltip.map-locator.marker",
							typeName,
							formatCoordinate(decoration.x()),
							formatCoordinate(decoration.z())));
		}

		return tooltip;
	}

	private static int calculateMapCenter(double coordinate, byte scale) {
		int mapSize = 128 * (1 << scale);
		int grid = (int) Math.floor((coordinate + 64.0D) / mapSize);
		return grid * mapSize + mapSize / 2 - 64;
	}

	private static String formatCoordinate(double coordinate) {
		if (coordinate == Math.rint(coordinate)
				&& coordinate >= Long.MIN_VALUE
				&& coordinate <= Long.MAX_VALUE) {
			return Long.toString((long) coordinate);
		}
		return Double.toString(coordinate);
	}
}

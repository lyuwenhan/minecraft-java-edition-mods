package com.example.glideplate;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public final class GlideplateUtil {
	private static final Map<Item, ChestplateLevel> CHESTPLATES =
			Map.of(
					Items.LEATHER_CHESTPLATE, new ChestplateLevel(Items.LEATHER_CHESTPLATE, "leather"),
					Items.CHAINMAIL_CHESTPLATE, new ChestplateLevel(Items.CHAINMAIL_CHESTPLATE, "chainmail"),
					Items.COPPER_CHESTPLATE, new ChestplateLevel(Items.COPPER_CHESTPLATE, "copper"),
					Items.IRON_CHESTPLATE, new ChestplateLevel(Items.IRON_CHESTPLATE, "iron"),
					Items.GOLDEN_CHESTPLATE, new ChestplateLevel(Items.GOLDEN_CHESTPLATE, "golden"),
					Items.DIAMOND_CHESTPLATE, new ChestplateLevel(Items.DIAMOND_CHESTPLATE, "diamond"),
					Items.NETHERITE_CHESTPLATE, new ChestplateLevel(Items.NETHERITE_CHESTPLATE, "netherite"));

	private GlideplateUtil() {}

	public static boolean isChestplate(ItemStack stack) {
		return getChestplateLevel(stack).isPresent();
	}

	public static Optional<ChestplateLevel> getChestplateLevel(ItemStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(CHESTPLATES.get(stack.getItem()));
	}

	public static boolean hasElytra(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		CompoundTag customData = data.copyTag();
		if (customData.getBoolean("gliding").orElse(false)) {
			return true;
		}
		if (customData.getBoolean("glideplate_has_elytra").orElse(false)) {
			return true;
		}
		CustomModelData customModelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
		if (customModelData == null) {
			return false;
		}
		return customModelData.strings().contains("glideplate:with_elytra");
	}

	public static boolean isUsableForGliding(ItemStack stack) {
		return hasElytra(stack);
	}

	public static ItemStack copyAsPlainChestplate(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.remove(DataComponents.CUSTOM_DATA);
		copy.remove(DataComponents.CUSTOM_MODEL_DATA);
		copy.remove(DataComponents.ITEM_MODEL);
		return copy;
	}

	public record ChestplateLevel(Item item, String id) {}
}

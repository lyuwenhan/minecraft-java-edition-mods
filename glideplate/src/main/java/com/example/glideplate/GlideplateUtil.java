package com.example.glideplate;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.Map;
import java.util.Optional;

public final class GlideplateUtil {
    public static final String CUSTOM_MODEL_STRING = GlideplateMod.MOD_ID + ":with_elytra";
    public static final String LEGACY_CUSTOM_MODEL_STRING = GlideplateMod.LEGACY_NAMESPACE + ":with_elytra";

    private static final String GLIDING_KEY = "gliding";
    private static final String MARKER_KEY = "gliding_chestplate_has_elytra";
    private static final Map<Item, ChestplateLevel> CHESTPLATES = Map.of(
            Items.LEATHER_CHESTPLATE, new ChestplateLevel(Items.LEATHER_CHESTPLATE, "leather"),
            Items.CHAINMAIL_CHESTPLATE, new ChestplateLevel(Items.CHAINMAIL_CHESTPLATE, "chainmail"),
            Items.COPPER_CHESTPLATE, new ChestplateLevel(Items.COPPER_CHESTPLATE, "copper"),
            Items.IRON_CHESTPLATE, new ChestplateLevel(Items.IRON_CHESTPLATE, "iron"),
            Items.GOLDEN_CHESTPLATE, new ChestplateLevel(Items.GOLDEN_CHESTPLATE, "golden"),
            Items.DIAMOND_CHESTPLATE, new ChestplateLevel(Items.DIAMOND_CHESTPLATE, "diamond"),
            Items.NETHERITE_CHESTPLATE, new ChestplateLevel(Items.NETHERITE_CHESTPLATE, "netherite")
    );

    private GlideplateUtil() {
    }

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

        NbtCompound customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (customData.getBoolean(GLIDING_KEY, false) || customData.getBoolean(MARKER_KEY, false)) {
            return true;
        }

        CustomModelDataComponent customModelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        return customModelData != null
                && (customModelData.strings().contains(CUSTOM_MODEL_STRING)
                || customModelData.strings().contains(LEGACY_CUSTOM_MODEL_STRING));
    }

    public static boolean isUsableForGliding(ItemStack stack) {
        return hasElytra(stack);
    }

    public static ItemStack copyAsPlainChestplate(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.remove(DataComponentTypes.CUSTOM_DATA);
        copy.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        copy.remove(DataComponentTypes.ITEM_MODEL);
        return copy;
    }

    public record ChestplateLevel(Item item, String id) {
    }
}

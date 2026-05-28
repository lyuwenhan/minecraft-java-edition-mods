package com.example.glideplateserver;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GlideplateServerUtil {
    public static final float CUSTOM_MODEL_DATA_NUMBER = 121211.0F;
    private static final Text LORE_LINE = Text.translatableWithFallback(
            "tooltip.glideplate.with_elytra",
            "With Elytra"
    ).styled(style -> style
        .withColor(Formatting.GRAY)
        .withItalic(false)
    );
    private static final Map<Item, ChestplateLevel> CHESTPLATES = Map.of(
            Items.LEATHER_CHESTPLATE, new ChestplateLevel(Items.LEATHER_CHESTPLATE, "leather", "Leather"),
            Items.CHAINMAIL_CHESTPLATE, new ChestplateLevel(Items.CHAINMAIL_CHESTPLATE, "chainmail", "Chainmail"),
            Items.COPPER_CHESTPLATE, new ChestplateLevel(Items.COPPER_CHESTPLATE, "copper", "Copper"),
            Items.IRON_CHESTPLATE, new ChestplateLevel(Items.IRON_CHESTPLATE, "iron", "Iron"),
            Items.GOLDEN_CHESTPLATE, new ChestplateLevel(Items.GOLDEN_CHESTPLATE, "golden", "Golden"),
            Items.DIAMOND_CHESTPLATE, new ChestplateLevel(Items.DIAMOND_CHESTPLATE, "diamond", "Diamond"),
            Items.NETHERITE_CHESTPLATE, new ChestplateLevel(Items.NETHERITE_CHESTPLATE, "netherite", "Netherite")
    );

    private GlideplateServerUtil() {
    }

    public static Optional<ChestplateLevel> getChestplateLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(CHESTPLATES.get(stack.getItem()));
    }

    public static boolean canCombine(ItemStack left, ItemStack right) {
        return !left.isEmpty()
                && !right.isEmpty()
                && right.isOf(Items.ELYTRA)
                && getChestplateLevel(left).isPresent()
                && !hasElytra(left);
    }

    public static boolean isBlockedDoubleCombine(ItemStack left, ItemStack right) {
        return !left.isEmpty()
                && !right.isEmpty()
                && right.isOf(Items.ELYTRA)
                && getChestplateLevel(left).isPresent()
                && hasElytra(left);
    }

    public static boolean hasElytra(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        NbtCompound customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        if (customData.getBoolean("gliding", false) || customData.getBoolean("glideplate_has_elytra", false)) {
            return true;
        }

        CustomModelDataComponent customModelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        return customModelData != null
                && (customModelData.strings().contains("glideplate:with_elytra")
                || customModelData.strings().contains("glideplate:with_elytra"));
    }

    public static boolean isUsableForGliding(ItemStack stack) {
        return hasElytra(stack);
    }

    public static ItemStack createGlideplateServer(ItemStack chestplate) {
        ChestplateLevel level = getChestplateLevel(chestplate).orElseThrow();
        ItemStack result = chestplate.copy();
        result.setCount(1);

        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, result, nbt -> {
            nbt.putBoolean("gliding", true);
            nbt.putBoolean("glideplate_has_elytra", true);
            nbt.putString("gliding_chestplate_has_elytra", level.id());
        });
        result.set(DataComponentTypes.ITEM_NAME, Text.translatableWithFallback(
                "item.glideplate." + level.id() + "_chestplate_with_elytra",
                level.englishName() + " Chestplate with Elytra"
        ));
        result.set(DataComponentTypes.LORE, result.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).with(LORE_LINE));
        result.set(DataComponentTypes.CUSTOM_MODEL_DATA, mergeCustomModelData(result, level));
        result.set(DataComponentTypes.GLIDER, Unit.INSTANCE);
        result.set(DataComponentTypes.REPAIR_COST, 0);
        return result;
    }

    private static CustomModelDataComponent mergeCustomModelData(ItemStack stack, ChestplateLevel level) {
        CustomModelDataComponent existing = stack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT);
        List<Float> floats = new ArrayList<>(existing.floats());
        List<Boolean> flags = new ArrayList<>(existing.flags());
        List<String> strings = new ArrayList<>(existing.strings());
        List<Integer> colors = new ArrayList<>(existing.colors());

        if (!floats.contains(CUSTOM_MODEL_DATA_NUMBER)) {
            floats.add(CUSTOM_MODEL_DATA_NUMBER);
        }
        addStringTag(strings, "glideplate:with_elytra");
        addStringTag(strings, "glideplate:" + level.id());

        return new CustomModelDataComponent(List.copyOf(floats), List.copyOf(flags), List.copyOf(strings), List.copyOf(colors));
    }

    private static void addStringTag(List<String> strings, String tag) {
        if (!strings.contains(tag)) {
            strings.add(tag);
        }
    }

    public record ChestplateLevel(Item item, String id, String englishName) {
    }
}

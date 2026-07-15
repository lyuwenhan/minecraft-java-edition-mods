package com.example.glideplateserver;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GlideplateServerUtil {
    public static final float CUSTOM_MODEL_DATA_NUMBER = 121211.0F;

    private static final Component LORE_LINE =
            Component.translatableWithFallback("tooltip.glideplate.with_elytra", "With Elytra")
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false));

    private static final Map<Item, ChestplateLevel> CHESTPLATES =
            Map.of(
                    Items.LEATHER_CHESTPLATE,
                            new ChestplateLevel(Items.LEATHER_CHESTPLATE, "leather", "Leather"),
                    Items.CHAINMAIL_CHESTPLATE,
                            new ChestplateLevel(
                                    Items.CHAINMAIL_CHESTPLATE, "chainmail", "Chainmail"),
                    Items.COPPER_CHESTPLATE,
                            new ChestplateLevel(Items.COPPER_CHESTPLATE, "copper", "Copper"),
                    Items.IRON_CHESTPLATE,
                            new ChestplateLevel(Items.IRON_CHESTPLATE, "iron", "Iron"),
                    Items.GOLDEN_CHESTPLATE,
                            new ChestplateLevel(Items.GOLDEN_CHESTPLATE, "golden", "Golden"),
                    Items.DIAMOND_CHESTPLATE,
                            new ChestplateLevel(Items.DIAMOND_CHESTPLATE, "diamond", "Diamond"),
                    Items.NETHERITE_CHESTPLATE,
                            new ChestplateLevel(
                                    Items.NETHERITE_CHESTPLATE, "netherite", "Netherite"));

    private GlideplateServerUtil() {}

    public static Optional<ChestplateLevel> getChestplateLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(CHESTPLATES.get(stack.getItem()));
    }

    public static boolean canCombine(ItemStack left, ItemStack right) {
        return !left.isEmpty()
                && !right.isEmpty()
                && right.is(Items.ELYTRA)
                && getChestplateLevel(left).isPresent()
                && !hasElytra(left);
    }

    public static boolean isBlockedDoubleCombine(ItemStack left, ItemStack right) {
        return !left.isEmpty()
                && !right.isEmpty()
                && right.is(Items.ELYTRA)
                && getChestplateLevel(left).isPresent()
                && hasElytra(left);
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

    public static ItemStack createGlideplateServer(ItemStack chestplate) {
        ChestplateLevel level = getChestplateLevel(chestplate).orElseThrow();
        ItemStack result = chestplate.copy();
        result.setCount(1);

        CustomData.update(
                DataComponents.CUSTOM_DATA,
                result,
                tag -> {
                    tag.putBoolean("gliding", true);
                    tag.putBoolean("glideplate_has_elytra", true);
                    tag.putString("gliding_chestplate_has_elytra", level.id());
                });

        result.set(
                DataComponents.ITEM_NAME,
                Component.translatableWithFallback(
                        "item.glideplate." + level.id() + "_chestplate_with_elytra",
                        level.englishName() + " Chestplate with Elytra"));

        ItemLore lore = result.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        result.set(DataComponents.LORE, lore.withLineAdded(LORE_LINE));
        result.set(DataComponents.CUSTOM_MODEL_DATA, mergeCustomModelData(result, level));
        result.set(DataComponents.GLIDER, Unit.INSTANCE);
        result.set(DataComponents.REPAIR_COST, 0);
        return result;
    }

    private static CustomModelData mergeCustomModelData(ItemStack stack, ChestplateLevel level) {
        CustomModelData existing =
                stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);
        List<Float> floats = new ArrayList<>(existing.floats());
        List<Boolean> flags = new ArrayList<>(existing.flags());
        List<String> strings = new ArrayList<>(existing.strings());
        List<Integer> colors = new ArrayList<>(existing.colors());

        if (!floats.contains(CUSTOM_MODEL_DATA_NUMBER)) {
            floats.add(CUSTOM_MODEL_DATA_NUMBER);
        }

        addStringTag(strings, "glideplate:with_elytra");
        addStringTag(strings, "glideplate:" + level.id());

        return new CustomModelData(
                List.copyOf(floats), List.copyOf(flags), List.copyOf(strings), List.copyOf(colors));
    }

    private static void addStringTag(List<String> strings, String tag) {
        if (!strings.contains(tag)) {
            strings.add(tag);
        }
    }

    public record ChestplateLevel(Item item, String id, String englishName) {}
}

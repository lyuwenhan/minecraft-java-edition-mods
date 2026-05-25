package com.example.elytrachestplate;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.Set;
import java.util.function.UnaryOperator;

public final class ModItems {
    private static final Set<Item> ELYTRA_CHESTPLATES;

    public static final Item LEATHER_CHESTPLATE_WITH_ELYTRA = registerChestplate("leather_chestplate_with_elytra", ArmorMaterials.LEATHER, UnaryOperator.identity());
    public static final Item COPPER_CHESTPLATE_WITH_ELYTRA = registerChestplate("copper_chestplate_with_elytra", ArmorMaterials.COPPER, UnaryOperator.identity());
    public static final Item CHAINMAIL_CHESTPLATE_WITH_ELYTRA = registerChestplate("chainmail_chestplate_with_elytra", ArmorMaterials.CHAIN, UnaryOperator.identity());
    public static final Item IRON_CHESTPLATE_WITH_ELYTRA = registerChestplate("iron_chestplate_with_elytra", ArmorMaterials.IRON, UnaryOperator.identity());
    public static final Item GOLDEN_CHESTPLATE_WITH_ELYTRA = registerChestplate("golden_chestplate_with_elytra", ArmorMaterials.GOLD, UnaryOperator.identity());
    public static final Item DIAMOND_CHESTPLATE_WITH_ELYTRA = registerChestplate("diamond_chestplate_with_elytra", ArmorMaterials.DIAMOND, UnaryOperator.identity());
    public static final Item NETHERITE_CHESTPLATE_WITH_ELYTRA = registerChestplate("netherite_chestplate_with_elytra", ArmorMaterials.NETHERITE, Item.Settings::fireproof);

    static {
        ELYTRA_CHESTPLATES = Set.of(
                LEATHER_CHESTPLATE_WITH_ELYTRA,
                COPPER_CHESTPLATE_WITH_ELYTRA,
                CHAINMAIL_CHESTPLATE_WITH_ELYTRA,
                IRON_CHESTPLATE_WITH_ELYTRA,
                GOLDEN_CHESTPLATE_WITH_ELYTRA,
                DIAMOND_CHESTPLATE_WITH_ELYTRA,
                NETHERITE_CHESTPLATE_WITH_ELYTRA
        );
    }

    private ModItems() {
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(LEATHER_CHESTPLATE_WITH_ELYTRA);
            entries.add(COPPER_CHESTPLATE_WITH_ELYTRA);
            entries.add(CHAINMAIL_CHESTPLATE_WITH_ELYTRA);
            entries.add(IRON_CHESTPLATE_WITH_ELYTRA);
            entries.add(GOLDEN_CHESTPLATE_WITH_ELYTRA);
            entries.add(DIAMOND_CHESTPLATE_WITH_ELYTRA);
            entries.add(NETHERITE_CHESTPLATE_WITH_ELYTRA);
        });
    }

    public static boolean isElytraChestplate(Item item) {
        return ELYTRA_CHESTPLATES.contains(item);
    }

    public static Item fromVanillaChestplate(Item item) {
        if (item == Items.LEATHER_CHESTPLATE) {
            return LEATHER_CHESTPLATE_WITH_ELYTRA;
        }

        if (item == Items.COPPER_CHESTPLATE) {
            return COPPER_CHESTPLATE_WITH_ELYTRA;
        }

        if (item == Items.CHAINMAIL_CHESTPLATE) {
            return CHAINMAIL_CHESTPLATE_WITH_ELYTRA;
        }

        if (item == Items.IRON_CHESTPLATE) {
            return IRON_CHESTPLATE_WITH_ELYTRA;
        }

        if (item == Items.GOLDEN_CHESTPLATE) {
            return GOLDEN_CHESTPLATE_WITH_ELYTRA;
        }

        if (item == Items.DIAMOND_CHESTPLATE) {
            return DIAMOND_CHESTPLATE_WITH_ELYTRA;
        }

        if (item == Items.NETHERITE_CHESTPLATE) {
            return NETHERITE_CHESTPLATE_WITH_ELYTRA;
        }

        return null;
    }

    public static void applyElytraChestplateDefaultComponents(ItemStack stack) {
        ItemStack defaultStack = stack.getItem().getDefaultStack();
        EquippableComponent equippable = defaultStack.get(DataComponentTypes.EQUIPPABLE);
        Identifier itemModel = defaultStack.get(DataComponentTypes.ITEM_MODEL);
        Unit glider = defaultStack.get(DataComponentTypes.GLIDER);

        if (equippable != null) {
            stack.set(DataComponentTypes.EQUIPPABLE, equippable);
        }

        if (itemModel != null) {
            stack.set(DataComponentTypes.ITEM_MODEL, itemModel);
        }

        if (glider != null) {
            stack.set(DataComponentTypes.GLIDER, glider);
        }
    }

    private static Item registerChestplate(String name, ArmorMaterial material, UnaryOperator<Item.Settings> settingsOperator) {
        Identifier id = Identifier.of(ElytraChestplateMod.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        RegistryKey<EquipmentAsset> equipmentAssetKey = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, id);
        EquippableComponent equippable = EquippableComponent.builder(EquipmentSlot.CHEST)
                .equipSound(material.equipSound())
                .model(equipmentAssetKey)
                .dispensable(true)
                .swappable(true)
                .damageOnHurt(true)
                .build();
        Item.Settings settings = new Item.Settings()
                .registryKey(itemKey)
                .armor(material, EquipmentType.CHESTPLATE)
                .component(DataComponentTypes.EQUIPPABLE, equippable)
                .component(DataComponentTypes.GLIDER, Unit.INSTANCE);

        settings = settingsOperator.apply(settings);

        return Registry.register(Registries.ITEM, itemKey, new Item(settings));
    }
}

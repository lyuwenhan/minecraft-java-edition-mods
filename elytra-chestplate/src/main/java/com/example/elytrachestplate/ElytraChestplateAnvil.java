package com.example.elytrachestplate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class ElytraChestplateAnvil {
    private ElytraChestplateAnvil() {
    }

    public static ItemStack createResult(ItemStack first, ItemStack second) {
        ItemStack chestplate = ItemStack.EMPTY;

        if (isVanillaChestplate(first) && second.isOf(Items.ELYTRA)) {
            chestplate = first;
        } else if (first.isOf(Items.ELYTRA) && isVanillaChestplate(second)) {
            chestplate = second;
        } else {
            return ItemStack.EMPTY;
        }

        Item resultItem = ModItems.fromVanillaChestplate(chestplate.getItem());

        if (resultItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = chestplate.copyComponentsToNewStack(resultItem, 1);
        ModItems.applyElytraChestplateDefaultComponents(result);

        if (chestplate.isDamageable()) {
            result.setDamage(chestplate.getDamage());
        }

        return result;
    }

    public static boolean isCombination(ItemStack first, ItemStack second) {
        return !createResult(first, second).isEmpty();
    }

    public static boolean isElytraChestplate(ItemStack stack) {
        return ModItems.isElytraChestplate(stack.getItem());
    }


    private static boolean isVanillaChestplate(ItemStack stack) {
        return ModItems.fromVanillaChestplate(stack.getItem()) != null;
    }
}

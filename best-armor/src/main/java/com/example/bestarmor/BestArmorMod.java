package com.example.bestarmor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class BestArmorMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.OP_BLOCKS)
				.register(
						output -> {
							if (output.shouldShowOpRestrictedItems()) {
								addOperatorTabItem(output);
							}
						});
	}

	private static void addOperatorTabItem(FabricCreativeModeTabOutput output) {
		ClientLevel level = Minecraft.getInstance().level;

		if (level == null) {
			return;
		}

		HolderLookup.Provider registries = level.registryAccess();
		output.accept(
				createYellowShulkerBox(registries), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		output.accept(
				createOrangeShulkerBox(registries), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		output.accept(
				createRedShulkerBox(registries), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	private static ItemStack createYellowShulkerBox(HolderLookup.Provider registries) {
		List<ItemStack> containerItems = createContainerItems(27);

		ItemStack slot0Stack =
				createItemStack(
						Items.NETHERITE_SWORD,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.SWEEPING_EDGE, 3),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.KNOCKBACK, 2),
						enchantment(Enchantments.LOOTING, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(0, slot0Stack);

		ItemStack slot1Stack =
				createItemStack(
						Items.NETHERITE_AXE,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(1, slot1Stack);

		ItemStack slot2Stack =
				createItemStack(
						Items.NETHERITE_PICKAXE,
						registries,
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(2, slot2Stack);

		ItemStack slot3Stack =
				createItemStack(
						Items.NETHERITE_SHOVEL,
						registries,
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(3, slot3Stack);

		ItemStack slot4Stack =
				createItemStack(
						Items.NETHERITE_HOE,
						registries,
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(4, slot4Stack);

		ItemStack slot5Stack =
				createItemStack(
						Items.BOW,
						registries,
						enchantment(Enchantments.POWER, 5),
						enchantment(Enchantments.FLAME, 1),
						enchantment(Enchantments.PUNCH, 2),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.INFINITY, 1),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(5, slot5Stack);

		ItemStack slot6Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(6, slot6Stack);

		ItemStack slot7Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.RIPTIDE, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(7, slot7Stack);

		ItemStack slot8Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.CHANNELING, 1),
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(8, slot8Stack);

		ItemStack slot9Stack =
				createItemStack(
						Items.NETHERITE_SPEAR,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.KNOCKBACK, 2),
						enchantment(Enchantments.LOOTING, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.LUNGE, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(9, slot9Stack);

		ItemStack slot10Stack =
				createItemStack(
						Items.NETHERITE_AXE,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(10, slot10Stack);

		ItemStack slot11Stack =
				createItemStack(
						Items.NETHERITE_PICKAXE,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(11, slot11Stack);

		ItemStack slot12Stack =
				createItemStack(
						Items.NETHERITE_SHOVEL,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(12, slot12Stack);

		ItemStack slot13Stack =
				createItemStack(
						Items.NETHERITE_HOE,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(13, slot13Stack);

		ItemStack slot14Stack =
				createItemStack(
						Items.CROSSBOW,
						registries,
						enchantment(Enchantments.PIERCING, 4),
						enchantment(Enchantments.QUICK_CHARGE, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(14, slot14Stack);

		ItemStack slot15Stack =
				createItemStack(
						Items.MACE,
						registries,
						enchantment(Enchantments.DENSITY, 5),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(15, slot15Stack);

		ItemStack slot16Stack =
				createItemStack(
						Items.MACE,
						registries,
						enchantment(Enchantments.BREACH, 4),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(16, slot16Stack);

		ItemStack slot17Stack =
				createItemStack(
						Items.MACE,
						registries,
						enchantment(Enchantments.WIND_BURST, 3),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(17, slot17Stack);

		ItemStack slot18Stack =
				createItemStack(
						Items.NETHERITE_HELMET,
						registries,
						enchantment(Enchantments.PROTECTION, 4),
						enchantment(Enchantments.RESPIRATION, 3),
						enchantment(Enchantments.AQUA_AFFINITY, 1),
						enchantment(Enchantments.THORNS, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(18, slot18Stack);

		ItemStack slot19Stack =
				createItemStack(
						Items.NETHERITE_CHESTPLATE,
						registries,
						enchantment(Enchantments.PROTECTION, 4),
						enchantment(Enchantments.THORNS, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(19, slot19Stack);

		ItemStack slot20Stack =
				createItemStack(
						Items.NETHERITE_LEGGINGS,
						registries,
						enchantment(Enchantments.PROTECTION, 4),
						enchantment(Enchantments.SWIFT_SNEAK, 3),
						enchantment(Enchantments.THORNS, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(20, slot20Stack);

		ItemStack slot21Stack =
				createItemStack(
						Items.NETHERITE_BOOTS,
						registries,
						enchantment(Enchantments.PROTECTION, 4),
						enchantment(Enchantments.FEATHER_FALLING, 4),
						enchantment(Enchantments.SOUL_SPEED, 3),
						enchantment(Enchantments.DEPTH_STRIDER, 3),
						enchantment(Enchantments.THORNS, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(21, slot21Stack);

		ItemStack elytraStack = new ItemStack(Items.ELYTRA);
		containerItems.set(22, elytraStack);

		ItemStack glideplateChestplateStack =
				createItemStack(
						Items.NETHERITE_CHESTPLATE,
						registries,
						enchantment(Enchantments.PROTECTION, 4),
						enchantment(Enchantments.THORNS, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		applyGlideplateData(glideplateChestplateStack);
		containerItems.set(23, glideplateChestplateStack);

		ItemStack slot22Stack =
				createItemStack(
						Items.NETHERITE_BOOTS,
						registries,
						enchantment(Enchantments.PROTECTION, 4),
						enchantment(Enchantments.FEATHER_FALLING, 4),
						enchantment(Enchantments.SOUL_SPEED, 3),
						enchantment(Enchantments.FROST_WALKER, 2),
						enchantment(Enchantments.THORNS, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(24, slot22Stack);

		ItemStack slot23Stack =
				createItemStack(
						Items.SHIELD,
						registries,
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(25, slot23Stack);

		ItemStack slot24Stack =
				createItemStack(
						Items.FISHING_ROD,
						registries,
						enchantment(Enchantments.LUCK_OF_THE_SEA, 3),
						enchantment(Enchantments.LURE, 3),
						enchantment(Enchantments.UNBREAKING, 3),
						enchantment(Enchantments.MENDING, 1));
		containerItems.set(26, slot24Stack);

		ItemStack box = new ItemStack(Items.YELLOW_SHULKER_BOX);
		box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(containerItems));
		return box;
	}

	private static ItemStack createOrangeShulkerBox(HolderLookup.Provider registries) {
		List<ItemStack> containerItems = createContainerItems(26);

		ItemStack slot0Stack =
				createItemStack(
						Items.NETHERITE_SWORD,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.SMITE, 5),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 5),
						enchantment(Enchantments.SWEEPING_EDGE, 255),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.KNOCKBACK, 2),
						enchantment(Enchantments.LOOTING, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot0Stack);
		containerItems.set(0, slot0Stack);

		ItemStack slot1Stack =
				createItemStack(
						Items.NETHERITE_AXE,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.SMITE, 5),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 5),
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot1Stack);
		containerItems.set(1, slot1Stack);

		ItemStack slot2Stack =
				createItemStack(
						Items.NETHERITE_PICKAXE,
						registries,
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot2Stack);
		containerItems.set(2, slot2Stack);

		ItemStack slot3Stack =
				createItemStack(
						Items.NETHERITE_SHOVEL,
						registries,
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot3Stack);
		containerItems.set(3, slot3Stack);

		ItemStack slot4Stack =
				createItemStack(
						Items.NETHERITE_HOE,
						registries,
						enchantment(Enchantments.FORTUNE, 3),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot4Stack);
		containerItems.set(4, slot4Stack);

		ItemStack slot5Stack =
				createItemStack(
						Items.BOW,
						registries,
						enchantment(Enchantments.POWER, 5),
						enchantment(Enchantments.FLAME, 1),
						enchantment(Enchantments.PUNCH, 2),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.INFINITY, 1),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot5Stack);
		containerItems.set(5, slot5Stack);

		ItemStack slot6Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot6Stack);
		containerItems.set(6, slot6Stack);

		ItemStack slot7Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.RIPTIDE, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot7Stack);
		containerItems.set(7, slot7Stack);

		ItemStack slot8Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.CHANNELING, 1),
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot8Stack);
		containerItems.set(8, slot8Stack);

		ItemStack slot9Stack =
				createItemStack(
						Items.NETHERITE_SPEAR,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.SMITE, 5),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 5),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.KNOCKBACK, 2),
						enchantment(Enchantments.LOOTING, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.LUNGE, 3),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot9Stack);
		containerItems.set(9, slot9Stack);

		ItemStack slot10Stack =
				createItemStack(
						Items.NETHERITE_AXE,
						registries,
						enchantment(Enchantments.SHARPNESS, 5),
						enchantment(Enchantments.SMITE, 5),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 5),
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot10Stack);
		containerItems.set(10, slot10Stack);

		ItemStack slot11Stack =
				createItemStack(
						Items.NETHERITE_PICKAXE,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot11Stack);
		containerItems.set(11, slot11Stack);

		ItemStack slot12Stack =
				createItemStack(
						Items.NETHERITE_SHOVEL,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot12Stack);
		containerItems.set(12, slot12Stack);

		ItemStack slot13Stack =
				createItemStack(
						Items.NETHERITE_HOE,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot13Stack);
		containerItems.set(13, slot13Stack);

		ItemStack slot14Stack =
				createItemStack(
						Items.CROSSBOW,
						registries,
						enchantment(Enchantments.MULTISHOT, 1),
						enchantment(Enchantments.PIERCING, 10),
						enchantment(Enchantments.QUICK_CHARGE, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.INFINITY, 1),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot14Stack);
		containerItems.set(14, slot14Stack);

		ItemStack slot15Stack =
				createItemStack(
						Items.MACE,
						registries,
						enchantment(Enchantments.DENSITY, 5),
						enchantment(Enchantments.BREACH, 4),
						enchantment(Enchantments.SMITE, 5),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 5),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot15Stack);
		containerItems.set(15, slot15Stack);

		ItemStack slot16Stack =
				createItemStack(
						Items.MACE,
						registries,
						enchantment(Enchantments.WIND_BURST, 3),
						enchantment(Enchantments.DENSITY, 5),
						enchantment(Enchantments.BREACH, 4),
						enchantment(Enchantments.SMITE, 5),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 5),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot16Stack);
		containerItems.set(16, slot16Stack);

		ItemStack slot17Stack =
				createItemStack(
						Items.SHIELD,
						registries,
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot17Stack);

		HolderLookup.RegistryLookup<DamageType> damageTypes =
				registries.lookupOrThrow(Registries.DAMAGE_TYPE);

		HolderSet<DamageType> blockedDamageTypes =
				HolderSet.direct(damageTypes.listElements().toList());

		slot17Stack.set(
				DataComponents.BLOCKS_ATTACKS,
				new BlocksAttacks(
						0.0F,
						0.0F,
						List.of(
								new BlocksAttacks.DamageReduction(
										360.0F, Optional.of(blockedDamageTypes), 1000.0F, 1000.0F)),
						new BlocksAttacks.ItemDamageFunction(0.0F, 0.0F, 0.0F),
						Optional.empty(),
						Optional.empty(),
						Optional.empty()));

		containerItems.set(17, slot17Stack);

		ItemStack slot18Stack =
				createItemStack(
						Items.NETHERITE_HELMET,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.RESPIRATION, 255),
						enchantment(Enchantments.AQUA_AFFINITY, 1),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot18Stack);
		containerItems.set(18, slot18Stack);

		ItemStack slot19Stack =
				createItemStack(
						Items.NETHERITE_CHESTPLATE,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot19Stack);
		containerItems.set(19, slot19Stack);

		ItemStack slot20Stack =
				createItemStack(
						Items.NETHERITE_LEGGINGS,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.SWIFT_SNEAK, 3),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot20Stack);
		containerItems.set(20, slot20Stack);

		ItemStack slot21Stack =
				createItemStack(
						Items.NETHERITE_BOOTS,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.FEATHER_FALLING, 4),
						enchantment(Enchantments.SOUL_SPEED, 3),
						enchantment(Enchantments.DEPTH_STRIDER, 3),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot21Stack);
		setAttributeModifiers(
				slot21Stack,
				attributeModifier(
						Attributes.ARMOR,
						"custom_netherite_boots2_armor",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.ARMOR_TOUGHNESS,
						"custom_netherite_boots2_armor_toughness",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.KNOCKBACK_RESISTANCE,
						"custom_netherite_boots2_knockback_resistance",
						0.1D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.FALL_DAMAGE_MULTIPLIER,
						"no_fall_damage_boots_frost",
						-1.0D,
						AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
						EquipmentSlotGroup.FEET));
		containerItems.set(21, slot21Stack);

		ItemStack elytraStack = new ItemStack(Items.ELYTRA);
		containerItems.set(22, elytraStack);

		ItemStack glideplateChestplateStack =
				createItemStack(
						Items.NETHERITE_CHESTPLATE,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(glideplateChestplateStack);
		applyGlideplateData(glideplateChestplateStack);
		containerItems.set(23, glideplateChestplateStack);

		ItemStack slot22Stack =
				createItemStack(
						Items.NETHERITE_BOOTS,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.FEATHER_FALLING, 4),
						enchantment(Enchantments.SOUL_SPEED, 3),
						enchantment(Enchantments.FROST_WALKER, 2),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot22Stack);
		setAttributeModifiers(
				slot22Stack,
				attributeModifier(
						Attributes.ARMOR,
						"custom_netherite_boots2_armor",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.ARMOR_TOUGHNESS,
						"custom_netherite_boots2_armor_toughness",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.KNOCKBACK_RESISTANCE,
						"custom_netherite_boots2_knockback_resistance",
						0.1D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.FALL_DAMAGE_MULTIPLIER,
						"no_fall_damage_boots_frost",
						-1.0D,
						AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
						EquipmentSlotGroup.FEET));
		containerItems.set(24, slot22Stack);

		ItemStack slot23Stack =
				createItemStack(
						Items.FISHING_ROD,
						registries,
						enchantment(Enchantments.LUCK_OF_THE_SEA, 3),
						enchantment(Enchantments.LURE, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot23Stack);
		containerItems.set(25, slot23Stack);

		ItemStack box = new ItemStack(Items.ORANGE_SHULKER_BOX);
		box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(containerItems));
		return box;
	}

	private static ItemStack createRedShulkerBox(HolderLookup.Provider registries) {
		List<ItemStack> containerItems = createContainerItems(26);

		ItemStack slot0Stack =
				createItemStack(
						Items.NETHERITE_SWORD,
						registries,
						enchantment(Enchantments.SHARPNESS, 255),
						enchantment(Enchantments.SMITE, 255),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 255),
						enchantment(Enchantments.SWEEPING_EDGE, 255),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.KNOCKBACK, 2),
						enchantment(Enchantments.LOOTING, 10),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot0Stack);
		setAttributeModifiers(
				slot0Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(0, slot0Stack);

		ItemStack slot1Stack =
				createItemStack(
						Items.NETHERITE_AXE,
						registries,
						enchantment(Enchantments.SHARPNESS, 255),
						enchantment(Enchantments.SMITE, 255),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 255),
						enchantment(Enchantments.FORTUNE, 10),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot1Stack);
		setAttributeModifiers(
				slot1Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(1, slot1Stack);

		ItemStack slot2Stack =
				createItemStack(
						Items.NETHERITE_PICKAXE,
						registries,
						enchantment(Enchantments.FORTUNE, 10),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot2Stack);
		containerItems.set(2, slot2Stack);

		ItemStack slot3Stack =
				createItemStack(
						Items.NETHERITE_SHOVEL,
						registries,
						enchantment(Enchantments.FORTUNE, 10),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot3Stack);
		containerItems.set(3, slot3Stack);

		ItemStack slot4Stack =
				createItemStack(
						Items.NETHERITE_HOE,
						registries,
						enchantment(Enchantments.FORTUNE, 10),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot4Stack);
		containerItems.set(4, slot4Stack);

		ItemStack slot5Stack =
				createItemStack(
						Items.BOW,
						registries,
						enchantment(Enchantments.POWER, 255),
						enchantment(Enchantments.FLAME, 1),
						enchantment(Enchantments.PUNCH, 2),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.INFINITY, 1),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot5Stack);
		containerItems.set(5, slot5Stack);

		ItemStack slot6Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot6Stack);
		setAttributeModifiers(
				slot6Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(6, slot6Stack);

		ItemStack slot7Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.RIPTIDE, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot7Stack);
		setAttributeModifiers(
				slot7Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(7, slot7Stack);

		ItemStack slot8Stack =
				createItemStack(
						Items.TRIDENT,
						registries,
						enchantment(Enchantments.CHANNELING, 1),
						enchantment(Enchantments.IMPALING, 5),
						enchantment(Enchantments.LOYALTY, 3),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot8Stack);
		setAttributeModifiers(
				slot8Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(8, slot8Stack);

		ItemStack slot9Stack =
				createItemStack(
						Items.NETHERITE_SPEAR,
						registries,
						enchantment(Enchantments.SHARPNESS, 255),
						enchantment(Enchantments.SMITE, 255),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 255),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.KNOCKBACK, 2),
						enchantment(Enchantments.LOOTING, 10),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.LUNGE, 3),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot9Stack);
		setAttributeModifiers(
				slot9Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(9, slot9Stack);

		ItemStack slot10Stack =
				createItemStack(
						Items.NETHERITE_AXE,
						registries,
						enchantment(Enchantments.SHARPNESS, 255),
						enchantment(Enchantments.SMITE, 255),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 255),
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot10Stack);
		containerItems.set(10, slot10Stack);

		ItemStack slot11Stack =
				createItemStack(
						Items.NETHERITE_PICKAXE,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot11Stack);
		containerItems.set(11, slot11Stack);

		ItemStack slot12Stack =
				createItemStack(
						Items.NETHERITE_SHOVEL,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot12Stack);
		containerItems.set(12, slot12Stack);

		ItemStack slot13Stack =
				createItemStack(
						Items.NETHERITE_HOE,
						registries,
						enchantment(Enchantments.SILK_TOUCH, 1),
						enchantment(Enchantments.EFFICIENCY, 255),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot13Stack);
		containerItems.set(13, slot13Stack);

		ItemStack slot14Stack =
				createItemStack(
						Items.CROSSBOW,
						registries,
						enchantment(Enchantments.MULTISHOT, 1),
						enchantment(Enchantments.PIERCING, 10),
						enchantment(Enchantments.QUICK_CHARGE, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.INFINITY, 1),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot14Stack);
		containerItems.set(14, slot14Stack);

		ItemStack slot15Stack =
				createItemStack(
						Items.MACE,
						registries,
						enchantment(Enchantments.DENSITY, 255),
						enchantment(Enchantments.BREACH, 255),
						enchantment(Enchantments.SMITE, 255),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 255),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot15Stack);
		setAttributeModifiers(
				slot15Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(15, slot15Stack);

		ItemStack slot16Stack =
				createItemStack(
						Items.MACE,
						registries,
						enchantment(Enchantments.WIND_BURST, 3),
						enchantment(Enchantments.DENSITY, 5),
						enchantment(Enchantments.BREACH, 4),
						enchantment(Enchantments.SMITE, 255),
						enchantment(Enchantments.BANE_OF_ARTHROPODS, 255),
						enchantment(Enchantments.FIRE_ASPECT, 2),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot16Stack);
		setAttributeModifiers(
				slot16Stack,
				attributeModifier(
						Attributes.ATTACK_DAMAGE,
						"attack_damage",
						2048.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND),
				attributeModifier(
						Attributes.ATTACK_SPEED,
						"attack_speed",
						1024.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.MAINHAND));
		containerItems.set(16, slot16Stack);

		ItemStack slot17Stack =
				createItemStack(
						Items.SHIELD,
						registries,
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot17Stack);

		HolderLookup.RegistryLookup<DamageType> damageTypes =
				registries.lookupOrThrow(Registries.DAMAGE_TYPE);

		HolderSet<DamageType> blockedDamageTypes =
				HolderSet.direct(damageTypes.listElements().toList());

		slot17Stack.set(
				DataComponents.BLOCKS_ATTACKS,
				new BlocksAttacks(
						0.0F,
						0.0F,
						List.of(
								new BlocksAttacks.DamageReduction(
										360.0F, Optional.of(blockedDamageTypes), 1000.0F, 1000.0F)),
						new BlocksAttacks.ItemDamageFunction(0.0F, 0.0F, 0.0F),
						Optional.empty(),
						Optional.empty(),
						Optional.empty()));

		containerItems.set(17, slot17Stack);

		ItemStack slot18Stack =
				createItemStack(
						Items.NETHERITE_HELMET,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.RESPIRATION, 255),
						enchantment(Enchantments.AQUA_AFFINITY, 1),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot18Stack);
		containerItems.set(18, slot18Stack);

		ItemStack slot19Stack =
				createItemStack(
						Items.NETHERITE_CHESTPLATE,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot19Stack);
		containerItems.set(19, slot19Stack);

		ItemStack slot20Stack =
				createItemStack(
						Items.NETHERITE_LEGGINGS,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.SWIFT_SNEAK, 3),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot20Stack);
		containerItems.set(20, slot20Stack);

		ItemStack slot21Stack =
				createItemStack(
						Items.NETHERITE_BOOTS,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.FEATHER_FALLING, 4),
						enchantment(Enchantments.SOUL_SPEED, 3),
						enchantment(Enchantments.DEPTH_STRIDER, 3),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot21Stack);
		setAttributeModifiers(
				slot21Stack,
				attributeModifier(
						Attributes.ARMOR,
						"armor",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.ARMOR_TOUGHNESS,
						"armor_toughness",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.KNOCKBACK_RESISTANCE,
						"knockback_resistance",
						0.1D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.FALL_DAMAGE_MULTIPLIER,
						"fall_damage_multiplier",
						-1.0D,
						AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
						EquipmentSlotGroup.FEET));
		containerItems.set(21, slot21Stack);

		ItemStack elytraStack = new ItemStack(Items.ELYTRA);
		containerItems.set(22, elytraStack);

		ItemStack glideplateChestplateStack =
				createItemStack(
						Items.NETHERITE_CHESTPLATE,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(glideplateChestplateStack);
		applyGlideplateData(glideplateChestplateStack);
		containerItems.set(23, glideplateChestplateStack);

		ItemStack slot22Stack =
				createItemStack(
						Items.NETHERITE_BOOTS,
						registries,
						enchantment(Enchantments.PROTECTION, 255),
						enchantment(Enchantments.PROJECTILE_PROTECTION, 255),
						enchantment(Enchantments.BLAST_PROTECTION, 255),
						enchantment(Enchantments.FIRE_PROTECTION, 255),
						enchantment(Enchantments.FEATHER_FALLING, 4),
						enchantment(Enchantments.SOUL_SPEED, 3),
						enchantment(Enchantments.FROST_WALKER, 2),
						enchantment(Enchantments.THORNS, 7),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot22Stack);
		setAttributeModifiers(
				slot22Stack,
				attributeModifier(
						Attributes.ARMOR,
						"armor",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.ARMOR_TOUGHNESS,
						"armor_toughness",
						3.0D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.KNOCKBACK_RESISTANCE,
						"knockback_resistance",
						0.1D,
						AttributeModifier.Operation.ADD_VALUE,
						EquipmentSlotGroup.FEET),
				attributeModifier(
						Attributes.FALL_DAMAGE_MULTIPLIER,
						"fall_damage_multiplier",
						-1.0D,
						AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
						EquipmentSlotGroup.FEET));
		containerItems.set(24, slot22Stack);

		ItemStack slot23Stack =
				createItemStack(
						Items.FISHING_ROD,
						registries,
						enchantment(Enchantments.LUCK_OF_THE_SEA, 3),
						enchantment(Enchantments.LURE, 5),
						enchantment(Enchantments.UNBREAKING, 255),
						enchantment(Enchantments.MENDING, 1));
		setUnbreakable(slot23Stack);
		containerItems.set(25, slot23Stack);

		ItemStack box = new ItemStack(Items.RED_SHULKER_BOX);
		box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(containerItems));
		return box;
	}

	private static void applyGlideplateData(ItemStack stack) {
		CompoundTag customData = new CompoundTag();
		customData.putByte("glideplate_has_elytra", (byte) 1);
		customData.putByte("gliding", (byte) 1);
		customData.putString("gliding_chestplate_has_elytra", "netherite");
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
		stack.set(
				DataComponents.CUSTOM_MODEL_DATA,
				new CustomModelData(
						List.of(121211.0F),
						List.of(),
						List.of("glideplate:with_elytra", "glideplate:netherite"),
						List.of()));
		stack.set(DataComponents.GLIDER, Unit.INSTANCE);
		stack.set(
				DataComponents.ITEM_NAME,
				Component.translatableWithFallback(
						"item.glideplate.netherite_chestplate_with_elytra",
						"Netherite Chestplate with Elytra"));
		stack.set(
				DataComponents.LORE,
				new ItemLore(
						List.of(
								Component.translatableWithFallback("tooltip.glideplate.with_elytra", "With Elytra")
										.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)))));
	}

	private static List<ItemStack> createContainerItems(int size) {
		List<ItemStack> stacks = new ArrayList<>();

		for (int index = 0; index < size; index++) {
			stacks.add(ItemStack.EMPTY);
		}

		return stacks;
	}

	private static ItemStack createItemStack(
			Item item, HolderLookup.Provider registries, EnchantmentLevel... enchantments) {
		ItemStack stack = new ItemStack(item);
		setEnchantments(stack, registries, enchantments);

		return stack;
	}

	private static void setEnchantments(
			ItemStack stack, HolderLookup.Provider registries, EnchantmentLevel... enchantments) {
		if (enchantments.length == 0) {
			return;
		}

		HolderGetter<Enchantment> enchantmentLookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
		ItemEnchantments.Mutable mutableEnchantments =
				new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

		for (EnchantmentLevel enchantment : enchantments) {
			mutableEnchantments.set(enchantmentLookup.getOrThrow(enchantment.key()), enchantment.level());
		}

		stack.set(DataComponents.ENCHANTMENTS, mutableEnchantments.toImmutable());
	}

	private static EnchantmentLevel enchantment(ResourceKey<Enchantment> key, int level) {
		return new EnchantmentLevel(key, level);
	}

	private static void setUnbreakable(ItemStack stack) {
		stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
	}

	private static void setAttributeModifiers(ItemStack stack, AttributeModifierSpec... modifiers) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

		for (AttributeModifierSpec modifier : modifiers) {
			builder.add(
					modifier.attribute(),
					new AttributeModifier(
							minecraft(modifier.idPath()), modifier.amount(), modifier.operation()),
					modifier.slot());
		}

		stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
	}

	private static AttributeModifierSpec attributeModifier(
			Holder<Attribute> attribute,
			String idPath,
			double amount,
			AttributeModifier.Operation operation,
			EquipmentSlotGroup slot) {
		return new AttributeModifierSpec(attribute, idPath, amount, operation, slot);
	}

	private static Identifier minecraft(String path) {
		return Identifier.fromNamespaceAndPath("minecraft", path);
	}

	private record EnchantmentLevel(ResourceKey<Enchantment> key, int level) {}

	private record AttributeModifierSpec(
			Holder<Attribute> attribute,
			String idPath,
			double amount,
			AttributeModifier.Operation operation,
			EquipmentSlotGroup slot) {}
}

package com.example.wenhanclient.safedrop;

import com.example.wenhanclient.WenhanClientMod;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.Set;

public final class SafeDropSubMod {
	private static final long CONFIRMATION_NANOS = 500_000_000L;
	private static final Set<Item> PROTECTED_ITEMS =
			Set.of(
					Items.IRON_INGOT,
					Items.GOLD_INGOT,
					Items.COPPER_INGOT,
					Items.NETHERITE_INGOT,
					Items.IRON_NUGGET,
					Items.GOLD_NUGGET,
					Items.COPPER_NUGGET,
					Items.COAL_ORE,
					Items.DEEPSLATE_COAL_ORE,
					Items.IRON_ORE,
					Items.DEEPSLATE_IRON_ORE,
					Items.COPPER_ORE,
					Items.DEEPSLATE_COPPER_ORE,
					Items.GOLD_ORE,
					Items.DEEPSLATE_GOLD_ORE,
					Items.NETHER_GOLD_ORE,
					Items.REDSTONE_ORE,
					Items.DEEPSLATE_REDSTONE_ORE,
					Items.EMERALD_ORE,
					Items.DEEPSLATE_EMERALD_ORE,
					Items.LAPIS_ORE,
					Items.DEEPSLATE_LAPIS_ORE,
					Items.DIAMOND_ORE,
					Items.DEEPSLATE_DIAMOND_ORE,
					Items.NETHER_QUARTZ_ORE,
					Items.RAW_IRON,
					Items.RAW_GOLD,
					Items.RAW_COPPER,
					Items.RAW_IRON_BLOCK,
					Items.RAW_GOLD_BLOCK,
					Items.RAW_COPPER_BLOCK,
					Items.FLINT_AND_STEEL,
					Items.SHEARS,
					Items.BRUSH,
					Items.FISHING_ROD,
					Items.CARROT_ON_A_STICK,
					Items.WARPED_FUNGUS_ON_A_STICK,
					Items.SHIELD,
					Items.SPYGLASS,
					Items.COMPASS,
					Items.RECOVERY_COMPASS,
					Items.CLOCK,
					Items.ANCIENT_DEBRIS,
					Items.NETHERITE_SCRAP,
					Items.TOTEM_OF_UNDYING,
					Items.GOLDEN_APPLE,
					Items.ENCHANTED_GOLDEN_APPLE,
					Items.NETHER_STAR,
					Items.DRAGON_EGG,
					Items.OMINOUS_TRIAL_KEY,
					Items.HEAVY_CORE,
					Items.ENCHANTED_BOOK,
					Items.DRAGON_HEAD,
					Items.WITHER_SKELETON_SKULL,
					Items.SHULKER_SHELL,
					Items.HEART_OF_THE_SEA,
					Items.CONDUIT,
					Items.BEACON,
					Items.TRIAL_KEY,
					Items.BREEZE_ROD,
					Items.BLAZE_ROD,
					Items.BLAZE_POWDER,
					Items.ENDER_EYE,
					Items.ENDER_CHEST);
	private static SafeDropConfig config = new SafeDropConfig();
	private static ItemStack pendingStack = ItemStack.EMPTY;
	private static LocalPlayer pendingPlayer;
	private static ClientLevel pendingLevel;
	private static int pendingSlot = -1;
	private static boolean pendingDropAll;
	private static boolean pendingKeyReleased;
	private static long pendingSince;

	private SafeDropSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.safeDrop.copy();
		clearConfirmation();
	}

	public static SafeDropConfig config() {
		return config.copy();
	}

	public static void setConfig(SafeDropConfig newConfig) {
		config = newConfig == null ? new SafeDropConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.safeDrop = config.copy();
		WenhanClientMod.CONFIG.save();
		clearConfirmation();
	}

	public static boolean isEnabled() {
		return config.enabled;
	}

	public static boolean shouldBlockDrop(Minecraft client, LocalPlayer player, boolean dropAll) {
		validateConfirmation(client);
		if (!config.enabled
				|| client.gui.screen() != null
				|| client.level == null
				|| player != client.player
				|| player == null) {
			clearConfirmation();
			return false;
		}
		ItemStack stack = player.getInventory().getSelectedItem();
		if (!isProtected(stack)) {
			clearConfirmation();
			return false;
		}
		if (!pendingStack.isEmpty() && !pendingKeyReleased) {
			return true;
		}
		if (!pendingStack.isEmpty() && pendingDropAll == dropAll) {
			clearConfirmation();
			return false;
		}
		pendingStack = stack.copy();
		pendingPlayer = player;
		pendingLevel = client.level;
		pendingSlot = player.getInventory().getSelectedSlot();
		pendingDropAll = dropAll;
		pendingKeyReleased = !client.options.keyDrop.isDown();
		pendingSince = System.nanoTime();
		client.gui.hud.setOverlayMessage(
				Component.translatable("message.wenhan-client.safe-drop.confirm"), false);
		return true;
	}

	public static void onDropKeyReleased() {
		pendingKeyReleased = true;
	}

	public static void validateConfirmation(Minecraft client) {
		if (pendingStack.isEmpty()) {
			return;
		}
		if (!config.enabled
				|| client.gui.screen() != null
				|| client.player == null
				|| client.player != pendingPlayer
				|| client.level != pendingLevel
				|| !client.player.isAlive()
				|| client.player.getInventory().getSelectedSlot() != pendingSlot
				|| !ItemStack.matches(pendingStack, client.player.getInventory().getSelectedItem())
				|| System.nanoTime() - pendingSince >= CONFIRMATION_NANOS) {
			clearConfirmation();
		}
	}

	private static void clearConfirmation() {
		pendingStack = ItemStack.EMPTY;
		pendingPlayer = null;
		pendingLevel = null;
		pendingSlot = -1;
		pendingKeyReleased = false;
		pendingSince = 0;
	}

	public static boolean isProtected(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return stack.getRarity() != Rarity.COMMON
				|| stack.has(DataComponents.CUSTOM_DATA)
				|| stack.has(DataComponents.CUSTOM_NAME)
				|| stack.isEnchanted()
				|| !stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
						.isEmpty()
				|| stack.has(DataComponents.WEAPON)
				|| stack.has(DataComponents.PIERCING_WEAPON)
				|| stack.has(DataComponents.KINETIC_WEAPON)
				|| stack.has(DataComponents.TOOL)
				|| stack.has(DataComponents.EQUIPPABLE)
				|| stack.has(DataComponents.BUNDLE_CONTENTS)
				|| stack.has(DataComponents.JUKEBOX_PLAYABLE)
				|| stack.getItem() instanceof BundleItem
				|| stack.getItem() instanceof ProjectileWeaponItem
				|| stack.getItem() instanceof SmithingTemplateItem
				|| stack.getItem() instanceof BlockItem blockItem
						&& blockItem.getBlock() instanceof ShulkerBoxBlock
				|| stack.is(ConventionalItemTags.TOOLS)
				|| stack.is(ConventionalItemTags.ARMORS)
				|| stack.is(ConventionalItemTags.INGOTS)
				|| stack.is(ConventionalItemTags.NUGGETS)
				|| stack.is(ConventionalItemTags.ORES)
				|| stack.is(ConventionalItemTags.RAW_MATERIALS)
				|| stack.is(ConventionalItemTags.STORAGE_BLOCKS_RAW_COPPER)
				|| stack.is(ConventionalItemTags.STORAGE_BLOCKS_RAW_GOLD)
				|| stack.is(ConventionalItemTags.STORAGE_BLOCKS_RAW_IRON)
				|| stack.is(ConventionalItemTags.SHULKER_BOXES)
				|| PROTECTED_ITEMS.contains(stack.getItem());
	}
}

package com.example.wenhanclient.transparentblocks;

import com.example.wenhanclient.WenhanClientMod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class TransparentBlocksSubMod {
	private static TransparentBlocksConfig config = new TransparentBlocksConfig();
	private static int lastRenderMask = -1;

	private TransparentBlocksSubMod() {}

	public static void init() {
		config = WenhanClientMod.CONFIG.transparentBlocks.copy();
		ClientTickEvents.END_CLIENT_TICK.register(TransparentBlocksSubMod::onEndClientTick);
	}

	public static TransparentBlocksConfig config() {
		return config.copy();
	}

	public static void setConfig(TransparentBlocksConfig newConfig) {
		config = newConfig == null ? new TransparentBlocksConfig() : newConfig.copy();
		WenhanClientMod.CONFIG.transparentBlocks = config.copy();
		WenhanClientMod.CONFIG.save();
		refreshChunkGeometry(Minecraft.getInstance());
	}

	public static boolean shouldRender(Block block) {
		if (config.alwaysRender) {
			return isSupportedBlock(block);
		}
		if (block == Blocks.BARRIER) {
			return isHolding(Items.BARRIER);
		}
		if (block == Blocks.STRUCTURE_VOID) {
			return isHolding(Items.STRUCTURE_VOID);
		}
		if (block == Blocks.LIGHT) {
			return isHolding(Items.LIGHT);
		}
		return false;
	}

	private static void onEndClientTick(Minecraft client) {
		int renderMask = renderMask();
		if (renderMask == lastRenderMask) {
			return;
		}
		lastRenderMask = renderMask;
		refreshChunkGeometry(client);
	}

	private static int renderMask() {
		if (config.alwaysRender) {
			return 0b111;
		}
		int mask = 0;
		if (isHolding(Items.BARRIER)) {
			mask |= 0b001;
		}
		if (isHolding(Items.STRUCTURE_VOID)) {
			mask |= 0b010;
		}
		if (isHolding(Items.LIGHT)) {
			mask |= 0b100;
		}
		return mask;
	}

	private static boolean isSupportedBlock(Block block) {
		return block == Blocks.BARRIER || block == Blocks.STRUCTURE_VOID || block == Blocks.LIGHT;
	}

	private static boolean isHolding(Item item) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return false;
		}
		return isItem(client.player.getMainHandItem(), item)
				|| isItem(client.player.getOffhandItem(), item);
	}

	private static boolean isItem(ItemStack stack, Item item) {
		return stack != null && stack.is(item);
	}

	private static void refreshChunkGeometry(Minecraft client) {
		if (client.level == null || client.levelRenderer == null || client.gameRenderer == null) {
			return;
		}
		SectionPos cameraSection = SectionPos.of(client.gameRenderer.mainCamera().position());
		int renderDistance = client.options.getEffectiveRenderDistance();
		client.level.setSectionRangeDirty(
				cameraSection.x() - renderDistance,
				client.level.getMinSectionY(),
				cameraSection.z() - renderDistance,
				cameraSection.x() + renderDistance,
				client.level.getMaxSectionY() - 1,
				cameraSection.z() + renderDistance);
	}
}

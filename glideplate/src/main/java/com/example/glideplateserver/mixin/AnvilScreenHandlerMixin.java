package com.example.glideplateserver.mixin;

import com.example.glideplateserver.GlideplateServerUtil;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {
	@Shadow @Final private DataSlot cost;
	@Shadow private int repairItemCountCost;

	private AnvilScreenHandlerMixin(
			MenuType<?> type,
			int syncId,
			Inventory playerInventory,
			ContainerLevelAccess context,
			ItemCombinerMenuSlotDefinition slotDefinition) {
		super(type, syncId, playerInventory, context, slotDefinition);
	}

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void glideplateServer$createResult(CallbackInfo callbackInfo) {
		if (!this.glideplateServer$isLogicalServer()) {
			return;
		}

		ItemStack left = this.inputSlots.getItem(0);
		ItemStack right = this.inputSlots.getItem(1);

		if (GlideplateServerUtil.isBlockedDoubleCombine(left, right)) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
			this.cost.set(0);
			this.broadcastChanges();
			callbackInfo.cancel();
			return;
		}

		if (!GlideplateServerUtil.canCombine(left, right)) {
			return;
		}

		this.resultSlots.setItem(0, GlideplateServerUtil.createGlideplateServer(left));
		this.repairItemCountCost = 1;
		this.cost.set(0);
		this.broadcastChanges();
		callbackInfo.cancel();
	}

	@Inject(method = "setItemName", at = @At("HEAD"), cancellable = true)
	private void glideplateServer$setItemName(
			String itemName, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		ItemStack left = this.inputSlots.getItem(0);
		ItemStack right = this.inputSlots.getItem(1);

		if (GlideplateServerUtil.canCombine(left, right)
				|| GlideplateServerUtil.isBlockedDoubleCombine(left, right)) {
			callbackInfoReturnable.setReturnValue(false);
		}
	}

	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void glideplateServer$mayPickup(
			Player player,
			boolean present,
			CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (!this.glideplateServer$isLogicalServer()) {
			return;
		}

		if (!present) {
			return;
		}

		if (GlideplateServerUtil.canCombine(
				this.inputSlots.getItem(0), this.inputSlots.getItem(1))) {
			callbackInfoReturnable.setReturnValue(true);
		}
	}

	@Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
	private void glideplateServer$onTake(
			Player player, ItemStack stack, CallbackInfo callbackInfo) {
		if (!this.glideplateServer$isLogicalServer()) {
			return;
		}

		ItemStack left = this.inputSlots.getItem(0);
		ItemStack right = this.inputSlots.getItem(1);

		if (!GlideplateServerUtil.canCombine(left, right)
				|| !GlideplateServerUtil.hasElytra(stack)) {
			return;
		}

		this.inputSlots.setItem(0, ItemStack.EMPTY);
		right.shrink(1);

		if (right.isEmpty()) {
			this.inputSlots.setItem(1, ItemStack.EMPTY);
		}

		this.repairItemCountCost = 0;
		this.cost.set(0);
		this.access.execute((level, pos) -> level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0));
		this.broadcastChanges();
		callbackInfo.cancel();
	}

	private boolean glideplateServer$isLogicalServer() {
		return this.access.evaluate((level, pos) -> !level.isClientSide(), false);
	}
}

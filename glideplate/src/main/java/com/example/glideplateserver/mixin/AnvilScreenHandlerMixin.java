package com.example.glideplateserver.mixin;

import com.example.glideplateserver.GlideplateServerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.Property;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    private AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void glideplateServer$updateResult(CallbackInfo callbackInfo) {
        if (!this.glideplateServer$isLogicalServer()) {
            return;
        }

        ItemStack left = this.input.getStack(0);
        ItemStack right = this.input.getStack(1);

        if (GlideplateServerUtil.isBlockedDoubleCombine(left, right)) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            this.sendContentUpdates();
            callbackInfo.cancel();
            return;
        }

        if (!GlideplateServerUtil.canCombine(left, right)) {
            return;
        }

        this.output.setStack(0, GlideplateServerUtil.createGlideplateServer(left));
        this.repairItemUsage = 1;
        this.levelCost.set(1);
        this.sendContentUpdates();
        callbackInfo.cancel();
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    private void glideplateServer$canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (!this.glideplateServer$isLogicalServer()) {
            return;
        }

        if (!present) {
            return;
        }

        if (GlideplateServerUtil.canCombine(this.input.getStack(0), this.input.getStack(1))) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    private void glideplateServer$onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo callbackInfo) {
        if (!this.glideplateServer$isLogicalServer()) {
            return;
        }

        ItemStack left = this.input.getStack(0);
        ItemStack right = this.input.getStack(1);

        if (!GlideplateServerUtil.canCombine(left, right) || !GlideplateServerUtil.hasElytra(stack)) {
            return;
        }

        this.input.setStack(0, ItemStack.EMPTY);
        right.decrement(1);
        if (right.isEmpty()) {
            this.input.setStack(1, ItemStack.EMPTY);
        }

        this.repairItemUsage = 0;
        this.levelCost.set(0);
        this.context.run((world, pos) -> world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0));
        this.sendContentUpdates();
        callbackInfo.cancel();
    }

    private boolean glideplateServer$isLogicalServer() {
        boolean[] server = {false};
        this.context.run((world, pos) -> server[0] = !world.isClient());
        return server[0];
    }
}



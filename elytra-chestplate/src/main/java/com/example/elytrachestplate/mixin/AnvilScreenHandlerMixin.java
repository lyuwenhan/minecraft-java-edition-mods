package com.example.elytrachestplate.mixin;

import com.example.elytrachestplate.ElytraChestplateAnvil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.Property;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow
    @Final
    private Property levelCost;

    @Shadow
    private int repairItemUsage;

    protected AnvilScreenHandlerMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, ForgingSlotsManager forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void elytraChestplate$updateResult(CallbackInfo ci) {
        ItemStack first = this.input.getStack(0);
        ItemStack second = this.input.getStack(1);
        ItemStack result = ElytraChestplateAnvil.createResult(first, second);

        if (result.isEmpty()) {
            return;
        }

        this.repairItemUsage = 1;
        this.levelCost.set(0);
        this.output.setStack(0, result);
        this.sendContentUpdates();
        ci.cancel();
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    private void elytraChestplate$canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (!present) {
            return;
        }

        if (ElytraChestplateAnvil.isCombination(this.input.getStack(0), this.input.getStack(1))) {
            cir.setReturnValue(true);
        }
    }
}

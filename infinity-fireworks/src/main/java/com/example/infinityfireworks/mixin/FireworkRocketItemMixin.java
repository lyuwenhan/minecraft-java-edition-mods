package com.example.infinityfireworks.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {
    @Redirect(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V")
    )
    private void infinity_fireworks$keepRocketAfterUseOnBlock(ItemStack stack, int amount) {
    }

    @Redirect(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V")
    )
    private void infinity_fireworks$keepRocketAfterGlidingUse(ItemStack stack, int amount, LivingEntity entity) {
    }
}

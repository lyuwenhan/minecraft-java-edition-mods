package com.example.maplocator.mixin;

import com.example.maplocator.MapLocator;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
	private void maplocator$appendExactMapInformation(
			Item.TooltipContext context,
			Player player,
			TooltipFlag flag,
			CallbackInfoReturnable<List<Component>> cir) {
		ItemStack self = (ItemStack) (Object) this;
		cir.setReturnValue(
				MapLocator.appendExactMapInformation(self, context, cir.getReturnValue()));
	}
}

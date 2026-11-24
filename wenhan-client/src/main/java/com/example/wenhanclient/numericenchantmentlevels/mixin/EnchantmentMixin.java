package com.example.wenhanclient.numericenchantmentlevels.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
	@Inject(method = "getFullname", at = @At("HEAD"), cancellable = true)
	private static void numericEnchantmentLevels$useNumericLevelText(
			Holder<Enchantment> enchantment, int level, CallbackInfoReturnable<Component> cir) {
		MutableComponent name = enchantment.value().description().copy();
		ChatFormatting color =
				enchantment.is(EnchantmentTags.CURSE) ? ChatFormatting.RED : ChatFormatting.GRAY;
		ComponentUtils.mergeStyles(name, Style.EMPTY.withColor(color));

		if (level != 1 || enchantment.value().getMaxLevel() != 1) {
			name.append(CommonComponents.SPACE).append(Component.literal(Integer.toString(level)));
		}

		cir.setReturnValue(name);
	}
}

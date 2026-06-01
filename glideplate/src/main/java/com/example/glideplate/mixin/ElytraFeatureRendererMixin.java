package com.example.glideplate.mixin;

import com.example.glideplate.GlideplateUtil;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WingsLayer.class)
public abstract class ElytraFeatureRendererMixin {
	@ModifyVariable(method = "submit", at = @At("STORE"), ordinal = 0, require = 0)
	private ItemStack glideplate$useElytraEquipmentForMarkedChestplate(ItemStack stack) {
		if (GlideplateUtil.isChestplate(stack) && GlideplateUtil.hasElytra(stack)) {
			return Items.ELYTRA.getDefaultInstance();
		}

		return stack;
	}
}

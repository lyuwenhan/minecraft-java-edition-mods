package com.example.glideplate.mixin;

import com.example.glideplate.GlideplateUtil;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin {
    @ModifyVariable(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V", at = @At("STORE"), ordinal = 0)
    private ItemStack glideplate$useElytraEquipmentForMarkedChestplate(ItemStack stack) {
        if (GlideplateUtil.isChestplate(stack) && GlideplateUtil.hasElytra(stack)) {
            return Items.ELYTRA.getDefaultStack();
        }
        return stack;
    }
}

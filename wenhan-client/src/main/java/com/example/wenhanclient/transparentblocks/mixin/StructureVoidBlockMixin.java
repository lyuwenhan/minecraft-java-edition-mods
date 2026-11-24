package com.example.wenhanclient.transparentblocks.mixin;

import com.example.wenhanclient.transparentblocks.TransparentBlocksSubMod;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StructureVoidBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureVoidBlock.class)
public abstract class StructureVoidBlockMixin {
	@Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
	private void transparentBlocks$renderStructureVoidModel(
			BlockState state, CallbackInfoReturnable<RenderShape> cir) {
		if (TransparentBlocksSubMod.shouldRender(Blocks.STRUCTURE_VOID)) {
			cir.setReturnValue(RenderShape.MODEL);
		}
	}
}

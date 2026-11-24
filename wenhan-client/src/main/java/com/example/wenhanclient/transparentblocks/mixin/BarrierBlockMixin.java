package com.example.wenhanclient.transparentblocks.mixin;

import com.example.wenhanclient.transparentblocks.TransparentBlocksSubMod;

import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrierBlock.class)
public abstract class BarrierBlockMixin {
	@Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
	private void transparentBlocks$renderBarrierModel(
			BlockState state, CallbackInfoReturnable<RenderShape> cir) {
		if (TransparentBlocksSubMod.shouldRender(Blocks.BARRIER)) {
			cir.setReturnValue(RenderShape.MODEL);
		}
	}
}

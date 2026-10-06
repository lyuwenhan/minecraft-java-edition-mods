package com.example.wenhanclient.transparentblocks.mixin;

import com.example.wenhanclient.transparentblocks.TransparentBlocksSubMod;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightBlock.class)
public abstract class LightBlockMixin {
	@Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
	private void transparentBlocks$renderLightModel(
			BlockState state, CallbackInfoReturnable<RenderShape> cir) {
		if (TransparentBlocksSubMod.shouldRender(Blocks.LIGHT)) {
			cir.setReturnValue(RenderShape.MODEL);
		}
	}
}

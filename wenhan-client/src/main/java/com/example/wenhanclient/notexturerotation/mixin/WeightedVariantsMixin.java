package com.example.wenhanclient.notexturerotation.mixin;

import com.example.wenhanclient.notexturerotation.NoTextureRotationSubMod;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.WeightedVariants;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WeightedVariants.class)
public abstract class WeightedVariantsMixin {
	@Shadow @Final private WeightedList<BlockStateModel> list;

	@Inject(method = "collectParts", at = @At("HEAD"), cancellable = true)
	private void noTextureRotation$useFirstVariant(
			RandomSource random, List<BlockStateModelPart> parts, CallbackInfo ci) {
		if (!NoTextureRotationSubMod.isEnabled()) {
			return;
		}

		Weighted<BlockStateModel> first = this.list.unwrap().getFirst();
		first.value().collectParts(random, parts);
		ci.cancel();
	}
}

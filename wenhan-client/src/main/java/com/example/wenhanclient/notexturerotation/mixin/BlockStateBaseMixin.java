package com.example.wenhanclient.notexturerotation.mixin;

import com.example.wenhanclient.notexturerotation.NoTextureRotationSubMod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Inject(method = "getOffset", at = @At("HEAD"), cancellable = true)
	private void noTextureRotation$disableRandomOffset(
			BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
		if (!NoTextureRotationSubMod.shouldDisableRandomOffset()) {
			return;
		}

		BlockState state = (BlockState) (Object) this;
		if (NoTextureRotationSubMod.shouldKeepCollisionShapeOffsets()
				&& ((BlockBehaviourAccessor) state.getBlock()).noTextureRotation$hasCollision()) {
			return;
		}

		cir.setReturnValue(Vec3.ZERO);
	}
}

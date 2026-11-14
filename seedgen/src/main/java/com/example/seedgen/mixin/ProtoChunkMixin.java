package com.example.seedgen.mixin;

import com.example.seedgen.core.SeedgenWorldgenRules;
import com.example.seedgen.core.WorldgenSeedContext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProtoChunk.class)
abstract class ProtoChunkMixin {
	@Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
	private void seedgen$rejectLootAndSpawnerBlocks(
			BlockPos pos,
			BlockState state,
			int flags,
			CallbackInfoReturnable<BlockState> callback) {
		ProtoChunk chunk = (ProtoChunk) (Object) this;
		if ((WorldgenSeedContext.isVirtualWorldActive()
						|| SeedgenWorldgenRules.isVirtualChunk(chunk))
				&& SeedgenWorldgenRules.shouldRejectGeneratedBlock(state)) {
			callback.setReturnValue(chunk.getBlockState(pos));
		}
	}
}

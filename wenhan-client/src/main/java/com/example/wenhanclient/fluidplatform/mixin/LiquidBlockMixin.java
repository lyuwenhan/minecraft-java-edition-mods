package com.example.wenhanclient.fluidplatform.mixin;

import com.example.wenhanclient.fluidplatform.FluidPlatformSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {
	@Redirect(
			method = "getCollisionShape",
			at = @At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I", ordinal = 0))
	private int fluidplatform$allowFlowingFluidCollision(
			Integer level,
			BlockState state,
			BlockGetter blockGetter,
			BlockPos pos,
			CollisionContext context) {
		if (context instanceof EntityCollisionContext entityContext
				&& entityContext.getEntity() instanceof LocalPlayer player
				&& FluidPlatformSubMod.isEnabled()
				&& !player.isShiftKeyDown()
				&& (state.getFluidState().is(FluidTags.WATER)
						|| state.getFluidState().is(FluidTags.LAVA))) {
			return 0;
		}
		return level.intValue();
	}
}

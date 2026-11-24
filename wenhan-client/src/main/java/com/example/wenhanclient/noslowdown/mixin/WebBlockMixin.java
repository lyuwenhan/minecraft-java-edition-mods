package com.example.wenhanclient.noslowdown.mixin;

import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WebBlock.class)
public abstract class WebBlockMixin {
	@Redirect(
			method = "entityInside",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/Entity;makeStuckInBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V"))
	private void noSlowdown$removeSneakingVerticalCobwebSlowdown(
			Entity entity, BlockState state, Vec3 speedMultiplier) {
		if (NoSlowdownSubMod.shouldCancelBlockSlowdown(entity) && entity.isShiftKeyDown()) {
			speedMultiplier = new Vec3(speedMultiplier.x, 1.0D, speedMultiplier.z);
		}
		entity.makeStuckInBlock(state, speedMultiplier);
	}
}

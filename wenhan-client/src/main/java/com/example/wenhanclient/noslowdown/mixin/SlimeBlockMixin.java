package com.example.wenhanclient.noslowdown.mixin;

import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin {
	@Redirect(
			method = "stepOn",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
	private void noSlowdown$skipSlimeStepSlowdown(Entity entity, Vec3 movement) {
		if (!NoSlowdownSubMod.shouldCancelBlockSlowdown(entity)) {
			entity.setDeltaMovement(movement);
		}
	}
}

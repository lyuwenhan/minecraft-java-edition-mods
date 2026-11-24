package com.example.wenhanclient.noslimebounce.mixin;

import com.example.wenhanclient.noslimebounce.NoSlimeBounceSubMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Redirect(
			method = "getBlockBounciness",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/block/Block;getBounceRestitution()F"))
	private float noSlimeBounce$removeSlimeBlockBounce(Block block) {
		if (NoSlimeBounceSubMod.shouldCancelBounce((Entity) (Object) this)
				&& block == Blocks.SLIME_BLOCK) {
			return 0.0F;
		}
		return block.getBounceRestitution();
	}
}

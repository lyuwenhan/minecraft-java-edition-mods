package com.example.wenhanclient.solidcobweb.mixin;

import com.example.wenhanclient.solidcobweb.SolidCobwebSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityCollisionContext.class)
public abstract class EntityCollisionContextMixin {
	@Shadow @Final private Entity entity;

	@Inject(method = "getCollisionShape", at = @At("RETURN"), cancellable = true)
	private void solidCobweb$makeCobwebSolidForLocalPlayer(
			BlockState state,
			CollisionGetter level,
			BlockPos pos,
			CallbackInfoReturnable<VoxelShape> cir) {
		if (SolidCobwebSubMod.isEnabled()
				&& this.entity instanceof LocalPlayer
				&& state.is(Blocks.COBWEB)) {
			cir.setReturnValue(Shapes.block());
		}
	}
}

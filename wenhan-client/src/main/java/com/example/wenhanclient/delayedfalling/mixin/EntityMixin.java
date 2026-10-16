package com.example.wenhanclient.delayedfalling.mixin;

import com.example.wenhanclient.delayedfalling.DelayedFallingSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Unique private boolean delayedFalling$wasOnGroundBeforeMove;
	@Unique private boolean delayedFalling$hadSupportBeforeMove;
	@Unique private double delayedFalling$yBeforeMove;

	@Inject(method = "move", at = @At("HEAD"))
	private void delayedFalling$captureMoveStart(
			MoverType moverType, Vec3 movement, CallbackInfo callbackInfo) {
		if ((Object) this instanceof LocalPlayer player) {
			delayedFalling$wasOnGroundBeforeMove = player.onGround();
			delayedFalling$hadSupportBeforeMove =
					delayedFalling$wasOnGroundBeforeMove
							&& DelayedFallingSubMod.hasSupportBelow(player);
			delayedFalling$yBeforeMove = player.getY();
		}
	}

	@Inject(method = "move", at = @At("TAIL"))
	private void delayedFalling$startOnLostSupport(
			MoverType moverType, Vec3 movement, CallbackInfo callbackInfo) {
		if ((Object) this instanceof LocalPlayer player
				&& delayedFalling$wasOnGroundBeforeMove
				&& delayedFalling$hadSupportBeforeMove
				&& !player.onGround()
				&& movement.y <= 0.0D) {
			DelayedFallingSubMod.startAfterWalkingOffEdge(player, delayedFalling$yBeforeMove);
		}
	}
}

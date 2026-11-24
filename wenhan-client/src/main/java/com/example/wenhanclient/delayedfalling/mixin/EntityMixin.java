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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Unique private boolean delayedFalling$hadSupportBeforeMove;
	@Unique private double delayedFalling$yBeforeMove;

	@ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
	private Vec3 delayedFalling$holdHeight(Vec3 movement) {
		if ((Object) this instanceof LocalPlayer player) {
			return DelayedFallingSubMod.restrictMovement(player, movement);
		}
		return movement;
	}

	@Inject(method = "move", at = @At("HEAD"))
	private void delayedFalling$captureMoveStart(
			MoverType moverType, Vec3 movement, CallbackInfo callbackInfo) {
		if ((Object) this instanceof LocalPlayer player) {
			delayedFalling$hadSupportBeforeMove =
					DelayedFallingSubMod.isEnabled()
							&& !DelayedFallingSubMod.isDelaying(player)
							&& movement.y <= 0.0D
							&& DelayedFallingSubMod.hasSupportBelow(player);
			delayedFalling$yBeforeMove = player.getY();
		}
	}

	@Inject(method = "move", at = @At("TAIL"))
	private void delayedFalling$startOnLostSupport(
			MoverType moverType, Vec3 movement, CallbackInfo callbackInfo) {
		if ((Object) this instanceof LocalPlayer player
				&& delayedFalling$hadSupportBeforeMove
				&& movement.y <= 0.0D
				&& !DelayedFallingSubMod.hasSupportBelow(player)) {
			DelayedFallingSubMod.startAfterWalkingOffEdge(player, delayedFalling$yBeforeMove);
		}
	}
}

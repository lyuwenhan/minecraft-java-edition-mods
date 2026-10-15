package com.example.wenhanclient.autoaim.mixin;

import com.example.wenhanclient.autoaim.AutoAimSubMod;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
	private void autoAim$highlightLockedTarget(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (AutoAimSubMod.isHighlightedTarget((Entity) (Object) this)) {
			callbackInfo.setReturnValue(true);
		}
	}

	@Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
	private void autoAim$useTargetHighlightColor(CallbackInfoReturnable<Integer> callbackInfo) {
		Integer color = AutoAimSubMod.getTargetHighlightColor((Entity) (Object) this);
		if (color != null) {
			callbackInfo.setReturnValue(color);
		}
	}
}

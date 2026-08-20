package com.example.entityhighlighter.mixin;

import com.example.entityhighlighter.EntityHighlighterMod;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityHighlightMixin {
	@Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
	private void entityhighlighter$forceGlow(CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;
		if (!EntityHighlighterMod.isGlowOverrideBypassed(self)
				&& !cir.getReturnValue()
				&& EntityHighlighterMod.getHighlightColor(self) != null) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
	private void entityhighlighter$overrideGlowColor(CallbackInfoReturnable<Integer> cir) {
		Entity self = (Entity) (Object) this;
		Integer color = EntityHighlighterMod.getHighlightColor(self);
		if (color != null && !EntityHighlighterMod.isGlowingWithoutEntityHighlighter(self)) {
			cir.setReturnValue(color);
		}
	}
}

package com.example.entityhighlighter.mixin;

import com.example.entityhighlighter.EntityHighlighterMod;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Display.class)
public abstract class DisplayHighlightColorMixin {
	@Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
	private void entityhighlighter$overrideDisplayGlowColor(CallbackInfoReturnable<Integer> cir) {
		Entity self = (Entity) (Object) this;
		Integer color = EntityHighlighterMod.getHighlightColor(self);
		if (color != null && !EntityHighlighterMod.isGlowingWithoutEntityHighlighter(self)) {
			cir.setReturnValue(color);
		}
	}
}

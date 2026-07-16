package com.example.playerhighlighter.mixin;

import com.example.playerhighlighter.PlayerHighlighterMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGlowingMixin {
	@Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
	private void playerhighlighter$glowOnlyWhenTabPressed(CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;

		if (self instanceof Player && PlayerHighlighterMod.isHighlightActive()) {
			cir.setReturnValue(true);
		}
	}
}

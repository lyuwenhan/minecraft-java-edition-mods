package com.example.wenhanclient.placementrestriction.mixin;

import com.example.wenhanclient.placementrestriction.PlacementRestrictionSubMod;
import com.example.wenhanclient.scaffoldplacement.ScaffoldPlacementSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 1100)
public abstract class MinecraftMixin {
	@Shadow public HitResult hitResult;
	@Shadow public Entity crosshairPickEntity;

	@Inject(method = "pick", at = @At("TAIL"))
	private void placementRestriction$focusAllowedPosition(
			float tickDelta, CallbackInfo callbackInfo) {
		BlockHitResult adjusted = PlacementRestrictionSubMod.adjustFocus((Minecraft) (Object) this);
		if (adjusted != null) {
			this.hitResult = adjusted;
			this.crosshairPickEntity = null;
			return;
		}

		BlockHitResult scaffoldTarget =
				ScaffoldPlacementSubMod.getCrosshairTarget((Minecraft) (Object) this);
		if (scaffoldTarget != null) {
			this.hitResult = scaffoldTarget;
			this.crosshairPickEntity = null;
		}
	}
}

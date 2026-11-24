package com.example.wenhanclient.placementrestriction.mixin;

import com.example.wenhanclient.placementrestriction.PlacementRestrictionSubMod;
import com.example.wenhanclient.scaffoldplacement.ScaffoldPlacementSubMod;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = Minecraft.class, priority = 1100)
public abstract class MinecraftMixin {
	@Shadow public HitResult hitResult;
	@Shadow public Entity crosshairPickEntity;

	@Unique private HitResult placementRestriction$normalHitResult;
	@Unique private Entity placementRestriction$normalPickEntity;
	@Unique private HitResult placementRestriction$placementHitResult;

	@WrapMethod(method = "pick")
	private void placementRestriction$focusAllowedPosition(
			float tickDelta, Operation<Void> original) {
		original.call(tickDelta);
		this.placementRestriction$normalHitResult = this.hitResult;
		this.placementRestriction$normalPickEntity = this.crosshairPickEntity;
		this.placementRestriction$placementHitResult = null;

		BlockHitResult adjusted = PlacementRestrictionSubMod.adjustFocus((Minecraft) (Object) this);
		if (adjusted == null) {
			adjusted = ScaffoldPlacementSubMod.getCrosshairTarget((Minecraft) (Object) this);
		}
		if (adjusted != null) {
			this.placementRestriction$placementHitResult = adjusted;
			this.hitResult = adjusted;
			this.crosshairPickEntity = null;
		}
	}

	@WrapMethod(method = "startAttack")
	private boolean placementRestriction$attackNormalTarget(Operation<Boolean> original) {
		HitResult savedHit = this.hitResult;
		Entity savedEntity = this.crosshairPickEntity;
		this.placementRestriction$restoreNormalTarget();
		try {
			return original.call();
		} finally {
			this.hitResult = savedHit;
			this.crosshairPickEntity = savedEntity;
		}
	}

	@WrapMethod(method = "continueAttack")
	private void placementRestriction$mineNormalTarget(
			boolean attackDown, Operation<Void> original) {
		HitResult savedHit = this.hitResult;
		Entity savedEntity = this.crosshairPickEntity;
		this.placementRestriction$restoreNormalTarget();
		try {
			original.call(attackDown);
		} finally {
			this.hitResult = savedHit;
			this.crosshairPickEntity = savedEntity;
		}
	}

	@ModifyExpressionValue(
			method = "startUseItem",
			at =
					@At(
							value = "FIELD",
							target =
									"Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;"),
			slice =
					@Slice(
							from =
									@At(
											value = "INVOKE",
											target =
													"Lnet/minecraft/world/item/ItemStack;getInteractAnimation()Lnet/minecraft/world/item/component/SwingAnimation;")))
	private HitResult placementRestriction$useTargetForHeldItem(
			HitResult target, @Local ItemStack stack) {
		if (target == this.placementRestriction$placementHitResult
				&& this.placementRestriction$placementHitResult != null
				&& !(stack.getItem() instanceof BlockItem)) {
			return this.placementRestriction$normalHitResult;
		}
		return target;
	}

	@Unique
	private void placementRestriction$restoreNormalTarget() {
		if (this.placementRestriction$placementHitResult != null
				&& this.hitResult == this.placementRestriction$placementHitResult) {
			this.hitResult = this.placementRestriction$normalHitResult;
			this.crosshairPickEntity = this.placementRestriction$normalPickEntity;
		}
	}
}

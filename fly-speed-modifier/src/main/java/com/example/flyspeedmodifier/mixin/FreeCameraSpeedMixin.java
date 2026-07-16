package com.example.flyspeedmodifier.mixin;

import com.example.flyspeedmodifier.FreecamSpeedController;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.xolt.freecam.util.FreeCamera", remap = false)
public abstract class FreeCameraSpeedMixin {
	@ModifyArg(
			method = "doMotion",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/xolt/freecam/util/Motion;doMotion(Lnet/xolt/freecam/util/FreeCamera;DD)V",
							remap = false),
			index = 1,
			require = 0,
			remap = false)
	private double flySpeedModifier$replaceDefaultHorizontalSpeed(double originalSpeed) {
		return FreecamSpeedController.applyFreecamSpeed(originalSpeed);
	}

	@ModifyArg(
			method = "doMotion",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/xolt/freecam/util/Motion;doMotion(Lnet/xolt/freecam/util/FreeCamera;DD)V",
							remap = false),
			index = 2,
			require = 0,
			remap = false)
	private double flySpeedModifier$replaceDefaultVerticalSpeed(double originalSpeed) {
		return FreecamSpeedController.applyFreecamSpeed(originalSpeed);
	}

	@ModifyArg(
			method = "doMotion",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/entity/player/Abilities;setFlyingSpeed(F)V",
							remap = true),
			index = 0,
			require = 0,
			remap = false)
	private float flySpeedModifier$replaceCreativeFlyingSpeed(float originalFlyingSpeed) {
		return FreecamSpeedController.applyFreecamCreativeFlyingSpeed(originalFlyingSpeed);
	}
}

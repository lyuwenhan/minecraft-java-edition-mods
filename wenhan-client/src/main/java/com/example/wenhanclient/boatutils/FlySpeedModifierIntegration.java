package com.example.wenhanclient.boatutils;

import com.example.wenhanclient.flyspeedmodifier.FreecamSpeedController;

public final class FlySpeedModifierIntegration {
	private FlySpeedModifierIntegration() {}

	public static double applyOtherMovementMultiplier(double movement) {
		if (!FreecamSpeedController.shouldModifyOtherMovement()) {
			return movement;
		}
		return movement * FreecamSpeedController.otherMovementMultiplier();
	}
}

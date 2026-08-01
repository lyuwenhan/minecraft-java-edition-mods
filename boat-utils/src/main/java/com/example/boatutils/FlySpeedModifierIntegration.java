package com.example.boatutils;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FlySpeedModifierIntegration {
	private static final String MOD_ID = "fly-speed-modifier";
	private static final String CONTROLLER_CLASS_NAME =
			"com.example.flyspeedmodifier.FreecamSpeedController";
	private static final Method SHOULD_MODIFY_OTHER_MOVEMENT_METHOD;
	private static final Method OTHER_MOVEMENT_MULTIPLIER_METHOD;

	static {
		Method shouldModifyOtherMovementMethod = null;
		Method otherMovementMultiplierMethod = null;
		if (FabricLoader.getInstance().isModLoaded(MOD_ID)) {
			try {
				Class<?> controllerClass = Class.forName(CONTROLLER_CLASS_NAME);
				shouldModifyOtherMovementMethod =
						controllerClass.getMethod("shouldModifyOtherMovement");
				otherMovementMultiplierMethod =
						controllerClass.getMethod("otherMovementMultiplier");
			} catch (ReflectiveOperationException | LinkageError ignored) {
				shouldModifyOtherMovementMethod = null;
				otherMovementMultiplierMethod = null;
			}
		}
		SHOULD_MODIFY_OTHER_MOVEMENT_METHOD = shouldModifyOtherMovementMethod;
		OTHER_MOVEMENT_MULTIPLIER_METHOD = otherMovementMultiplierMethod;
	}

	private FlySpeedModifierIntegration() {}

	public static double applyOtherMovementMultiplier(double movement) {
		if (SHOULD_MODIFY_OTHER_MOVEMENT_METHOD == null
				|| OTHER_MOVEMENT_MULTIPLIER_METHOD == null) {
			return movement;
		}

		try {
			Object shouldModify = SHOULD_MODIFY_OTHER_MOVEMENT_METHOD.invoke(null);
			if (!Boolean.TRUE.equals(shouldModify)) {
				return movement;
			}

			Object multiplier = OTHER_MOVEMENT_MULTIPLIER_METHOD.invoke(null);
			if (!(multiplier instanceof Double multiplierValue)) {
				return movement;
			}
			return movement * multiplierValue;
		} catch (IllegalAccessException | InvocationTargetException | LinkageError ignored) {
			return movement;
		}
	}
}

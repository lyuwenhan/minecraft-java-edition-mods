package com.example.dabrserverconfigoverride.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Keeps DABR's client-side thrust options editable even when the connected server reports
 * allowThrusting=false.
 *
 * <p>This only changes YACL option availability. It does not alter the received server config
 * object or the server-settings UI values.
 */
@Mixin(targets = "nl.enjarai.doabarrelroll.compat.yacl.YACLImplementation", remap = false)
public abstract class YACLImplementationMixin {
	@ModifyArg(
			method = "generateConfigScreen",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnl/enjarai/doabarrelroll/compat/yacl/YACLImplementation$Dependable;<init>(Z)V",
							ordinal = 0),
			index = 0)
	private static boolean dabrServerConfigOverride$enableThrustOptionsInitially(
			boolean available) {
		return true;
	}

	@ModifyArg(
			method = "lambda$generateConfigScreen$86",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnl/enjarai/doabarrelroll/compat/yacl/YACLImplementation$Dependable;set(Z)V",
							ordinal = 0),
			index = 0)
	private static boolean dabrServerConfigOverride$keepThrustOptionsEnabled(boolean available) {
		return true;
	}
}

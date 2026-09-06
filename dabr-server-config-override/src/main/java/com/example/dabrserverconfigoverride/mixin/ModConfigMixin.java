package com.example.dabrserverconfigoverride.mixin;

import nl.enjarai.doabarrelroll.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModConfig.class)
public abstract class ModConfigMixin {
	/**
	 * DABR's original getEnableThrust() combines the local client toggle with the server's
	 * allowThrusting value. For gameplay, keep only the client's own toggle. The received server
	 * configuration object itself is untouched.
	 */
	@Inject(method = "getEnableThrust", at = @At("HEAD"), cancellable = true)
	private void dabrServerConfigOverride$allowThrustingForGameplay(
			CallbackInfoReturnable<Boolean> cir) {
		ModConfig self = (ModConfig) (Object) this;
		cir.setReturnValue(self.getEnableThrustClient());
	}

	@Redirect(
			method = "notifyPlayerOfServerConfig",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnl/enjarai/doabarrelroll/util/ToastUtil;toasty(Ljava/lang/String;)V",
							ordinal = 0))
	private void dabrServerConfigOverride$suppressThrustingDisabledToast(String messageKey) {
		// Suppress only the thrusting-disabled server toast.
		// The received server config remains unchanged.
	}
}

package com.example.wenhanclient.dabrserverconfigoverride.mixin;

import nl.enjarai.doabarrelroll.config.ModConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModConfig.class)
public abstract class ModConfigMixin {
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
									"Lnl/enjarai/doabarrelroll/util/ToastUtil;toasty(Ljava/lang/String;)V"))
	private void dabrServerConfigOverride$suppressIgnoredServerOverrideToasts(String messageKey) {}
}

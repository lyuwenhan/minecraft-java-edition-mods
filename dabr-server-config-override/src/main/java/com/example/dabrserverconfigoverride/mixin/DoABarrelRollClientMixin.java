package com.example.dabrserverconfigoverride.mixin;

import nl.enjarai.doabarrelroll.DoABarrelRollClient;
import nl.enjarai.doabarrelroll.config.LimitedModConfigServer;
import nl.enjarai.doabarrelroll.net.HandshakeClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(DoABarrelRollClient.class)
public abstract class DoABarrelRollClientMixin {
	@Redirect(
			method = "isFallFlying",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnl/enjarai/doabarrelroll/net/HandshakeClient;getConfig()Ljava/util/Optional;"))
	private static Optional<LimitedModConfigServer> dabrServerConfigOverride$ignoreForceEnabled(
			HandshakeClient<?> handshakeClient) {
		return Optional.empty();
	}
}

package com.example.dabrserverconfigoverride.mixin;

import nl.enjarai.doabarrelroll.ModKeybindings;
import nl.enjarai.doabarrelroll.config.LimitedModConfigServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

/**
 * Makes both client toggle keys ignore the corresponding server-enforced boolean.
 *
 * <p>DABR passes method references to two {@code Optional.map(...)} calls in {@code clientTick}:
 * the first maps {@code LimitedModConfigServer::forceEnabled}, and the second maps {@code
 * LimitedModConfigServer::allowThrusting}. Method references compile to {@code invokedynamic}, so
 * replace the mapper arguments at the two exact call ordinals.
 */
@Mixin(ModKeybindings.class)
public abstract class ModKeybindingsMixin {
	@ModifyArg(
			method = "clientTick",
			at =
					@At(
							value = "INVOKE",
							target =
									"Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;",
							ordinal = 0),
			index = 0)
	private static Function<LimitedModConfigServer, Boolean>
			dabrServerConfigOverride$ignoreForceEnabledForToggle(
					Function<LimitedModConfigServer, Boolean> originalMapper) {
		return serverConfig -> false;
	}

	@ModifyArg(
			method = "clientTick",
			at =
					@At(
							value = "INVOKE",
							target =
									"Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;",
							ordinal = 1),
			index = 0)
	private static Function<LimitedModConfigServer, Boolean>
			dabrServerConfigOverride$ignoreAllowThrustingForToggle(
					Function<LimitedModConfigServer, Boolean> originalMapper) {
		return serverConfig -> true;
	}
}

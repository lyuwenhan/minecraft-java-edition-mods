package com.example.wenhanclient.flightdisabler.mixin;

import com.example.wenhanclient.flightdisabler.FlightDisablerSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public class LocalPlayerFlyingMixin {
	@Redirect(
			method = "aiStep",
			at =
					@At(
							value = "FIELD",
							target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
							opcode = Opcodes.PUTFIELD,
							ordinal = 0),
			require = 0)
	private void flightdisabler$preventSpectatorStartFlying(Abilities abilities, boolean flying) {
		flightdisabler$setFlyingUnlessDisabled(abilities, flying);
	}

	@Redirect(
			method = "aiStep",
			at =
					@At(
							value = "FIELD",
							target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
							opcode = Opcodes.PUTFIELD,
							ordinal = 1),
			require = 0)
	private void flightdisabler$preventToggleStartFlying(Abilities abilities, boolean flying) {
		flightdisabler$setFlyingUnlessDisabled(abilities, flying);
	}

	private static void flightdisabler$setFlyingUnlessDisabled(
			Abilities abilities, boolean flying) {
		if (FlightDisablerSubMod.isEnabled() && flying) {
			abilities.flying = false;
			return;
		}

		abilities.flying = flying;
	}
}

package com.example.flightdisabler.mixin;

import com.example.flightdisabler.FlightDisablerMod;

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
							opcode = Opcodes.PUTFIELD),
			require = 0)
	private void flightdisabler$preventStartFlying(Abilities abilities, boolean flying) {
		if (FlightDisablerMod.isEnabled() && flying) {
			abilities.flying = false;
			return;
		}

		abilities.flying = flying;
	}
}

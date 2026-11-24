package com.example.wenhanclient.noflightreset.mixin;

import com.example.wenhanclient.noflightreset.NoFlightResetSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Redirect(
			method = "aiStep",
			at =
					@At(
							value = "FIELD",
							target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
							opcode = Opcodes.PUTFIELD,
							ordinal = 2),
			require = 0)
	private void noFlightReset$keepFlyingOnGround(Abilities abilities, boolean flying) {
		if (NoFlightResetSubMod.isEnabled()) {
			return;
		}
		abilities.flying = flying;
	}
}

package com.example.wenhanclient.freecaminteraction.mixin;

import com.example.wenhanclient.freecaminteraction.FreecamInteractionSubMod;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Minecraft.class, priority = 1200)
public abstract class MinecraftMixin {
	@WrapMethod(method = "startAttack")
	private boolean freecamInteraction$attack(Operation<Boolean> original) {
		boolean entered = FreecamInteractionSubMod.enterInteraction();
		try {
			return original.call();
		} finally {
			FreecamInteractionSubMod.leaveInteraction(entered);
		}
	}

	@WrapMethod(method = "continueAttack")
	private void freecamInteraction$continueAttack(boolean held, Operation<Void> original) {
		boolean entered = held && FreecamInteractionSubMod.enterInteraction();
		try {
			original.call(held);
		} finally {
			FreecamInteractionSubMod.leaveInteraction(entered);
		}
	}

	@WrapMethod(method = {"startUseItem", "pickBlockOrEntity"})
	private void freecamInteraction$use(Operation<Void> original) {
		boolean entered = FreecamInteractionSubMod.enterInteraction();
		try {
			original.call();
		} finally {
			FreecamInteractionSubMod.leaveInteraction(entered);
		}
	}
}

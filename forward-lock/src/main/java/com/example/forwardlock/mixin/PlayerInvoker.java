package com.example.forwardlock.mixin;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerInvoker {
	@Invoker("hasEnoughFoodToDoExhaustiveManoeuvres")
	boolean forwardLock$hasEnoughFoodToDoExhaustiveManoeuvres();
}

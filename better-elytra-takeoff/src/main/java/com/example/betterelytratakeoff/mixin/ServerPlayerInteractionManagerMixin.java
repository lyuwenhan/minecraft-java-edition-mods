package com.example.betterelytratakeoff.mixin;

import com.example.betterelytratakeoff.BetterElytraTakeoffState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
	@Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
	private void betterElytraTakeoff$takeOff(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		betterElytraTakeoff$tryTakeOff(player, stack, hand, false, cir);
	}

	@Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
	private void betterElytraTakeoff$takeOffFromBlockUse(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		betterElytraTakeoff$tryTakeOff(player, stack, hand, true, cir);
	}

	private static void betterElytraTakeoff$tryTakeOff(ServerPlayerEntity player, ItemStack stack, Hand hand, boolean hasBlockTarget, CallbackInfoReturnable<ActionResult> cir) {
		if (!betterElytraTakeoff$shouldTakeOff(player, stack, hasBlockTarget)) {
			return;
		}

		BetterElytraTakeoffState.schedule(player, hand, stack);
		cir.setReturnValue(ActionResult.FAIL);
	}

	private static boolean betterElytraTakeoff$shouldTakeOff(ServerPlayerEntity player, ItemStack stack, boolean hasBlockTarget) {
		return stack.isOf(Items.FIREWORK_ROCKET)
				&& (player.getPitch() <= 0.0F || !hasBlockTarget)
				&& !player.isSneaking()
				&& !player.isGliding()
				&& BetterElytraTakeoffState.canUseTakeoff(player);
	}
}

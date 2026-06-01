package com.example.betterelytratakeoff;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class BetterElytraTakeoffMod implements ModInitializer {
	public BetterElytraTakeoffMod() {
	}

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				BetterElytraTakeoffState.tick(player);
			}
		});

		UseItemCallback.EVENT.register((player, world, hand) -> tryTakeOff(player, world, hand, false));

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> tryTakeOff(player, world, hand, true));
	}

	private static InteractionResult tryTakeOff(
		Player player,
		Level world,
		InteractionHand hand,
		boolean hasBlockTarget
	) {
		if (world.isClientSide()) {
			return InteractionResult.PASS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		ItemStack stack = serverPlayer.getItemInHand(hand);

		if (!shouldTakeOff(serverPlayer, stack, hasBlockTarget)) {
			return InteractionResult.PASS;
		}

		BetterElytraTakeoffState.schedule(serverPlayer, stack);
		return InteractionResult.FAIL;
	}

	private static boolean shouldTakeOff(
		ServerPlayer player,
		ItemStack stack,
		boolean hasBlockTarget
	) {
		return stack.is(Items.FIREWORK_ROCKET)
			&& (player.getXRot() <= 0.0F || !hasBlockTarget)
			&& !player.isShiftKeyDown()
			&& !player.isFallFlying()
			&& BetterElytraTakeoffState.canUseTakeoff(player);
	}
}

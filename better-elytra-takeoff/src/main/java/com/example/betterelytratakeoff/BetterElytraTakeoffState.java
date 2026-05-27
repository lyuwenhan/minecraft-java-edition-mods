package com.example.betterelytratakeoff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public final class BetterElytraTakeoffState {
	private static final int FIRST_ROCKET_TICK = 1;
	private static final int LAST_GLIDING_TICK = -8;
	private static final String INFINITY_FIREWORKS_MOD_ID = "infinity-fireworks";
	private static final Map<UUID, PendingTakeoff> PENDING_TAKEOFFS = new HashMap<>();

	private BetterElytraTakeoffState() {
	}

	public static void schedule(ServerPlayerEntity player, Hand hand, ItemStack stack) {
		ItemStack rocket = stack.copyWithCount(1);
		PENDING_TAKEOFFS.put(player.getUuid(), new PendingTakeoff(FIRST_ROCKET_TICK, hand, rocket));

		player.startGliding();

		if (!player.isCreative() && !FabricLoader.getInstance().isModLoaded(INFINITY_FIREWORKS_MOD_ID)) {
			stack.decrement(1);
		}
		player.incrementStat(Stats.USED.getOrCreateStat(Items.FIREWORK_ROCKET));
	}

	public static void tick(ServerPlayerEntity player) {
		PendingTakeoff pending = PENDING_TAKEOFFS.get(player.getUuid());
		if (pending == null) {
			return;
		}

		if (pending.ticksLeft < LAST_GLIDING_TICK) {
			PENDING_TAKEOFFS.remove(player.getUuid());
			return;
		}

		if (!canUseTakeoff(player)) {
			PENDING_TAKEOFFS.remove(player.getUuid());
			return;
		}

		player.startGliding();

		if (pending.ticksLeft == FIRST_ROCKET_TICK && player.getEntityWorld() instanceof ServerWorld serverWorld) {
			ProjectileEntity.spawn(new FireworkRocketEntity(serverWorld, pending.rocket, player), serverWorld, pending.rocket);
			PENDING_TAKEOFFS.put(player.getUuid(), pending.next());
			return;
		}

		PENDING_TAKEOFFS.put(player.getUuid(), pending.next());
	}

	public static boolean canUseTakeoff(ServerPlayerEntity player) {
		return !player.isSpectator()
				&& !player.hasVehicle()
				&& !player.getAbilities().flying
				&& isWearingGlider(player);
	}

	private static boolean isWearingGlider(ServerPlayerEntity player) {
		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (slot == EquipmentSlot.OFFHAND) {
				continue;
			}

			if (player.getEquippedStack(slot).contains(DataComponentTypes.GLIDER)) {
				return true;
			}
		}

		return false;
	}

	private record PendingTakeoff(int ticksLeft, Hand hand, ItemStack rocket) {
		private PendingTakeoff next() {
			return new PendingTakeoff(this.ticksLeft - 1, this.hand, this.rocket);
		}
	}
}

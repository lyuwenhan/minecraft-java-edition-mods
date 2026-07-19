package com.example.betterelytratakeoff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BetterElytraTakeoffState {
	private static final int FIRST_ROCKET_TICK = 1;
	private static final int LAST_GLIDING_TICK = -8;
	private static final String INFINITY_FIREWORKS_MOD_ID = "infinity-fireworks";
	private static final Map<UUID, PendingTakeoff> PENDING_TAKEOFFS = new HashMap<>();

	private BetterElytraTakeoffState() {}

	public static void schedule(ServerPlayer player, ItemStack stack) {
		ItemStack rocket = stack.copyWithCount(1);
		PENDING_TAKEOFFS.put(player.getUUID(), new PendingTakeoff(FIRST_ROCKET_TICK, rocket));
		player.startFallFlying();
		if (!player.isCreative()
				&& !FabricLoader.getInstance().isModLoaded(INFINITY_FIREWORKS_MOD_ID)) {
			stack.shrink(1);
		}
		player.awardStat(Stats.ITEM_USED.get(Items.FIREWORK_ROCKET));
	}

	public static void tick(ServerPlayer player) {
		PendingTakeoff pending = PENDING_TAKEOFFS.get(player.getUUID());
		if (pending == null) {
			return;
		}
		if (pending.ticksLeft < LAST_GLIDING_TICK) {
			PENDING_TAKEOFFS.remove(player.getUUID());
			return;
		}
		if (!canUseTakeoff(player)) {
			PENDING_TAKEOFFS.remove(player.getUUID());
			return;
		}
		player.startFallFlying();
		if (pending.ticksLeft == FIRST_ROCKET_TICK
				&& player.level() instanceof ServerLevel serverLevel) {
			FireworkRocketEntity firework = new FireworkRocketEntity(serverLevel, pending.rocket, player);
			serverLevel.addFreshEntity(firework);
			PENDING_TAKEOFFS.put(player.getUUID(), pending.next());
			return;
		}
		PENDING_TAKEOFFS.put(player.getUUID(), pending.next());
	}

	public static boolean canUseTakeoff(ServerPlayer player) {
		return !player.isSpectator()
				&& !player.isPassenger()
				&& !player.getAbilities().flying
				&& isWearingGlider(player);
	}

	private static boolean isWearingGlider(ServerPlayer player) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot == EquipmentSlot.OFFHAND) {
				continue;
			}
			if (player.getItemBySlot(slot).has(DataComponents.GLIDER)) {
				return true;
			}
		}
		return false;
	}

	private record PendingTakeoff(int ticksLeft, ItemStack rocket) {
		private PendingTakeoff next() {
			return new PendingTakeoff(this.ticksLeft - 1, this.rocket);
		}
	}
}

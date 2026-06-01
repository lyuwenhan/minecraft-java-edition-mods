package com.example.clientflying;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

public class ClientFlyingMod implements ClientModInitializer {
	private boolean first = true;
	private boolean lastElytra = false;
	private void resetState() {
		first = true;
		lastElytra = false;
	}
	@Override
	public void onInitializeClient() {
		System.out.println("[ClientFlying] Client initialized");
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			resetState();
			System.out.println("[ClientFlying] State reset on join");
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			resetState();
			System.out.println("[ClientFlying] State reset on disconnect");
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			if (client.gameMode == null) {
				return;
			}
			GameType gameMode = client.gameMode.getPlayerMode();
			boolean validGameMode = gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE;
			ItemStack chestStack = client.player.getItemBySlot(EquipmentSlot.CHEST);
			boolean wearingGlider = chestStack.get(DataComponents.GLIDER) != null;
			boolean inAir = !client.player.onGround();
			boolean shouldStartGliding = false;
			if (validGameMode) {
				if (inAir && (wearingGlider != lastElytra || (first && inAir))) {
					client.player.getAbilities().flying = !wearingGlider;
					shouldStartGliding = wearingGlider;
				}

				client.player.getAbilities().mayfly = true;
				syncAbilities(client);
			}
			ClientPacketListener connection = client.getConnection();
			if (connection != null) {
				if (wearingGlider && !client.player.getAbilities().flying) {
					if (shouldStartGliding) {
						connection.send(new ServerboundPlayerCommandPacket(
							client.player,
							ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
						));
					}
				} else {
					connection.send(new ServerboundMovePlayerPacket.PosRot(
						client.player.getX(),
						client.player.getY(),
						client.player.getZ(),
						client.player.getYRot(),
						client.player.getXRot(),
						true,
						client.player.horizontalCollision
					));
				}
			}
			lastElytra = wearingGlider;
			first = false;
		});
	}
	private static void syncAbilities(Minecraft client) {
		if (client.player == null) {
			return;
		}
		ClientPacketListener connection = client.getConnection();
		if (connection == null) {
			return;
		}
		connection.send(new ServerboundPlayerAbilitiesPacket(client.player.getAbilities()));
	}
}

package com.example.clientflying;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.GameMode;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class ClientFlyingMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("[ClientFlying] Client initialized");
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.interactionManager != null) {
				GameMode gameMode = client.interactionManager.getCurrentGameMode();
				boolean isOkMode = gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
				ItemStack chestStack = client.player.getEquippedStack(EquipmentSlot.CHEST);
				boolean isElytra = chestStack.isOf(Items.ELYTRA);
				boolean isInAir = !client.player.isOnGround();
				boolean needEly = false;
				if (isOkMode) {
					if(isInAir && isElytra == client.player.getAbilities().allowFlying){
						client.player.getAbilities().flying = !isElytra;
						needEly = isElytra;
					}
					client.player.getAbilities().allowFlying = !isElytra;
					client.player.sendAbilitiesUpdate();
				}
				ClientPlayNetworkHandler net = client.getNetworkHandler();
				if (net != null){
					if(isElytra && (needEly || client.player.isGliding())){
						if(needEly){
							net.sendPacket(new ClientCommandC2SPacket(
								client.player,
								ClientCommandC2SPacket.Mode.START_FALL_FLYING
							));
						}
					}else{
						double x = client.player.getX();
						double y = client.player.getY();
						double z = client.player.getZ();

						float yaw = client.player.getYaw();
						float pitch = client.player.getPitch();

						net.sendPacket(new PlayerMoveC2SPacket.Full(
							x, y, z,
							yaw, pitch,
							true,
							client.player.horizontalCollision
						));
					}
				}
			}
		});
	}
}

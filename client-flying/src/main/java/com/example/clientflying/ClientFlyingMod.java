package com.example.clientflying;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import org.lwjgl.glfw.GLFW;

public class ClientFlyingMod implements ClientModInitializer {
    private static final String MOD_ID = "client-flying";
    private static final String KEY_TOGGLE = "key.client-flying.toggle";

    private final KeyMapping.Category category =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "client_flying"));

    private KeyMapping toggleKey;
    private ClientFlyingConfig config = new ClientFlyingConfig();

    private boolean first = true;
    private boolean lastElytra = false;
    private boolean lastFlying = false;
    private boolean lastFallFlying = false;
    private static int notFlyingTicks = 0;

    private static int startFallFlyingResendTicks = 0;
    private static boolean sendingInternalStartFallFlyingPacket = false;

    private void resetState() {
        first = true;
        lastElytra = false;
        lastFlying = false;
        lastFallFlying = false;
        notFlyingTicks = 0;
        startFallFlyingResendTicks = 0;
        sendingInternalStartFallFlyingPacket = false;
    }

    private void disableClientFlight(Minecraft client) {
        if (client.player == null || client.gameMode == null) {
            resetState();
            return;
        }

        GameType gameMode = client.gameMode.getPlayerMode();

        if (gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE) {
            client.player.getAbilities().flying = false;
            client.player.getAbilities().mayfly = false;
        }

        resetState();
    }

    private void handleToggleKey(Minecraft client) {
        if (toggleKey == null || client.player == null) {
            return;
        }

        while (toggleKey.consumeClick()) {
            config.enabled = !config.enabled;

            if (config.enabled) {
                resetState();
            } else {
                disableClientFlight(client);
            }

            config.save();

            client.player.sendOverlayMessage(
                    Component.literal("Client Flying: " + (config.enabled ? "ON" : "OFF")));
        }
    }

    public static boolean isSendingInternalStartFallFlyingPacket() {
        return sendingInternalStartFallFlyingPacket;
    }

    public static void onClientStartFallFlyingPacket() {
        if (sendingInternalStartFallFlyingPacket) {
            return;
        }

        startFallFlyingResendTicks = 10;
        notFlyingTicks = 0;
    }

    private static void sendInternalStartFallFlyingPacket(
            Minecraft client, ClientPacketListener connection) {
        if (client.player == null) {
            return;
        }

        sendingInternalStartFallFlyingPacket = true;

        try {
            connection.send(
                    new ServerboundPlayerCommandPacket(
                            client.player,
                            ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        } finally {
            sendingInternalStartFallFlyingPacket = false;
        }
    }

    @Override
    public void onInitializeClient() {
        System.out.println("[ClientFlying] Client initialized");

        config = ClientFlyingConfig.load();

        toggleKey =
                KeyMappingHelper.registerKeyMapping(
                        new KeyMapping(
                                KEY_TOGGLE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, category));

        ClientPlayConnectionEvents.JOIN.register(
                (handler, sender, client) -> {
                    resetState();
                    System.out.println("[ClientFlying] State reset on join");
                });

        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    resetState();
                    System.out.println("[ClientFlying] State reset on disconnect");
                });

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (client.player == null || client.gameMode == null) {
                        return;
                    }

                    handleToggleKey(client);

                    if (!config.enabled) {
                        return;
                    }

                    GameType gameMode = client.gameMode.getPlayerMode();

                    if (gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE) {
                        ItemStack chestStack = client.player.getItemBySlot(EquipmentSlot.CHEST);
                        boolean wearingGlider = chestStack.get(DataComponents.GLIDER) != null;
                        boolean inAir = !client.player.onGround();
                        boolean shouldStartGliding = false;

                        client.player.getAbilities().mayfly = true;

                        if (inAir && (first || wearingGlider != lastElytra)) {
                            client.player.getAbilities().flying = !wearingGlider;
                            shouldStartGliding = wearingGlider;
                        }

                        ClientPacketListener connection = client.getConnection();
                        if (connection != null) {
                            if (startFallFlyingResendTicks > 0) {
                                client.player.getAbilities().flying = false;
                                if (client.player.isFallFlying() || !wearingGlider || !inAir) {
                                    startFallFlyingResendTicks = 0;
                                } else {
                                    sendInternalStartFallFlyingPacket(client, connection);
                                    startFallFlyingResendTicks = startFallFlyingResendTicks - 1;
                                }
                            } else if (inAir
                                    && wearingGlider
                                    && (shouldStartGliding
                                            || (client.player.isFallFlying()
                                                    && !client.player.getAbilities().flying))) {
                                client.player.getAbilities().flying = false;
                                if (shouldStartGliding) {
                                    notFlyingTicks = 0;
                                    startFallFlyingResendTicks = 10;
                                    sendInternalStartFallFlyingPacket(client, connection);
                                }
                            } else {
                                if (!lastFlying
                                        && client.player.getAbilities().flying
                                        && lastFallFlying) {
                                    notFlyingTicks = 10;
                                    client.player.stopFallFlying();
                                }
                                connection.send(
                                        new ServerboundMovePlayerPacket.PosRot(
                                                client.player.getX(),
                                                client.player.getY(),
                                                client.player.getZ(),
                                                client.player.getYRot(),
                                                client.player.getXRot(),
                                                (notFlyingTicks == 0) || !inAir,
                                                client.player.horizontalCollision));
                                notFlyingTicks = Math.max(0, notFlyingTicks - 1);
                            }
                        }

                        lastElytra = wearingGlider;
                        lastFlying = client.player.getAbilities().flying;
                        lastFallFlying = client.player.isFallFlying();
                        first = false;
                    } else {
                        resetState();
                    }
                });
    }
}

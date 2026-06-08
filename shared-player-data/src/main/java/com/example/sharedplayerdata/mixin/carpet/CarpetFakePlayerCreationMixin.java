package com.example.sharedplayerdata.mixin.carpet;

import com.example.sharedplayerdata.SharedPlayerDataMod;
import com.example.sharedplayerdata.SharedProfileManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(targets = "carpet.patches.EntityPlayerMPFake", remap = false)
public abstract class CarpetFakePlayerCreationMixin {
    private static final ThreadLocal<UUID> sharedPlayerData$reservedSpawnUuid = new ThreadLocal<>();

    private CarpetFakePlayerCreationMixin() {
    }

    @Inject(method = "createFake", at = @At("HEAD"), cancellable = true)
    private static void sharedPlayerData$prepareBoundCreateFake(
            String username,
            MinecraftServer server,
            Vec3 pos,
            double yaw,
            double pitch,
            ResourceKey<Level> dimensionId,
            GameType gamemode,
            boolean flying,
            CallbackInfoReturnable<Boolean> cir
    ) {
        sharedPlayerData$reservedSpawnUuid.remove();
        SharedProfileManager.CarpetFakeSpawnDecision decision = SharedPlayerDataMod.MANAGER.prepareCarpetFakeSpawn(server, username);

        if (!decision.allowed()) {
            SharedPlayerDataMod.LOGGER.warn(
                    "Blocked Carpet fake player spawn for '{}' because the Shared Player Data group is already occupied or could not be prepared.",
                    username
            );
            cir.setReturnValue(false);
            return;
        }

        Optional<UUID> optionalReservedUuid = decision.reservedUuid();

        if (optionalReservedUuid.isPresent()) {
            sharedPlayerData$reservedSpawnUuid.set(optionalReservedUuid.get());
        }
    }

    @Inject(method = "createFake", at = @At("RETURN"))
    private static void sharedPlayerData$releaseFailedBoundCreateFake(
            String username,
            MinecraftServer server,
            Vec3 pos,
            double yaw,
            double pitch,
            ResourceKey<Level> dimensionId,
            GameType gamemode,
            boolean flying,
            CallbackInfoReturnable<Boolean> cir
    ) {
        UUID reservedUuid = sharedPlayerData$reservedSpawnUuid.get();
        sharedPlayerData$reservedSpawnUuid.remove();

        if (reservedUuid == null) {
            return;
        }

        Boolean created = cir.getReturnValue();

        if (Boolean.TRUE.equals(created)) {
            return;
        }

        SharedPlayerDataMod.MANAGER.releaseExternalReservation(reservedUuid);
        SharedPlayerDataMod.LOGGER.warn(
                "Released Shared Player Data reservation for Carpet fake spawn '{}' ({}) because Carpet did not create the fake player.",
                username,
                reservedUuid
        );
    }
}

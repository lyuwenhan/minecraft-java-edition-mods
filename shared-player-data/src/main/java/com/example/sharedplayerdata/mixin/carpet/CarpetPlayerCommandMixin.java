package com.example.sharedplayerdata.mixin.carpet;

import carpet.utils.Messenger;
import com.example.sharedplayerdata.SharedPlayerDataMod;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "carpet.commands.PlayerCommand", remap = false)
public abstract class CarpetPlayerCommandMixin {
    private CarpetPlayerCommandMixin() {
    }

    @Inject(
            method = "spawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lcarpet/patches/EntityPlayerMPFake;createFake(Ljava/lang/String;Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/phys/Vec3;DDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/GameType;Z)Z"
            ),
            cancellable = true
    )
    private static void sharedPlayerData$blockOccupiedBoundSpawnAfterCarpetChecks(
            CommandContext<CommandSourceStack> context,
            CallbackInfoReturnable<Integer> cir
    ) {
        String playerName = StringArgumentType.getString(context, "player");

        if (!SharedPlayerDataMod.MANAGER.isKnownBoundPlayerGroupOccupied(context.getSource().getServer(), playerName)) {
            return;
        }

        Messenger.m(
                context.getSource(),
                "r Player ",
                "rb " + playerName,
                "r  is in a Shared Player Data group that is already online"
        );
        cir.setReturnValue(0);
    }
}

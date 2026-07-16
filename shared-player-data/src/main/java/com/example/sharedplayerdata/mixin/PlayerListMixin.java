package com.example.sharedplayerdata.mixin;

import com.example.sharedplayerdata.LoginDecision;
import com.example.sharedplayerdata.SharedPlayerDataMod;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Inject(method = "canPlayerLogin", at = @At("RETURN"), cancellable = true)
	private void sharedplayerdata$afterVanillaCanPlayerLogin(
			SocketAddress address, NameAndId nameAndId, CallbackInfoReturnable<Component> cir) {
		if (cir.getReturnValue() != null) {
			return;
		}

		MinecraftServer server = ((PlayerList) (Object) this).getServer();
		LoginDecision decision =
				SharedPlayerDataMod.MANAGER.afterVanillaCanPlayerLogin(
						server, nameAndId.id(), nameAndId.name());

		if (!decision.allowed()) {
			cir.setReturnValue(decision.reason());
		}
	}

	@Inject(method = "placeNewPlayer", at = @At("TAIL"))
	private void sharedplayerdata$afterPlaceNewPlayer(
			Connection connection,
			ServerPlayer player,
			CommonListenerCookie cookie,
			CallbackInfo ci) {
		MinecraftServer server = ((PlayerList) (Object) this).getServer();
		SharedPlayerDataMod.MANAGER.markPlayJoined(server, player);
	}

	@Inject(method = "remove", at = @At("TAIL"))
	private void sharedplayerdata$afterRemove(ServerPlayer player, CallbackInfo ci) {
		MinecraftServer server = ((PlayerList) (Object) this).getServer();
		SharedPlayerDataMod.MANAGER.afterPlayerRemoved(server, player);
	}

	@Inject(method = "op(Lnet/minecraft/server/players/NameAndId;)V", at = @At("TAIL"))
	private void sharedplayerdata$afterOp(NameAndId nameAndId, CallbackInfo ci) {
		MinecraftServer server = ((PlayerList) (Object) this).getServer();
		SharedPlayerDataMod.MANAGER.afterOperatorStatusChanged(server, nameAndId, true);
	}

	@Inject(
			method =
					"op(Lnet/minecraft/server/players/NameAndId;Ljava/util/Optional;Ljava/util/Optional;)V",
			at = @At("TAIL"))
	private void sharedplayerdata$afterOpWithDetails(
			NameAndId nameAndId,
			Optional<LevelBasedPermissionSet> permissions,
			Optional<Boolean> canBypassPlayerLimit,
			CallbackInfo ci) {
		MinecraftServer server = ((PlayerList) (Object) this).getServer();
		SharedPlayerDataMod.MANAGER.afterOperatorStatusChanged(server, nameAndId, true);
	}

	@Inject(method = "deop(Lnet/minecraft/server/players/NameAndId;)V", at = @At("TAIL"))
	private void sharedplayerdata$afterDeop(NameAndId nameAndId, CallbackInfo ci) {
		MinecraftServer server = ((PlayerList) (Object) this).getServer();
		SharedPlayerDataMod.MANAGER.afterOperatorStatusChanged(server, nameAndId, false);
	}
}

package com.example.autogreeting.client.mixin;

import com.example.autogreeting.client.AutoGreetingClientDelay;
import com.example.autogreeting.client.AutoGreetingClientMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

	@Inject(method = "onPlayerList", at = @At("TAIL"))
	private void onPlayerList(PlayerListS2CPacket packet, CallbackInfo ci) {
		if (!AutoGreetingClientMod.CONFIG.otherEnabled) return;

		if (System.currentTimeMillis() - AutoGreetingClientMod.joinWorldAt < 1000) return;

		if (!packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		packet.getEntries().forEach(entry -> {
			String name = entry.profile().name();
			String uuid = entry.profile().id().toString();

			if(AutoGreetingClientMod.CONFIG.otherBlacklist.match(name) && !AutoGreetingClientMod.CONFIG.otherBlacklistExcept.match(name)) {
				return;
			}
			
			if(!AutoGreetingClientMod.CONFIG.otherWhitelist.isEmpty() && (!AutoGreetingClientMod.CONFIG.otherWhitelist.match(name) || AutoGreetingClientMod.CONFIG.otherWhitelistExcept.match(name))) {
				return;
			}

			if (name.equals(client.player.getName().getString())) {
				return;
			}

			AutoGreetingClientDelay.greetAfter1Second(name, uuid);
		});
	}
}

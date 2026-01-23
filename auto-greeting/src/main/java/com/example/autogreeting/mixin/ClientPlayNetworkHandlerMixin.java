package com.example.autogreeting.mixin;

import com.example.autogreeting.AutoGreetingDelay;
import com.example.autogreeting.AutoGreetingMod;
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
		if (!AutoGreetingMod.CONFIG.otherEnabled) return;

		if (System.currentTimeMillis() - AutoGreetingMod.joinWorldAt < 1000) return;

		if (!packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) return;

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		packet.getEntries().forEach(entry -> {
			String name = entry.profile().name();

			if (name.equals(client.player.getName().getString())) return;

			AutoGreetingDelay.greetAfter1Second(name);
		});
	}
}

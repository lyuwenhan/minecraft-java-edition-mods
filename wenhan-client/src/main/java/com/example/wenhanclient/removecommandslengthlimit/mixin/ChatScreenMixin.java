package com.example.wenhanclient.removecommandslengthlimit.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
	private static final int CHAT_SEGMENT_LENGTH = 256;

	@ModifyArg(
			method = "init",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/client/gui/components/EditBox;setMaxLength(I)V"))
	private int removeCommandsLengthLimit$expandChatInputLimit(int maxLength) {
		return Integer.MAX_VALUE;
	}

	@Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
	private void removeCommandsLengthLimit$sendLongInput(
			String message, boolean addToRecentChat, CallbackInfo callbackInfo) {
		String normalized = removeCommandsLengthLimit$normalizeWithoutTruncating(message);
		if (normalized.isEmpty()) {
			callbackInfo.cancel();
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (addToRecentChat) {
			minecraft.gui.hud.getChat().addRecentChat(normalized);
		}
		if (normalized.startsWith("/")) {
			minecraft.player.connection.sendCommand(normalized.substring(1));
		} else {
			for (int start = 0; start < normalized.length(); ) {
				int end =
						removeCommandsLengthLimit$segmentEnd(
								normalized, start, start + CHAT_SEGMENT_LENGTH);
				minecraft.player.connection.sendChat(normalized.substring(start, end));
				start = end;
			}
		}

		callbackInfo.cancel();
	}

	private static String removeCommandsLengthLimit$normalizeWithoutTruncating(String message) {
		return StringUtils.normalizeSpace(message.trim());
	}

	private static int removeCommandsLengthLimit$segmentEnd(String value, int start, int end) {
		int clampedEnd = Math.min(end, value.length());
		if (clampedEnd > start && Character.isHighSurrogate(value.charAt(clampedEnd - 1))) {
			clampedEnd--;
		}
		return clampedEnd <= start ? Math.min(end, value.length()) : clampedEnd;
	}
}

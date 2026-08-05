package com.example.forwardlock;

import com.example.forwardlock.mixin.KeyMappingAccessor;
import com.example.forwardlock.mixin.PlayerInvoker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ForwardLockClient implements ClientModInitializer {
	private static final ForwardLockController CONTROLLER = new ForwardLockController();

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(ForwardLockClient::onEndClientTick);
	}

	public static boolean isLocked() {
		return CONTROLLER.isLocked();
	}

	public static boolean shouldKeepSprinting(LocalPlayer player) {
		return CONTROLLER.isLocked()
				&& player != null
				&& ((PlayerInvoker) player).forwardLock$hasEnoughFoodToDoExhaustiveManoeuvres();
	}

	private static void onEndClientTick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			CONTROLLER.reset();
			return;
		}

		boolean forwardDown = rawDown(client.options.keyUp);
		boolean backwardDown = rawDown(client.options.keyDown);
		boolean crouchDown = rawDown(client.options.keyShift);

		// GUI key presses must not activate or cancel the lock. The raw states are
		// still synchronized so a key held while closing a screen is not treated as
		// a fresh press.
		CONTROLLER.update(forwardDown, backwardDown, crouchDown, client.screen == null);

		if (shouldKeepSprinting(player)) {
			player.setSprinting(true);
		}
	}

	private static boolean rawDown(KeyMapping keyMapping) {
		return ((KeyMappingAccessor) (Object) keyMapping).forwardLock$isRawDown();
	}

	public ForwardLockClient() {}
}

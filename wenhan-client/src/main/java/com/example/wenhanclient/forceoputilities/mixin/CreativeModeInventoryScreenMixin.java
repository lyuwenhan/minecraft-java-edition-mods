package com.example.wenhanclient.forceoputilities.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
	@Shadow @Final private boolean displayOperatorCreativeTab;

	@Inject(method = "hasPermissions", at = @At("HEAD"), cancellable = true)
	private void wc$showOpUtilitiesWhenLocalOptionEnabled(
			Player player, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(displayOperatorCreativeTab);
	}
}

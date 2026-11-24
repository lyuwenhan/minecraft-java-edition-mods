package com.example.wenhanclient.hiderealms.mixin;

import com.example.wenhanclient.hiderealms.HideRealmsSubMod;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
	private static final Component REALMS_BUTTON_TEXT = Component.translatable("menu.online");
	private static final int MENU_ROW_HEIGHT = 24;
	private static final int COPYRIGHT_BOTTOM_MARGIN = 30;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "realmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
	private void hideRealms$disableNotifications(CallbackInfoReturnable<Boolean> cir) {
		if (HideRealmsSubMod.isEnabled()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void hideRealms$hideRealmsButton(CallbackInfo ci) {
		if (!HideRealmsSubMod.isEnabled()) {
			return;
		}

		AbstractWidget realmsButton = null;
		for (GuiEventListener child : this.children()) {
			if (child instanceof AbstractWidget widget
					&& widget.getMessage().getString().equals(REALMS_BUTTON_TEXT.getString())) {
				realmsButton = widget;
				break;
			}
		}

		if (realmsButton == null) {
			return;
		}

		int realmsY = realmsButton.getY();
		realmsButton.visible = false;
		realmsButton.active = false;

		for (GuiEventListener child : this.children()) {
			if (child instanceof AbstractWidget widget
					&& widget != realmsButton
					&& widget.getY() > realmsY
					&& widget.getY() < this.height - COPYRIGHT_BOTTOM_MARGIN) {
				widget.setY(widget.getY() - MENU_ROW_HEIGHT);
			}
		}
	}
}

package com.example.hidepassword.mixin;

import com.example.hidepassword.HidePasswordMod;
import com.example.hidepassword.util.PasswordMasker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EditBox.class)
public abstract class TextFieldWidgetMixin {
	@Shadow
	public abstract String getValue();

	@Shadow @Final private List<EditBox.TextFormatter> formatters;

	@Unique private boolean hidepassword$formatterInstalled;

	@Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
	private void hidepassword$installRenderOnlyFormatter(
			GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (hidepassword$formatterInstalled) {
			return;
		}

		// Put the password formatter first so later command-syntax formatters cannot expose the
		// password.
		formatters.add(0, this::hidepassword$formatRenderSegment);
		hidepassword$formatterInstalled = true;
	}

	@Unique
	private FormattedCharSequence hidepassword$formatRenderSegment(String text, int offset) {
		if (HidePasswordMod.CONFIG == null || !HidePasswordMod.CONFIG.enabled) {
			return null;
		}

		String masked =
				PasswordMasker.maskRenderSegment(
						this.getValue(), text, offset, HidePasswordMod.CONFIG.hideLength);
		return masked == null ? null : FormattedCharSequence.forward(masked, Style.EMPTY);
	}
}

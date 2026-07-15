package com.example.hidepassword.mixin;

import com.example.hidepassword.HidePasswordMod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;

@Mixin(EditBox.class)
public abstract class TextFieldWidgetMixin {
    @Shadow
    public abstract String getValue();

    @Shadow
    public abstract void setValue(String value);

    private String hidepassword$real;
    private boolean hidepassword$active;

    private static final List<String> COMMAND_PREFIXES =
            List.of(
                    "/login",
                    "/l",
                    "/register",
                    "/reg",
                    "/changepassword",
                    "/autologin set",
                    "/account unregister",
                    "/account changepassword");

    private static final String FIXED_MASK = "********";

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void hidepassword$beforeRender(
            GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (HidePasswordMod.CONFIG == null) {
            return;
        }

        if (!HidePasswordMod.CONFIG.enabled) {
            return;
        }

        hidepassword$real = this.getValue();

        String masked = maskIfNeeded(hidepassword$real);
        hidepassword$active = masked != null;

        if (hidepassword$active) {
            this.setValue(masked);
        }
    }

    @Inject(method = "extractWidgetRenderState", at = @At("TAIL"))
    private void hidepassword$afterRender(
            GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (hidepassword$active) {
            this.setValue(hidepassword$real);
            hidepassword$real = null;
            hidepassword$active = false;
        }
    }

    private static String maskIfNeeded(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String lower = input.toLowerCase(Locale.ROOT);

        for (String cmd : COMMAND_PREFIXES) {
            if (lower.startsWith(cmd + " ")) {
                int prefixLen = cmd.length();
                String visiblePrefix = input.substring(0, prefixLen + 1);

                if (visiblePrefix.isEmpty()) {
                    return "";
                }

                String password = input.substring(prefixLen + 1);
                return visiblePrefix + maskPassword(password);
            }
        }

        return null;
    }

    private static String maskPassword(String password) {
        if (HidePasswordMod.CONFIG.hideLength) {
            return FIXED_MASK;
        }

        return password.replaceAll("\\S", "*");
    }
}

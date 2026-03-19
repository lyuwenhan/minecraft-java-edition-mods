package com.example.hidepassword.mixin;
import com.example.hidepassword.HidePasswordMod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {

    @Shadow
    private String text;

    @Shadow
    protected abstract void setText(String text);

    private String hidepassword$real;
    private boolean hidepassword$active;

    private static final List<String> COMMAND_PREFIXES = List.of(
            "/login",
            "/l",
            "/register",
            "/reg",
            "/changepassword",
            "/autologin set",
            "/account unregister",
            "/account changepassword"
    );

    /* ===== 渲染前：替换为 ***** ===== */

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void hidepassword$beforeRender(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        if(!HidePasswordMod.CONFIG.enabled) {
            return;
        }
        hidepassword$real = this.text;

        String masked = maskIfNeeded(hidepassword$real);
        hidepassword$active = masked != null;

        if (hidepassword$active) {
            setText(masked);
        }
    }

    /* ===== 渲染后：恢复真实文本 ===== */

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void hidepassword$afterRender(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        if (hidepassword$active) {
            setText(hidepassword$real);
            hidepassword$real = null;  // Clear sensitive data immediately
        }
    }

    /* ===== 逻辑 ===== */
    /* 使用固定长度掩码，避免泄露密码长度和结构（符合 CWE-549 / 肩窥防护最佳实践） */

    private static final String FIXED_MASK = "********";

    private static String maskIfNeeded(String input) {
        if (input == null || input.isEmpty()) return null;

        String lower = input.toLowerCase(Locale.ROOT);

        for (String cmd : COMMAND_PREFIXES) {
            if (lower.startsWith(cmd + " ")) {
                int prefixLen = cmd.length();
                String visiblePrefix = input.substring(0, prefixLen + 1);
                if(visiblePrefix.isEmpty()) {
                    return "";
                }
                return visiblePrefix + FIXED_MASK;
            }
        }
        return null;
    }
}

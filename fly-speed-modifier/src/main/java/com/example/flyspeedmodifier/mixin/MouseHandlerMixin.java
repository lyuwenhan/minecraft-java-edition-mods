package com.example.flyspeedmodifier.mixin;

import com.example.flyspeedmodifier.FreecamSpeedController;

import net.minecraft.client.MouseHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void flySpeedModifier$onScroll(
            long window, double horizontalScroll, double verticalScroll, CallbackInfo ci) {
        if (FreecamSpeedController.handleMouseScroll(verticalScroll)) {
            ci.cancel();
        }
    }
}

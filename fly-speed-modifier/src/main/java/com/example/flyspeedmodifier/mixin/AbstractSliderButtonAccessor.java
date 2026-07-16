package com.example.flyspeedmodifier.mixin;

import net.minecraft.client.gui.components.AbstractSliderButton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSliderButton.class)
public interface AbstractSliderButtonAccessor {
	@Invoker("setValue")
	void flySpeedModifier$invokeSetValue(double value);
}

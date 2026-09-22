package com.example.dabrserverconfigoverride.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(
		targets = "nl.enjarai.doabarrelroll.compat.yacl.YACLImplementation$Dependable",
		remap = false)
public abstract class YACLDependableMixin {
	@ModifyVariable(method = "set", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private boolean dabrServerConfigOverride$keepAvailable(boolean available) {
		return true;
	}
}

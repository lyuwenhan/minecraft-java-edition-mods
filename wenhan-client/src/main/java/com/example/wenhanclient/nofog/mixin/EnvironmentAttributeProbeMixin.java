package com.example.wenhanclient.nofog.mixin;

import com.example.wenhanclient.nofog.NoFogSubMod;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(EnvironmentAttributeProbe.class)
public abstract class EnvironmentAttributeProbeMixin {
	@Shadow private Level level;

	@Unique private Level noFog$colorLevel;
	@Unique private EnvironmentAttributeSystem noFog$colorAttributes;

	@Unique
	private final Map<EnvironmentAttribute<?>, Object> noFog$previousColors = new HashMap<>();

	@Unique
	private final Map<EnvironmentAttribute<?>, Object> noFog$currentColors = new HashMap<>();

	@Inject(method = "tick", at = @At("HEAD"))
	private void noFog$tickColors(Level level, Vec3 position, CallbackInfo ci) {
		if (!NoFogSubMod.isEnabled() || noFog$colorLevel != level) {
			noFog$resetColors();
			return;
		}
		noFog$previousColors.clear();
		noFog$previousColors.putAll(noFog$currentColors);
		noFog$currentColors.clear();
		if (noFog$colorAttributes != null) {
			noFog$colorAttributes.invalidateTickCache();
		}
	}

	@Inject(method = "reset", at = @At("HEAD"))
	private void noFog$resetColorProbe(CallbackInfo ci) {
		noFog$resetColors();
	}

	@Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
	@SuppressWarnings("unchecked")
	private <Value> void noFog$removeBiomeSkyColors(
			EnvironmentAttribute<Value> attribute,
			float partialTick,
			CallbackInfoReturnable<Value> cir) {
		if (!NoFogSubMod.isEnabled()
				|| !(level instanceof ClientLevel)
				|| (attribute != EnvironmentAttributes.SKY_COLOR
						&& attribute != EnvironmentAttributes.FOG_COLOR
						&& attribute != EnvironmentAttributes.SUNRISE_SUNSET_COLOR
						&& attribute != EnvironmentAttributes.CLOUD_COLOR)) {
			return;
		}
		if (noFog$colorAttributes == null || noFog$colorLevel != level) {
			noFog$resetColors();
			noFog$colorLevel = level;
			// Keep dimension, timeline and weather layers; omit the positional biome layer.
			noFog$colorAttributes =
					EnvironmentAttributeSystem.builder()
							.addConstantLayer(level.dimensionType().attributes())
							.addDynamicLayers(level)
							.build();
		}
		Value current =
				(Value)
						noFog$currentColors.computeIfAbsent(
								attribute,
								key -> noFog$colorAttributes.getValue(attribute, Vec3.ZERO, null));
		Value previous = (Value) noFog$previousColors.getOrDefault(attribute, current);
		cir.setReturnValue(
				attribute.type().partialTickLerp().apply(partialTick, previous, current));
	}

	@Unique
	private void noFog$resetColors() {
		noFog$colorLevel = null;
		noFog$colorAttributes = null;
		noFog$previousColors.clear();
		noFog$currentColors.clear();
	}
}

package com.example.seedgen.mixin;

import com.example.seedgen.xaero.XaeroMapScreenAccess;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public interface XaeroGuiMapMixin extends XaeroMapScreenAccess {
	@Override
	@Accessor(value = "cameraX", remap = false)
	double seedgen$getCameraX();

	@Override
	@Accessor(value = "cameraZ", remap = false)
	double seedgen$getCameraZ();

	@Override
	@Accessor(value = "lastNonNullViewedDimensionId", remap = false)
	ResourceKey<Level> seedgen$getViewedDimension();
}

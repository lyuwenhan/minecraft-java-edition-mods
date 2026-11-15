package com.example.seedgen.xaero;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface XaeroMapScreenAccess {
	double seedgen$getCameraX();

	double seedgen$getCameraZ();

	ResourceKey<Level> seedgen$getViewedDimension();
}

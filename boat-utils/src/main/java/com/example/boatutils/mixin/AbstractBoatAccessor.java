package com.example.boatutils.mixin;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBoat.class)
public interface AbstractBoatAccessor {
	@Accessor("deltaRotation")
	void boatUtils$setDeltaRotation(float deltaRotation);
}

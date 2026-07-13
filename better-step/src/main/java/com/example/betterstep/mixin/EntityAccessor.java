package com.example.betterstep.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

	@Invoker("collide")
	Vec3 betterstep$invokeCollide(Vec3 movement);
}

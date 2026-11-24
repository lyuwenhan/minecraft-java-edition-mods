package com.example.wenhanclient.notexturerotation.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {
	@Accessor("hasCollision")
	boolean noTextureRotation$hasCollision();
}

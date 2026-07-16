package com.example.creepernobreak.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@Mutable @Shadow @Final private boolean fire;

	@Mutable @Shadow @Final private Explosion.BlockInteraction blockInteraction;

	@Inject(
			method =
					"<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Explosion$BlockInteraction;)V",
			at = @At("RETURN"))
	private void creeperNoBreak$keepBlocksForCreepers(
			ServerLevel level,
			Entity source,
			DamageSource damageSource,
			ExplosionDamageCalculator damageCalculator,
			Vec3 center,
			float radius,
			boolean fire,
			Explosion.BlockInteraction blockInteraction,
			CallbackInfo callbackInfo) {
		if (source instanceof Creeper) {
			this.fire = false;
			this.blockInteraction = Explosion.BlockInteraction.KEEP;
		}
	}
}

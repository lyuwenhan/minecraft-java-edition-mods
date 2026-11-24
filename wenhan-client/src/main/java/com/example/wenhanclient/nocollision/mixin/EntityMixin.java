package com.example.wenhanclient.nocollision.mixin;

import com.example.wenhanclient.nocollision.NoCollisionSubMod;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Redirect(
			method = "push(Lnet/minecraft/world/entity/Entity;)V",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/entity/Entity;push(DDD)V",
							ordinal = 0))
	private void noCollision$skipLocalPlayerPrimaryPush(
			Entity entity, double x, double y, double z) {
		this.noCollision$pushUnlessLocalPlayer(entity, x, y, z);
	}

	@Redirect(
			method = "push(Lnet/minecraft/world/entity/Entity;)V",
			at =
					@At(
							value = "INVOKE",
							target = "Lnet/minecraft/world/entity/Entity;push(DDD)V",
							ordinal = 1))
	private void noCollision$skipLocalPlayerSecondaryPush(
			Entity entity, double x, double y, double z) {
		this.noCollision$pushUnlessLocalPlayer(entity, x, y, z);
	}

	private void noCollision$pushUnlessLocalPlayer(Entity entity, double x, double y, double z) {
		if (NoCollisionSubMod.isEnabled() && entity instanceof LocalPlayer) {
			return;
		}
		entity.push(x, y, z);
	}
}

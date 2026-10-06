package com.example.wenhanclient.attackthroughfoliage.mixin;

import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow public ClientLevel level;

	@Shadow public LocalPlayer player;

	@Shadow public Entity crosshairPickEntity;

	@Shadow public HitResult hitResult;

	@Shadow
	public abstract Entity getCameraEntity();

	@Inject(method = "pick", at = @At("TAIL"))
	private void attackThroughFoliage$pickEntityBehindNonCollidingBlock(
			float tickDelta, CallbackInfo ci) {
		if (!AttackThroughFoliageSubMod.isEnabled()
				|| this.level == null
				|| this.player == null
				|| !(this.hitResult instanceof BlockHitResult blockHitResult)) {
			return;
		}

		BlockPos blockPos = blockHitResult.getBlockPos();
		BlockState blockState = this.level.getBlockState(blockPos);
		if (!blockState.getCollisionShape(this.level, blockPos).isEmpty()) {
			return;
		}

		Entity cameraEntity = this.getCameraEntity();
		if (cameraEntity == null) {
			return;
		}

		EntityHitResult entityHitResult = this.findEntityHit(cameraEntity, tickDelta);
		if (entityHitResult != null) {
			this.hitResult = entityHitResult;
			this.crosshairPickEntity = entityHitResult.getEntity();
		}
	}

	private EntityHitResult findEntityHit(Entity cameraEntity, float tickDelta) {
		double reach = this.player.entityInteractionRange();
		Vec3 start = cameraEntity.getEyePosition(tickDelta);
		Vec3 view = cameraEntity.getViewVector(tickDelta);
		Vec3 end = start.add(view.x * reach, view.y * reach, view.z * reach);

		HitResult colliderHit =
				this.level.clip(
						new ClipContext(
								start,
								end,
								ClipContext.Block.COLLIDER,
								ClipContext.Fluid.NONE,
								cameraEntity));
		double maxDistanceSquared = reach * reach;
		if (colliderHit.getType() != HitResult.Type.MISS) {
			maxDistanceSquared = colliderHit.getLocation().distanceToSqr(start);
			end = colliderHit.getLocation();
		}

		AABB searchBox =
				cameraEntity
						.getBoundingBox()
						.expandTowards(view.scale(reach))
						.inflate(1.0D, 1.0D, 1.0D);
		return ProjectileUtil.getEntityHitResult(
				cameraEntity,
				start,
				end,
				searchBox,
				EntitySelector.CAN_BE_PICKED,
				maxDistanceSquared);
	}
}

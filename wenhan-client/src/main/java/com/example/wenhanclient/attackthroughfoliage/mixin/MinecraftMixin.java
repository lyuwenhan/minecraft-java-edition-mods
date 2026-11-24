package com.example.wenhanclient.attackthroughfoliage.mixin;

import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageConfig.Mode;
import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow public ClientLevel level;

	@Shadow public LocalPlayer player;

	@Shadow public Entity crosshairPickEntity;

	@Shadow public HitResult hitResult;

	@Shadow
	public abstract Entity getCameraEntity();

	@Inject(method = "pick", at = @At("TAIL"))
	private void attackThroughFoliage$pickEntityThroughBlocks(float tickDelta, CallbackInfo ci) {
		Mode mode = AttackThroughFoliageSubMod.mode();
		if (mode == Mode.NONE || this.level == null || this.player == null) {
			return;
		}

		Entity cameraEntity = this.getCameraEntity();
		if (cameraEntity == null) {
			return;
		}

		EntityHitResult entityHitResult = this.findEntityHit(cameraEntity, tickDelta, mode);
		if (entityHitResult != null) {
			this.hitResult = entityHitResult;
			this.crosshairPickEntity = entityHitResult.getEntity();
		}
	}

	private EntityHitResult findEntityHit(Entity cameraEntity, float tickDelta, Mode mode) {
		AttackRange attackRange = this.player.getActiveItem().get(DataComponents.ATTACK_RANGE);
		if (attackRange != null) {
			return this.findEntityHit(cameraEntity, attackRange, mode);
		}

		double reach = this.player.entityInteractionRange();
		Vec3 start = cameraEntity.getEyePosition(tickDelta);
		Vec3 view = cameraEntity.getViewVector(tickDelta);
		Vec3 end = start.add(view.x * reach, view.y * reach, view.z * reach);

		double maxDistanceSquared = reach * reach;
		if (mode == Mode.FOLIAGE) {
			HitResult colliderHit =
					this.level.clip(
							new ClipContext(
									start,
									end,
									ClipContext.Block.COLLIDER,
									ClipContext.Fluid.NONE,
									CollisionContext.empty()));
			if (colliderHit.getType() != HitResult.Type.MISS) {
				maxDistanceSquared = colliderHit.getLocation().distanceToSqr(start);
				end = colliderHit.getLocation();
			}
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
				entity ->
						EntitySelector.CAN_BE_PICKED.test(entity)
								&& (mode != Mode.ALL || entity instanceof LivingEntity),
				maxDistanceSquared);
	}

	private EntityHitResult findEntityHit(Entity cameraEntity, AttackRange attackRange, Mode mode) {
		Vec3 view = cameraEntity.getHeadLookAngle();
		Vec3 eye = cameraEntity.getEyePosition();
		double minReach = attackRange.effectiveMinRange(cameraEntity);
		double maxReach =
				attackRange.effectiveMaxRange(cameraEntity)
						+ Math.max(0.0D, cameraEntity.getKnownMovement().dot(view));
		Vec3 start = eye.add(view.scale(minReach));
		Vec3 end = eye.add(view.scale(maxReach));

		if (mode == Mode.FOLIAGE) {
			HitResult colliderHit =
					this.level.clip(
							new ClipContext(
									eye,
									end,
									ClipContext.Block.COLLIDER,
									ClipContext.Fluid.NONE,
									CollisionContext.empty()));
			if (colliderHit.getType() != HitResult.Type.MISS) {
				if (colliderHit.getLocation().distanceToSqr(eye) < start.distanceToSqr(eye)) {
					return null;
				}
				end = colliderHit.getLocation();
			}
		}

		double margin = attackRange.hitboxMargin();
		AABB searchBox =
				AABB.ofSize(start, margin, margin, margin)
						.expandTowards(end.subtract(start))
						.inflate(1.0D);
		Entity closestEntity = null;
		Vec3 closestLocation = null;
		double closestDistanceSquared = Double.MAX_VALUE;
		for (Entity candidate :
				this.level.getEntities(
						cameraEntity,
						searchBox,
						entity ->
								EntitySelector.CAN_BE_PICKED.test(entity)
										&& (mode != Mode.ALL || entity instanceof LivingEntity))) {
			AABB hitbox = candidate.getBoundingBox();
			Optional<Vec3> exactHit = hitbox.clip(start, end);
			Vec3 location;
			boolean marginOnly = false;
			if (hitbox.contains(start)) {
				location = start;
			} else if (exactHit.isPresent()) {
				location = exactHit.get();
			} else if (margin > 0.0D) {
				AABB expandedHitbox = hitbox.inflate(margin);
				Optional<Vec3> expandedHit = expandedHitbox.clip(start, end);
				if (expandedHitbox.contains(start)) {
					location = start;
				} else if (expandedHit.isPresent()) {
					location = expandedHit.get();
				} else {
					continue;
				}
				marginOnly = true;
			} else {
				continue;
			}

			if (marginOnly
					&& mode == Mode.FOLIAGE
					&& this.isColliderBetween(location, hitbox.getCenter())) {
				continue;
			}

			double distanceSquared = start.distanceToSqr(location);
			if (distanceSquared < closestDistanceSquared) {
				closestEntity = candidate;
				closestLocation = location;
				closestDistanceSquared = distanceSquared;
			}
		}

		return closestEntity == null ? null : new EntityHitResult(closestEntity, closestLocation);
	}

	private boolean isColliderBetween(Vec3 start, Vec3 end) {
		HitResult hit =
				this.level.clip(
						new ClipContext(
								start,
								end,
								ClipContext.Block.COLLIDER,
								ClipContext.Fluid.NONE,
								CollisionContext.empty()));
		return hit.getType() != HitResult.Type.MISS
				&& hit.getLocation().distanceToSqr(start) < end.distanceToSqr(start);
	}
}

package com.example.flyspeedmodifier.mixin;

import com.example.flyspeedmodifier.FreecamSpeedController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class OtherMovementSpeedMixin {
    @ModifyArg(
            method = "handleRelativeFrictionAndCalculateMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
            index = 0)
    private float flySpeedModifier$multiplyGroundAndAirMovementSpeed(float originalSpeed) {
        return flySpeedModifier$multiplyMovementSpeed(originalSpeed);
    }

    @ModifyArg(
            method = "travelInWater",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
            index = 0)
    private float flySpeedModifier$multiplyWaterMovementSpeed(float originalSpeed) {
        return flySpeedModifier$multiplyMovementSpeed(originalSpeed);
    }

    @ModifyArg(
            method = "travelInLava",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
            index = 0)
    private float flySpeedModifier$multiplyLavaMovementSpeed(float originalSpeed) {
        return flySpeedModifier$multiplyMovementSpeed(originalSpeed);
    }

    @ModifyArg(
            method = "travelFlying(Lnet/minecraft/world/phys/Vec3;FFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
            index = 0)
    private float flySpeedModifier$multiplyFlyingEntityMovementSpeed(float originalSpeed) {
        return flySpeedModifier$multiplyMovementSpeed(originalSpeed);
    }

    @ModifyArg(
            method = "jumpInLiquid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"),
            index = 1)
    private double flySpeedModifier$multiplyLiquidUpwardInput(double originalMovement) {
        return flySpeedModifier$multiplyVerticalInput(originalMovement);
    }

    @ModifyArg(
            method = "goDownInWater",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"),
            index = 1)
    private double flySpeedModifier$multiplyWaterDownwardInput(double originalMovement) {
        return flySpeedModifier$multiplyVerticalInput(originalMovement);
    }

    @ModifyArg(
            method = "handleRelativeFrictionAndCalculateMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"),
            index = 1)
    private double flySpeedModifier$multiplyClimbingUpwardMovement(double originalMovement) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.onClimbable()) {
            return originalMovement;
        }
        return flySpeedModifier$multiplyVerticalInput(originalMovement);
    }


    @ModifyArg(
            method = "travelInWater",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"),
            index = 1)
    private double flySpeedModifier$multiplyWaterClimbingUpwardMovement(double originalMovement) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.onClimbable()) {
            return originalMovement;
        }
        return flySpeedModifier$multiplyVerticalInput(originalMovement);
    }

    private float flySpeedModifier$multiplyMovementSpeed(float originalSpeed) {
        if (!flySpeedModifier$isMovementTarget()) {
            return originalSpeed;
        }
        if (!FreecamSpeedController.shouldModifyOtherMovement()) {
            return originalSpeed;
        }
        return (float) (originalSpeed * FreecamSpeedController.otherMovementMultiplier());
    }

    private double flySpeedModifier$multiplyVerticalInput(double originalMovement) {
        if (!flySpeedModifier$isMovementTarget()) {
            return originalMovement;
        }
        if (!FreecamSpeedController.shouldModifyOtherMovement()) {
            return originalMovement;
        }
        return originalMovement * FreecamSpeedController.otherMovementMultiplier();
    }

    private boolean flySpeedModifier$isMovementTarget() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return false;
        }

        if ((Object) this == client.player) {
            return !client.player.isPassenger();
        }

        LivingEntity self = (LivingEntity) (Object) this;
        Entity controllingPassenger = self.getControllingPassenger();
        return controllingPassenger == client.player;
    }
}

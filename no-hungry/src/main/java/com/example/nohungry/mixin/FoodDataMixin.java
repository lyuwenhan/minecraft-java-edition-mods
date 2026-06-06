package com.example.nohungry.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Unique
    private static final int NO_HUNGRY_FULL_FOOD_LEVEL = 20;

    @Unique
    private static final float NO_HUNGRY_FULL_SATURATION_LEVEL = 20.0F;

    @Unique
    private static final float NO_HUNGRY_EMPTY_EXHAUSTION_LEVEL = 0.0F;

    @Shadow
    private int foodLevel;

    @Shadow
    private float saturationLevel;

    @Shadow
    private float exhaustionLevel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void noHungry$afterConstruct(CallbackInfo ci) {
        this.noHungry$fillFoodData();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void noHungry$afterReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.noHungry$fillFoodData();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void noHungry$beforeTick(ServerPlayer player, CallbackInfo ci) {
        this.noHungry$fillFoodData();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void noHungry$afterTick(ServerPlayer player, CallbackInfo ci) {
        this.noHungry$fillFoodData();
    }

    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    private void noHungry$blockExhaustion(float amount, CallbackInfo ci) {
        this.exhaustionLevel = NO_HUNGRY_EMPTY_EXHAUSTION_LEVEL;
        ci.cancel();
    }

    @Inject(method = "eat(IF)V", at = @At("RETURN"))
    private void noHungry$afterEat(int food, float saturationModifier, CallbackInfo ci) {
        this.noHungry$fillFoodData();
    }

    @Inject(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At("RETURN"))
    private void noHungry$afterEatFoodProperties(FoodProperties foodProperties, CallbackInfo ci) {
        this.noHungry$fillFoodData();
    }

    @Unique
    private void noHungry$fillFoodData() {
        this.foodLevel = NO_HUNGRY_FULL_FOOD_LEVEL;
        this.saturationLevel = NO_HUNGRY_FULL_SATURATION_LEVEL;
        this.exhaustionLevel = NO_HUNGRY_EMPTY_EXHAUSTION_LEVEL;
    }
}

package com.example.nohungry.mixin;

import com.example.nohungry.NoHungryConfig;
import com.example.nohungry.NoHungryMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Shadow
    private int foodLevel;

    @Shadow
    private float saturationLevel;

    @Inject(method = "tick", at = @At("RETURN"))
    private void noHungry$enforceMinimumLevels(ServerPlayer player, CallbackInfo ci) {
        NoHungryConfig config = NoHungryMod.getConfig();
        if (config == null || !config.isEnabled()) {
            return;
        }

        int minimumFoodLevel = config.getMinimumFoodLevel();
        float minimumSaturationLevel = config.getMinimumSaturationLevel();

        if (this.foodLevel < minimumFoodLevel) {
            this.foodLevel = minimumFoodLevel;
        }

        if (this.saturationLevel < minimumSaturationLevel) {
            this.saturationLevel = minimumSaturationLevel;
        }
    }
}

package com.example.wenhanclient.stablemining.mixin;

import com.example.wenhanclient.stablemining.StableMiningSubMod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow public HitResult hitResult;

	@Inject(method = "tick", at = @At("TAIL"))
	private void stableMining$aimAtMiningBlock(CallbackInfo ci) {
		StableMiningSubMod.aimAtMiningBlock((Minecraft) (Object) this);
	}

	@Inject(method = "pick", at = @At("TAIL"))
	private void stableMining$preserveMiningBlockPick(float tickDelta, CallbackInfo ci) {
		BlockHitResult miningBlockHitResult =
				StableMiningSubMod.getMiningBlockHitResult((Minecraft) (Object) this);
		if (miningBlockHitResult != null) {
			this.hitResult = miningBlockHitResult;
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"))
	private void stableMining$clearStoppedMining(boolean attackDown, CallbackInfo ci) {
		if (!attackDown) {
			StableMiningSubMod.clearMiningBlock();
		}
	}
}

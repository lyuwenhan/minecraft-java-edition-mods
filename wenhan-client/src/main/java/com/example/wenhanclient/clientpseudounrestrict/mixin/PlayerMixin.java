package com.example.wenhanclient.clientpseudounrestrict.mixin;

import com.example.wenhanclient.clientpseudounrestrict.ClientPseudoUnrestrictSubMod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "blockInteractionRange", at = @At("HEAD"), cancellable = true)
	private void clientPseudoUnrestrict$extendBlockInteractionRange(
			CallbackInfoReturnable<Double> cir) {
		if (ClientPseudoUnrestrictSubMod.isBlockInteractionRangeEnabled()
				&& ClientPseudoUnrestrictSubMod.isLocalPlayer((Player) (Object) this)) {
			cir.setReturnValue(ClientPseudoUnrestrictSubMod.PSEUDO_INTERACTION_RANGE);
		}
	}

	@Inject(method = "entityInteractionRange", at = @At("HEAD"), cancellable = true)
	private void clientPseudoUnrestrict$extendEntityInteractionRange(
			CallbackInfoReturnable<Double> cir) {
		if (ClientPseudoUnrestrictSubMod.isEntityInteractionRangeEnabled()
				&& ClientPseudoUnrestrictSubMod.isLocalPlayer((Player) (Object) this)) {
			cir.setReturnValue(ClientPseudoUnrestrictSubMod.PSEUDO_INTERACTION_RANGE);
		}
	}

	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void clientPseudoUnrestrict$instantDestroySpeed(
			BlockState state, CallbackInfoReturnable<Float> cir) {
		if (ClientPseudoUnrestrictSubMod.isInstantBreakEnabled()
				&& ClientPseudoUnrestrictSubMod.isLocalPlayer((Player) (Object) this)) {
			cir.setReturnValue(ClientPseudoUnrestrictSubMod.PSEUDO_DESTROY_SPEED);
		}
	}
}

package com.example.wenhanclient;

import com.example.wenhanclient.attackthroughfoliage.AttackThroughFoliageSubMod;
import com.example.wenhanclient.autologin.AutoLoginSubMod;
import com.example.wenhanclient.bestarmor.BestArmorSubMod;
import com.example.wenhanclient.betterstep.BetterStepSubMod;
import com.example.wenhanclient.boatutils.BoatUtilsSubMod;
import com.example.wenhanclient.noslowdown.NoSlowdownSubMod;
import com.example.wenhanclient.creativeflying.CreativeFlyingSubMod;
import com.example.wenhanclient.doublejump.DoubleJumpSubMod;
import com.example.wenhanclient.elytracancel.ElytraCancelSubMod;
import com.example.wenhanclient.entityhighlighter.EntityHighlighterSubMod;
import com.example.wenhanclient.flightdisabler.FlightDisablerSubMod;
import com.example.wenhanclient.flyspeedmodifier.FlySpeedModifierSubMod;
import com.example.wenhanclient.forwardlock.ForwardLockSubMod;
import com.example.wenhanclient.ghostblock.GhostblockSubMod;
import com.example.wenhanclient.greet.GreetSubMod;
import com.example.wenhanclient.hidepassword.HidePasswordSubMod;
import com.example.wenhanclient.hiderealms.HideRealmsSubMod;
import com.example.wenhanclient.interactiondelay.InteractionDelaySubMod;
import com.example.wenhanclient.leavebutton.LeaveButtonSubMod;
import com.example.wenhanclient.maplocator.MapLocatorSubMod;
import com.example.wenhanclient.minecartdirection.MinecartDirectionSubMod;
import com.example.wenhanclient.nocollision.NoCollisionSubMod;
import com.example.wenhanclient.noflightreset.NoFlightResetSubMod;
import com.example.wenhanclient.nojumpdelay.NoJumpDelaySubMod;
import com.example.wenhanclient.notexturerotation.NoTextureRotationSubMod;
import com.example.wenhanclient.playerhighlighter.PlayerHighlighterSubMod;
import com.example.wenhanclient.transparentblocks.TransparentBlocksSubMod;

import net.fabricmc.api.ClientModInitializer;

public class WenhanClientMod implements ClientModInitializer {
	public static final WenhanClientConfig CONFIG = WenhanClientConfig.load();

	@Override
	public void onInitializeClient() {
		AttackThroughFoliageSubMod.init();
		AutoLoginSubMod.init();
		BetterStepSubMod.init();
		BestArmorSubMod.init();
		BoatUtilsSubMod.init();
		NoSlowdownSubMod.init();
		CreativeFlyingSubMod.init();
		DoubleJumpSubMod.init();
		ElytraCancelSubMod.init();
		EntityHighlighterSubMod.init();
		FlightDisablerSubMod.init();
		FlySpeedModifierSubMod.init();
		ForwardLockSubMod.init();
		GhostblockSubMod.init();
		HideRealmsSubMod.init();
		HidePasswordSubMod.init();
		InteractionDelaySubMod.init();
		LeaveButtonSubMod.init();
		MapLocatorSubMod.init();
		MinecartDirectionSubMod.init();
		NoCollisionSubMod.init();
		NoFlightResetSubMod.init();
		NoJumpDelaySubMod.init();
		NoTextureRotationSubMod.init();
		PlayerHighlighterSubMod.init();
		TransparentBlocksSubMod.init();
		GreetSubMod.init(CONFIG.greet);
	}
}

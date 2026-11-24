package com.example.wenhanclient.flyspeedmodifier;

import net.fabricmc.loader.api.FabricLoader;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class FlySpeedModifierMixinPlugin implements IMixinConfigPlugin {
	private static final String FREECAM_MOD_ID = "freecam";
	private static final String FREECAM_SPEED_MIXIN =
			"com.example.wenhanclient.flyspeedmodifier.mixin.FreeCameraSpeedMixin";

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (FREECAM_SPEED_MIXIN.equals(mixinClassName)) {
			return FabricLoader.getInstance().isModLoaded(FREECAM_MOD_ID);
		}

		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(
			String targetClassName,
			ClassNode targetClass,
			String mixinClassName,
			IMixinInfo mixinInfo) {}

	@Override
	public void postApply(
			String targetClassName,
			ClassNode targetClass,
			String mixinClassName,
			IMixinInfo mixinInfo) {}
}

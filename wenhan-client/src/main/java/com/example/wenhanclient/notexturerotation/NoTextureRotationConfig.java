package com.example.wenhanclient.notexturerotation;

import com.example.wenhanclient.WenhanClientMod;

public final class NoTextureRotationConfig {
	public boolean enabled = false;
	public boolean disableRandomOffset = true;
	public boolean keepCollisionShapeOffsets = true;

	public static NoTextureRotationConfig load() {
		return WenhanClientMod.CONFIG.noTextureRotation.copy();
	}

	public void save() {
		WenhanClientMod.CONFIG.noTextureRotation = copy();
		WenhanClientMod.CONFIG.save();
	}

	public NoTextureRotationConfig copy() {
		NoTextureRotationConfig copy = new NoTextureRotationConfig();
		copy.enabled = this.enabled;
		copy.disableRandomOffset = this.disableRandomOffset;
		copy.keepCollisionShapeOffsets = this.keepCollisionShapeOffsets;
		return copy;
	}
}

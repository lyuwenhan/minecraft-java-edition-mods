package com.example.wenhanclient.notexturerotation;

import com.example.wenhanclient.WenhanClientMod;

public final class NoTextureRotationConfig {
	public static final boolean DEFAULT_ENABLED = false;
	public static final boolean DEFAULT_DISABLE_RANDOM_OFFSET = true;
	public static final boolean DEFAULT_KEEP_COLLISION_SHAPE_OFFSETS = true;

	public boolean enabled = DEFAULT_ENABLED;
	public boolean disableRandomOffset = DEFAULT_DISABLE_RANDOM_OFFSET;
	public boolean keepCollisionShapeOffsets = DEFAULT_KEEP_COLLISION_SHAPE_OFFSETS;

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
